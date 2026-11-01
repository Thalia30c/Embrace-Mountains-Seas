// utils/battleEngine.js
// 战斗技能效果引擎（纯函数）
//
// 约定：
//   1. 不碰 UI、不调 wx.* 、不依赖 Page —— 输入状态，输出新状态。
//      将来搬到后端时，照此逻辑翻成 Java 即可。
//   2. 技能数值全部来自 passive_skill 表（effect_value / duration_round / target / effect_desc），
//      本文件只描述「怎么生效」，不写死数字。
//   3. 新增技能 = 数据库加一行 + 下面 RULES 加一条，不用改战斗流程。

const DEFAULT_MAX_HP = 10;

/* ========== 基础工具 ========== */

function clone(o) { return JSON.parse(JSON.stringify(o)); }

/** 创建一方的战斗运行时状态 */
function createSlot(hp) {
  const max = (typeof hp === 'number' && hp > 0) ? hp : DEFAULT_MAX_HP;
  return { hp: max, maxHp: max, mods: [], flags: {} };
}

/**
 * 取技能字段
 * @param {Object} skills { P01:{effectType,target,effectValue,durationRound,effectDesc}, ... }
 */
function sv(skills, code, field, fallback) {
  const s = skills && skills[code];
  if (!s) return fallback;
  const v = s[field];
  return (v === undefined || v === null) ? fallback : v;
}

/* ========== 技能规则表 ==========
 * timing:
 *   'mod'      → 进入 mods，持续 durationRound 个半场，回合末衰减
 *   'flag'     → 进入 flags，短期状态
 *   'instant'  → 立即结算，不进 mods
 */
const RULES = {
  P01: { timing: 'mod', stat: 'out', target: 'S', stack: false },    // 己方主动牌伤害+1，不可叠加
  P05: { timing: 'mod', stat: 'out', target: 'S', stack: true },     // 己方后续进攻牌伤害+1
  P08: { timing: 'mod', stat: 'guard', target: 'S', stack: true },   // 己方护盾+2
  P03: { timing: 'mod', stat: 'out', target: 'E' },                  // 对手本回合进攻-1
  P09: { timing: 'mod', stat: 'out', target: 'E' },                  // 对手下一张进攻牌减半
  // P10 表里 target=E 指「反弹给对手」，但触发条件是己方护盾承伤，
  // 所以这个效果必须由己方持有，用 selfHold 覆盖表里的 target，避免挂到对手身上
  P10: { timing: 'mod', stat: 'reflect', target: 'S', stack: false, selfHold: true },
  P06: { timing: 'instant', effect: 'heal' },                        // 回血，不超上限
  P02: { timing: 'instant', effect: 'clear' },                       // 清除对手全部持续被动
  P04: { timing: 'flag', flag: 'magicLocked', target: 'E' },         // 封锁对手下一回合法术
  P07: { timing: 'instant', effect: 'copy' }                         // 复制对手本回合法术效果
};

/* ========== mods 操作 ========== */

function addMod(actor, opponent, code, rule, skills, logs) {
  const target = rule.selfHold ? 'S' : sv(skills, code, 'target', rule.target || 'S');
  const value = sv(skills, code, 'effectValue', 0);
  const turns = sv(skills, code, 'durationRound', 1);
  const holder = (target === 'E') ? opponent : actor;

  const existing = holder.mods.filter(m => m.code === code)[0];
  if (existing) {
    if (rule.stack === false) {                 // 不可叠加：只刷新持续时间
      existing.turns = Math.max(existing.turns, turns);
      return;
    }
  }
  holder.mods.push({ code: code, value: value, turns: turns, fresh: true });
  logs.push(sv(skills, code, 'effectDesc', code));
}

/** 出牌瞬间触发的技能（自身携带的 passiveCode） */
function playSkills(actor, opponent, card, skills, logs) {
  if (!card || !card.passiveCode) return;
  const code = card.passiveCode;
  const rule = RULES[code];
  if (!rule) return;

  if (rule.timing === 'mod') {
    addMod(actor, opponent, code, rule, skills, logs);
  } else if (rule.timing === 'flag') {
    const holder = (sv(skills, code, 'target', 'E') === 'E') ? opponent : actor;
    holder.flags[rule.flag] = true;
    logs.push(sv(skills, code, 'effectDesc', code));
  } else if (rule.timing === 'instant' && rule.effect === 'clear') {
    if (opponent.mods.length) {
      const names = opponent.mods.map(m => m.code).join('、');
      opponent.mods = [];
      logs.push('清除对手全部持续被动：' + names);
    }
  }
  // heal / copy 在数值阶段统一处理
}

/* ========== 阶段 0：封锁（P04） ========== */
function applyLock(slot, card, logs) {
  if (!card) return null;
  if (slot.flags.magicLocked && card.type === 'magic') {
    logs.push('【' + card.name + '】被封锁，本半场无法打出法术牌');
    return null;   // 等同不出牌
  }
  return card;
}

/* ========== 阶段 1：基础数值 ========== */
function baseOf(card, phase, tongue) {
  if (!card) {
    // 免出牌半场：通卡法术能力自动触发
    if (phase === 'bottom' && tongue) return { out: tongue.mana || 0, guard: 0, heal: 0 };
    return { out: 0, guard: 0, heal: 0 };
  }
  const isDef = (card.type === 'defense') || (card.type === 'tongue' && card.kind === 'def');
  return {
    out: card.out || 0,
    guard: isDef ? (card.guard || 0) : 0,
    heal: (card.type === 'magic') ? (card.heal || 0) : 0
  };
}

/* ========== 阶段 2：持续效果修正 ========== */
function modOut(raw, mods) {
  let out = raw || 0;
  (mods || []).forEach(m => {
    if (m.code === 'P01' || m.code === 'P05') out += m.value;   // 己方增益
    if (m.code === 'P03') out -= m.value;                       // 对手减益
  });
  const half = (mods || []).filter(m => m.code === 'P09' && !m.used && !m.fresh)[0];
  if (half) {
    out = Math.floor(out * (100 - (half.value || 50)) / 100);   // effect_value=50 → 减半
    half.used = true;                                            // 只生效一次
  }
  return Math.max(0, out);
}

function modGuard(raw, mods) {
  let g = raw || 0;
  (mods || []).forEach(m => { if (m.code === 'P08') g += m.value; });
  return Math.max(0, g);
}

function reflectValue(slot, skills) {
  const m = (slot.mods || []).filter(mm => mm.code === 'P10')[0];
  if (!m) return 0;
  return m.value || sv(skills, 'P10', 'effectValue', 1);
}

/* ========== 阶段 3：治疗（P06 不超上限） ========== */
function healOf(slot, amount, logs) {
  if (!amount || amount <= 0) return 0;
  const before = slot.hp;
  slot.hp = Math.min(slot.maxHp, slot.hp + amount);
  const gained = slot.hp - before;
  if (gained > 0) logs.push('回复 ' + gained + ' 点血量');
  else logs.push('已达血量上限 ' + slot.maxHp + '，未回血');
  return gained;
}

/* ========== 主入口：结算一个半场 ==========
 * @param {Object} p
 *   x, y          : createSlot() 的状态
 *   xCard, yCard  : 本半场出的牌（null = 不出）
 *   xTongue,yTongue: 通卡（bottom 免出牌时生效）
 *   skills        : { P01:{...}, ... } 技能字典
 *   phase         : 'top' | 'bottom'
 * @returns { x, y, xDmg, yDmg, xHeal, yHeal, xReflect, yReflect, logs }
 */
function computeRound(p) {
  const skills = p.skills || {};
  const logs = [];
  const x = clone(p.x), y = clone(p.y);
  const phase = p.phase;

  /* 0. 封锁检查 */
  const xEff = applyLock(x, p.xCard, logs);
  const yEff = applyLock(y, p.yCard, logs);

  /* 1. 基础数值 */
  const xB = baseOf(xEff, phase, p.xTongue);
  const yB = baseOf(yEff, phase, p.yTongue);

  /* 2. 出牌触发技能（增益/减益/封锁/清除） */
  playSkills(x, y, xEff, skills, logs);
  playSkills(y, x, yEff, skills, logs);

  /* 3. P07 复制对手本回合法术瞬发效果 */
  let xCopy = 0, yCopy = 0;
  if (xEff && xEff.passiveCode === 'P07' && yEff && yEff.type === 'magic') {
    xCopy = Math.max(yB.heal, yB.guard, yB.out);
    if (xCopy > 0) logs.push('复制对方法术效果，追加 ' + xCopy + ' 点');
  }
  if (yEff && yEff.passiveCode === 'P07' && xEff && xEff.type === 'magic') {
    yCopy = Math.max(xB.heal, xB.guard, xB.out);
  }

  /* 4. 数值修正 + 伤害 */
  const xout = modOut(xB.out, x.mods);
  const yout = modOut(yB.out, y.mods);
  const xguard = modGuard(xB.guard, x.mods);
  const yguard = modGuard(yB.guard, y.mods);

  const dmgToY = Math.max(0, xout + xCopy - yguard);
  const dmgToX = Math.max(0, yout + yCopy - xguard);

  /* 5. P10 反弹：防守方有护盾且承伤时，把伤害弹回给攻击一方
        xRef = X 方护盾弹出的伤害 → 落到 Y 头上；yRef 同理落到 X 头上 */
  let xRef = 0, yRef = 0;
  if (dmgToX > 0 && xguard > 0) xRef = reflectValue(x, skills);
  if (dmgToY > 0 && yguard > 0) yRef = reflectValue(y, skills);
  if (xRef > 0) logs.push('X 方护盾承伤，反弹 ' + xRef + ' 点');
  if (yRef > 0) logs.push('Y 方护盾承伤，反弹 ' + yRef + ' 点');

  /* 6. 治疗 + 扣血（注意反弹伤害加在对手身上，不是扣自己） */
  const healX = healOf(x, xB.heal, logs);
  const healY = healOf(y, yB.heal, logs);

  x.hp = Math.max(0, x.hp - dmgToX - yRef);
  y.hp = Math.max(0, y.hp - dmgToY - xRef);

  /* 7. P09 生效一次后移除；fresh 标记作废（下次调用起该效果才参与结算） */
  x.mods = x.mods.filter(m => !(m.code === 'P09' && m.used));
  y.mods = y.mods.filter(m => !(m.code === 'P09' && m.used));
  x.mods.forEach(m => { if (m.fresh) delete m.fresh; });
  y.mods.forEach(m => { if (m.fresh) delete m.fresh; });

  return {
    x: x, y: y,
    xDmg: dmgToY, yDmg: dmgToX,
    xHeal: healX, yHeal: healY,
    xReflect: xRef, yReflect: yRef,
    logs: logs
  };
}

/* ========== 回合末：衰减持续效果 ========== */
function endTurn(slot) {
  slot.mods = slot.mods
    .map(m => { m.turns = (m.turns || 1) - 1; return m; })
    .filter(m => m.turns > 0);
  slot.flags.magicLocked = false;   // 封锁只影响下一个半场
  return slot;
}

module.exports = {
  createSlot: createSlot,
  computeRound: computeRound,
  endTurn: endTurn,
  DEFAULT_MAX_HP: DEFAULT_MAX_HP,
  RULES: RULES
};
