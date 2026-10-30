// pages/index/index.js
// 山海异兽 · 战略博弈战斗
// 状态用普通对象维护，界面统一由 render* 函数重渲染（无框架依赖）

/* ===================== 基础数据 ===================== */

const ROLES = {
  A: { key: 'A', name: '甲', isPlayer: true },
  B: { key: 'B', name: '乙', isPlayer: false },
  C: { key: 'C', name: '丙', isPlayer: false }
};

const TYPE_LABEL = { attack: '攻', defense: '防', magic: '法', tongue: '通' };
const GROUP_LABEL = { attack: '进攻卡组', defense: '防御卡组', magic: '法术卡组', reserve: '备用卡组' };
const GROUP_TAG = { attack: '攻', defense: '防', magic: '法', reserve: '备' };
const GROUPS = ['attack', 'defense', 'magic', 'reserve'];

const RANKS = [
  { n: '青铜', min: 0 },
  { n: '白银', min: 100 },
  { n: '黄金', min: 200 },
  { n: '铂金', min: 300 },
  { n: '钻石', min: 400 },
  { n: '王者', min: 500 }
];
const WIN_GAIN = 30;
const LOSE_LOSS = 20;
const MAX_HP = 10;

/* 神兽图标（内联 SVG，转 data-uri 供 <image> 使用） */
const BEAST_SVG = {
  'b-wolf': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M34,62 L28,44 L40,52 L50,46 L48,58 L62,66 L68,80 L40,80 L30,80 Z" fill="#c9b98a"/><path d="M40,52 L50,46 L52,54 L42,58 Z" fill="#e8d9a8"/></svg>',
  'b-tiger': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M22,72 Q20,46 40,40 Q52,36 58,42 Q66,48 66,58 L64,72 Q50,78 40,78 Q28,78 22,72 Z" fill="#c9b98a"/><path d="M30,50 L36,44 L42,50 L38,56 Z" fill="#e8d9a8"/></svg>',
  'b-turtle': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M30,66 Q30,42 50,40 Q70,42 70,66 L68,74 Q50,80 32,74 Z" fill="#c9b98a"/><path d="M24,60 L34,60 L34,70 L24,66 Z M66,60 L76,60 L76,66 L66,70 Z M48,72 L52,84 L48,84 Z" fill="#e8d9a8"/></svg>',
  'b-crane': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M48,20 L52,20 Q56,22 54,26 L52,30 L50,36 L52,42 Q54,48 52,52 L50,62 Q50,66 46,68 L42,66 Q42,60 44,56 L46,50 L44,44 Q42,38 44,34 L46,28 Z" fill="#c9b98a"/><path d="M30,66 Q40,62 44,64 L40,74 Q36,72 30,70 Z M56,64 Q64,62 66,66 L62,74 Q58,72 56,68 Z M42,72 L46,84 L40,84 Z M50,72 L54,84 L48,84 Z" fill="#e8d9a8"/></svg>',
  'b-rhino': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M20,72 Q18,50 34,46 L40,42 Q44,40 46,42 L48,46 Q58,46 64,52 L70,58 Q74,62 72,68 L68,74 Q50,80 34,76 Q24,76 20,72 Z" fill="#c9b98a"/><path d="M40,42 L44,26 L46,42 Z" fill="#e8d9a8"/></svg>',
  'b-boar': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M22,70 Q20,46 40,42 Q54,40 60,46 Q68,52 66,62 L64,70 Q50,76 38,76 Q26,76 22,70 Z" fill="#c9b98a"/><path d="M30,52 L34,58 L40,54 L38,60 Z M58,52 L54,58 L48,54 L50,60 Z" fill="#e8d9a8"/></svg>',
  'b-qilin': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M30,70 Q26,46 44,40 L50,36 Q52,32 56,36 L54,40 Q62,42 66,50 L70,58 Q74,64 72,70 L64,76 Q48,80 36,76 Q28,76 30,70 Z" fill="#c9b98a"/><path d="M50,36 L56,20 L58,36 Z M54,40 L62,30 L60,42 Z" fill="#e8d9a8"/></svg>',
  'b-trophy': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48"><path d="M12 6h24v9a12 12 0 0 1-24 0z" fill="#f6e6b0"/><rect x="20" y="27" width="8" height="7" fill="#b89b58"/><rect x="15" y="33" width="18" height="3" fill="#b89b58"/><rect x="22" y="36" width="4" height="6" fill="#b89b58"/></svg>'
};

const BEAST_URI = {};
Object.keys(BEAST_SVG).forEach(function (k) {
  BEAST_URI[k] = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(BEAST_SVG[k]);
});

/* 卡牌库 */
const CARD_DB = [
  { id: 'atk1', name: '玄甲犀', type: 'attack', out: 5, guard: 0, heal: 0, beast: 'b-rhino' },
  { id: 'atk2', name: '裂山貔', type: 'attack', out: 4, guard: 1, heal: 0, beast: 'b-wolf' },
  { id: 'atk3', name: '噬魂狰', type: 'attack', out: 6, guard: 0, heal: 0, beast: 'b-tiger' },
  { id: 'atk4', name: '赤焰猁', type: 'attack', out: 6, guard: 1, heal: 0, beast: 'b-tiger' },
  { id: 'atk5', name: '玄岩犼', type: 'attack', out: 5, guard: 1, heal: 0, beast: 'b-qilin' },

  { id: 'def1', name: '镇海龟', type: 'defense', out: 1, guard: 4, heal: 0, beast: 'b-turtle' },
  { id: 'def2', name: '玄武', type: 'defense', out: 1, guard: 5, heal: 0, beast: 'b-turtle' },
  { id: 'def3', name: '金甲兽', type: 'defense', out: 3, guard: 3, heal: 0, beast: 'b-rhino' },
  { id: 'def4', name: '灵犀', type: 'defense', out: 1, guard: 4, heal: 2, beast: 'b-rhino' },
  { id: 'def5', name: '玄壁貔', type: 'defense', out: 2, guard: 5, heal: 0, beast: 'b-wolf' },

  { id: 'mag1', name: '回春鹤', type: 'magic', out: 0, guard: 0, heal: 4, beast: 'b-crane' },
  { id: 'mag2', name: '噬灵魈', type: 'magic', out: 5, guard: 0, heal: 0, beast: 'b-wolf' },
  { id: 'mag3', name: '天雷彘', type: 'magic', out: 7, guard: 0, heal: 0, beast: 'b-boar' },
  { id: 'mag4', name: '玄冰貉', type: 'magic', out: 4, guard: 0, heal: 2, beast: 'b-crane' },
  { id: 'mag5', name: '炎符鹤', type: 'magic', out: 5, guard: 0, heal: 1, beast: 'b-crane' },

  { id: 'tongA1', name: '炎雷獠', type: 'tongue', kind: 'atk', out: 5, guard: 0, heal: 0, mana: 4, beast: 'b-qilin', sub: '攻·法' },
  { id: 'tongA2', name: '雷霆貔', type: 'tongue', kind: 'atk', out: 4, guard: 0, heal: 0, mana: 6, beast: 'b-wolf', sub: '攻·法' },
  { id: 'tongD1', name: '玄法龟', type: 'tongue', kind: 'def', out: 1, guard: 4, heal: 0, mana: 4, beast: 'b-turtle', sub: '防·法' },
  { id: 'tongD2', name: '灵法犀', type: 'tongue', kind: 'def', out: 1, guard: 5, heal: 2, mana: 3, beast: 'b-rhino', sub: '防·法' }
];

/* ===================== 工具函数 ===================== */

function byId(id) {
  for (let i = 0; i < CARD_DB.length; i++) { if (CARD_DB[i].id === id) return CARD_DB[i]; }
  return null;
}

function clone(o) { return JSON.parse(JSON.stringify(o)); }

function defaultDeck() {
  return {
    attack: [byId('atk1'), byId('atk2'), byId('atk3')],
    defense: [byId('def1'), byId('def2'), byId('def3')],
    magic: [byId('mag1'), byId('mag2'), byId('mag3')],
    reserve: [byId('tongA1'), byId('mag4'), byId('def4')]
  };
}

function canPlace(card, group) {
  if (!card) return true;
  if (group === 'attack') return card.type === 'attack' || (card.type === 'tongue' && card.kind === 'atk');
  if (group === 'defense') return card.type === 'defense' || (card.type === 'tongue' && card.kind === 'def');
  if (group === 'magic') return card.type === 'magic';
  return true;
}

function phaseName(phase) { return phase === 'top' ? '争锋' : '运玄'; }

function availablePool(deck, phase) {
  return phase === 'top' ? deck.attack.concat(deck.defense) : deck.magic;
}

function removeFromDeck(deck, cardId) {
  ['attack', 'defense', 'magic'].forEach(function (g) {
    const i = deck[g].findIndex(function (c) { return c.id === cardId; });
    if (i >= 0) deck[g].splice(i, 1);
  });
}

function skillName(card) {
  if (!card) return '不出牌';
  if (card.type === 'tongue') return card.name + '·' + (card.kind === 'atk' ? '攻' : '防') + '·法';
  return card.name + '·' + TYPE_LABEL[card.type];
}

function rankOf(score) {
  let r = RANKS[0];
  RANKS.forEach(function (k) { if (score >= k.min) r = k; });
  return r;
}

function playerSlotOf(f) {
  if (ROLES[f.x].isPlayer) return 'x';
  if (ROLES[f.y].isPlayer) return 'y';
  return null;
}

/* 卡牌 -> 视图数据 */
function cardVM(card, selected) {
  if (!card) return null;
  const stat = [];
  if (card.type === 'attack') {
    stat.push({ k: '攻', v: card.out });
  } else if (card.type === 'defense') {
    stat.push({ k: '防', v: card.guard });
    stat.push({ k: '攻', v: card.out });
  } else if (card.type === 'magic') {
    stat.push(card.out ? { k: '法', v: card.out } : { k: '疗', v: card.heal });
  } else {
    stat.push(card.kind === 'atk' ? { k: '攻', v: card.out } : { k: '防', v: card.guard });
    stat.push({ k: '法', v: card.mana });
  }
  return {
    id: card.id,
    name: card.name,
    type: card.type,
    typeLabel: TYPE_LABEL[card.type],
    sub: card.sub || '',
    stat: stat,
    icon: BEAST_URI[card.beast] || '',
    selected: !!selected
  };
}

function createState() {
  return {
    view: 'picka',
    stage: 'dice',
    deck: defaultDeck(),
    pickSel: null,
    swapSel: null,
    pendingMatch: 0,
    order: [],
    matchIndex: 0,
    totals: { A: 0, B: 0, C: 0 },
    firstX: null, firstY: null, firstZ: null,
    fight: null,
    fightHistory: [],
    score: 100,
    dice: null,
    diceSpinning: false,
    isWatch: false,
    msg: '',
    log: [],
    reveal: null
  };
}

/* ===================== 页面 ===================== */

Page({
  data: {
    view: 'picka',
    stage: 'dice',
    storage: [],
    groups: { attack: [], defense: [], magic: [], reserve: [] },
    counts: { attack: '0/3', defense: '0/3', magic: '0/3', reserve: '0/3' },
    pickaTime: 120,
    swapTitle: '第 1 场 · 组内交换',
    swapTime: 20,
    swapGroups: { attack: [], defense: [], magic: [], reserve: [] },
    arenaTitle: '对局准备',
    arenaMeta: '三人掷骰，点数最高两人进入首轮对战',
    diceSpinning: false,
    dice: null,
    opp: null,
    me: null,
    spectator: null,
    roundTag: '回合 1 / 6 · 【争锋】',
    fightTime: 30,
    table: { has: false },
    hand: [],
    showHand: false,
    playTitle: '请出牌',
    playHint: '点选一张卡牌，双方同时亮牌结算',
    msg: '',
    log: [],
    reveal: null,
    result: null
  },

  onLoad: function () {
    const saved = wx.getStorageSync('shanhai_score');
    this.st = createState();
    if (saved !== '' && saved !== null && !isNaN(Number(saved))) this.st.score = Number(saved);
    this.t = { picka: null, swap: null, fight: null, ai: null, reveal: null, flow: null, dice: null };
    this.resetToPickA();
  },

  onUnload: function () { this.clearTimers(); },
  onHide: function () { this.clearTimers(); },

  clearTimers: function () {
    if (!this.t) return;
    Object.keys(this.t).forEach((k) => {
      if (this.t[k]) { clearInterval(this.t[k]); clearTimeout(this.t[k]); this.t[k] = null; }
    });
  },

  toast: function (msg) {
    wx.showToast({ title: msg, icon: 'none', duration: 1500 });
  },

  /* ---------- 通用查询 ---------- */
  isInDeck: function (id) {
    const d = this.st.deck;
    return GROUPS.some((g) => d[g].some((c) => c && c.id === id));
  },
  tongCount: function () {
    const d = this.st.deck;
    return GROUPS.reduce((s, g) => s + d[g].filter((c) => c && c.type === 'tongue').length, 0);
  },
  fightDeck: function (roleKey) {
    if (ROLES[roleKey].isPlayer) {
      const d = this.st.deck;
      return clone({ attack: d.attack, defense: d.defense, magic: d.magic });
    }
    const d = defaultDeck();
    return clone({ attack: d.attack, defense: d.defense, magic: d.magic });
  },

  /* ===================== 界面A：仓库配卡 ===================== */

  resetToPickA: function () {
    this.clearTimers();
    this.st.deck = defaultDeck();
    this.st.pickSel = null;
    this.renderPickA();
    this.startPickACountdown();
  },

  renderPickA: function () {
    const st = this.st;
    const pool = CARD_DB.filter((c) => !this.isInDeck(c.id));
    const storage = pool.map((c) => cardVM(c, st.pickSel === c.id));
    const groups = {};
    const counts = {};
    GROUPS.forEach((g) => {
      const arr = [];
      for (let i = 0; i < 3; i++) {
        const c = st.deck[g][i];
        arr.push(c ? cardVM(c, false) : null);
      }
      groups[g] = arr;
      counts[g] = st.deck[g].length + '/3';
    });
    this.setData({ view: 'picka', storage: storage, groups: groups, counts: counts });
  },

  startPickACountdown: function () {
    clearInterval(this.t.picka);
    let t = 120;
    this.setData({ pickaTime: t });
    this.t.picka = setInterval(() => {
      t -= 1;
      this.setData({ pickaTime: Math.max(0, t) });
      if (t <= 0) {
        clearInterval(this.t.picka);
        this.confirmPickA();
      }
    }, 1000);
  },

  onStorageTap: function (e) {
    const id = e.currentTarget.dataset.id;
    this.st.pickSel = this.st.pickSel === id ? null : id;
    this.renderPickA();
  },

  onSlotTap: function (e) {
    const g = e.currentTarget.dataset.group;
    const idx = Number(e.currentTarget.dataset.index);
    const st = this.st;
    if (st.deck[g][idx]) {
      st.deck[g].splice(idx, 1);
      st.pickSel = null;
      this.renderPickA();
      return;
    }
    if (!st.pickSel) { this.toast('请先点选一张仓库卡'); return; }
    const card = byId(st.pickSel);
    if (!canPlace(card, g)) { this.toast('该卡不能放入' + GROUP_LABEL[g].replace('卡组', '')); return; }
    if (card.type === 'tongue' && this.tongCount() >= 2) { this.toast('通卡最多携带 2 张'); return; }
    st.deck[g].push(card);
    st.pickSel = null;
    this.renderPickA();
  },

  confirmPickA: function () {
    clearInterval(this.t.picka);
    const st = this.st;
    st.order = [];
    st.matchIndex = 0;
    st.fightHistory = [];
    st.totals = { A: 0, B: 0, C: 0 };
    st.stage = 'dice';
    st.dice = null;
    st.diceSpinning = false;
    this.setData({
      view: 'battle',
      stage: 'dice',
      arenaTitle: '对局准备',
      arenaMeta: '三人掷骰，点数最高两人进入首轮对战，第三人观战',
      dice: null,
      diceSpinning: false,
      log: [],
      msg: '点击掷骰，决定首轮对手'
    });
  },

  resetDefault: function () {
    this.st.deck = defaultDeck();
    this.st.pickSel = null;
    this.renderPickA();
    this.toast('已重置为默认配置');
  },

  /* ===================== 掷骰 ===================== */

  rollDice: function () {
    const st = this.st;
    if (st.dice) return;
    const dice = {
      A: Math.floor(Math.random() * 6) + 1,
      B: Math.floor(Math.random() * 6) + 1,
      C: Math.floor(Math.random() * 6) + 1
    };
    st.diceSpinning = true;
    this.setData({ diceSpinning: true });
    clearTimeout(this.t.dice);
    this.t.dice = setTimeout(() => {
      const list = ['A', 'B', 'C'].sort((a, b) => dice[b] - dice[a]);
      st.firstX = list[0]; st.firstY = list[1]; st.firstZ = list[2];
      st.order = [[list[0], list[1]]];
      st.dice = dice;
      st.diceSpinning = false;
      this.setData({
        dice: dice,
        diceSpinning: false,
        arenaMeta: ROLES[list[0]].name + ' 与 ' + ROLES[list[1]].name + ' 首轮对战，' + ROLES[list[2]].name + ' 观战'
      });
      clearTimeout(this.t.flow);
      this.t.flow = setTimeout(() => this.goMatch(0), 5000);
    }, 900);
  },

  ensureMatchOrder: function (mi) {
    const st = this.st;
    if (st.order[mi]) return;
    if (mi === 1) {
      const f0 = st.fightHistory[0];
      const high = f0.xhp >= f0.yhp ? f0.x : f0.y;
      st.order[1] = [high, st.firstZ];
    } else if (mi === 2) {
      const f0 = st.fightHistory[0];
      const s1Low = f0.xhp >= f0.yhp ? st.firstY : st.firstX;
      st.order[2] = [st.firstZ, s1Low];
    }
  },

  goMatch: function (mi) {
    this.ensureMatchOrder(mi);
    const pair = this.st.order[mi];
    const hasPlayer = pair.some((k) => ROLES[k].isPlayer);
    if (hasPlayer) this.openSwapB(mi);
    else this.startWatch(mi);
  },

  /* ===================== 界面B：组内交换 ===================== */

  openSwapB: function (mi) {
    const st = this.st;
    st.pendingMatch = mi;
    st.swapSel = null;
    this.renderSwapB();
    this.setData({
      view: 'pickb',
      swapTitle: '第 ' + (mi + 1) + ' 场 · 组内交换'
    });
    clearInterval(this.t.swap);
    let t = 20;
    this.setData({ swapTime: t });
    this.t.swap = setInterval(() => {
      t -= 1;
      this.setData({ swapTime: Math.max(0, t) });
      if (t <= 0) {
        clearInterval(this.t.swap);
        this.beginMatch(st.order[st.pendingMatch]);
      }
    }, 1000);
  },

  renderSwapB: function () {
    const st = this.st;
    const swapGroups = {};
    GROUPS.forEach((g) => {
      const arr = [];
      for (let i = 0; i < 3; i++) {
        const c = st.deck[g][i];
        const sel = st.swapSel && st.swapSel.group === g && st.swapSel.index === i;
        arr.push(c ? cardVM(c, sel) : null);
      }
      swapGroups[g] = arr;
    });
    this.setData({ swapGroups: swapGroups });
  },

  onSwapTap: function (e) {
    const st = this.st;
    const g = e.currentTarget.dataset.group;
    const idx = Number(e.currentTarget.dataset.index);
    const key = { group: g, index: idx };
    if (!st.swapSel) {
      if (st.deck[g][idx]) { st.swapSel = key; this.renderSwapB(); }
      return;
    }
    const a = st.swapSel, b = key;
    if (a.group === b.group && a.index === b.index) { st.swapSel = null; this.renderSwapB(); return; }
    this.doSwap(a, b);
  },

  doSwap: function (a, b) {
    const st = this.st;
    const ca = st.deck[a.group][a.index];
    const cb = st.deck[b.group][b.index];
    if (ca && !canPlace(ca, b.group)) { this.toast('不能放入目标组'); st.swapSel = null; this.renderSwapB(); return; }
    if (cb && !canPlace(cb, a.group)) { this.toast('目标卡不能放入此组'); st.swapSel = null; this.renderSwapB(); return; }
    st.deck[a.group][a.index] = cb;
    st.deck[b.group][b.index] = ca;
    st.swapSel = null;
    this.renderSwapB();
  },

  startBattleFromSwap: function () {
    clearInterval(this.t.swap);
    this.beginMatch(this.st.order[this.st.pendingMatch]);
  },

  /* ===================== 战斗 ===================== */

  beginMatch: function (pair) {
    const st = this.st;
    st.fight = {
      x: pair[0], y: pair[1], xhp: MAX_HP, yhp: MAX_HP,
      xDeck: this.fightDeck(pair[0]), yDeck: this.fightDeck(pair[1]),
      round: 1, phase: 'top',
      xSel: null, ySel: null, xLocked: false, yLocked: false,
      xTongue: null, yTongue: null,
      history: [], over: false
    };
    st.stage = 'fight';
    st.log = [];
    st.isWatch = !pair.some((k) => ROLES[k].isPlayer);
    st.msg = st.isWatch ? 'AI 对战自动进行，你可在场边观看' : '请出牌，双方同时亮牌结算';
    this.setData({
      view: 'battle',
      stage: 'fight',
      arenaTitle: '对局 · 第 ' + (st.matchIndex + 1) + ' 场',
      arenaMeta: st.isWatch
        ? '观战中：' + ROLES[pair[0]].name + ' vs ' + ROLES[pair[1]].name
        : ROLES[pair[0]].name + ' vs ' + ROLES[pair[1]].name,
      log: [],
      msg: st.msg,
      reveal: null
    });
    this.renderFight();
    this.startPhase();
  },

  startWatch: function (mi) {
    this.st.pendingMatch = mi;
    this.beginMatch(this.st.order[mi]);
  },

  startPhase: function () {
    const st = this.st, f = st.fight;
    if (!f || f.over) return;
    f.xSel = null; f.ySel = null; f.xLocked = false; f.yLocked = false;
    const ps = playerSlotOf(f);
    if (ps) {
      const deck = ps === 'x' ? f.xDeck : f.yDeck;
      if (availablePool(deck, f.phase).length === 0 || (f.phase === 'bottom' && f[ps + 'Tongue'])) {
        f[ps + 'Locked'] = true;
      }
    }
    this.renderFight();
    this.renderPlayZone();
    clearInterval(this.t.fight);
    let t = 30;
    this.setData({ fightTime: t });
    this.t.fight = setInterval(() => {
      t -= 1;
      this.setData({ fightTime: Math.max(0, t) });
      if (t <= 0) {
        clearInterval(this.t.fight);
        ['x', 'y'].forEach((slot) => {
          if (!f[slot + 'Locked']) {
            const deck = slot === 'x' ? f.xDeck : f.yDeck;
            const pool = availablePool(deck, f.phase);
            if (pool.length === 0) f[slot + 'Sel'] = null;
            else f[slot + 'Sel'] = pool[Math.floor(Math.random() * pool.length)];
            f[slot + 'Locked'] = true;
          }
        });
        this.maybeResolve();
      }
    }, 1000);
    this.scheduleAI();
  },

  scheduleAI: function () {
    clearTimeout(this.t.ai);
    this.t.ai = setTimeout(() => {
      const f = this.st.fight;
      if (!f || f.over) return;
      ['x', 'y'].forEach((slot) => {
        const key = f[slot];
        if (ROLES[key].isPlayer || f[slot + 'Locked']) return;
        if (f.phase === 'bottom' && f[slot + 'Tongue']) { f[slot + 'Locked'] = true; return; }
        const deck = slot === 'x' ? f.xDeck : f.yDeck;
        const pool = availablePool(deck, f.phase);
        if (pool.length === 0) f[slot + 'Locked'] = true;
        else { f[slot + 'Sel'] = pool[Math.floor(Math.random() * pool.length)]; f[slot + 'Locked'] = true; }
      });
      this.maybeResolve();
    }, 900);
  },

  maybeResolve: function () {
    const f = this.st.fight;
    if (!f || f.over) return;
    if (!(f.xLocked && f.yLocked)) return;
    clearInterval(this.t.fight);
    clearTimeout(this.t.ai);
    this.showReveal();
  },

  showReveal: function () {
    const f = this.st.fight;
    const xc = f.xSel, yc = f.ySel;
    const xTongue = (f.phase === 'bottom' && f.xTongue && !xc) ? f.xTongue : null;
    const yTongue = (f.phase === 'bottom' && f.yTongue && !yc) ? f.yTongue : null;
    f.history.forEach((h) => { h.current = false; });
    f.history.push({
      phase: f.phase,
      xCard: xc ? clone(xc) : null, yCard: yc ? clone(yc) : null,
      current: true,
      xTongue: xTongue ? clone(xTongue) : null, yTongue: yTongue ? clone(yTongue) : null
    });
    const xShow = xc || xTongue, yShow = yc || yTongue;
    this.setData({
      reveal: {
        phaseName: phaseName(f.phase),
        xCard: xShow ? cardVM(xShow) : null,
        yCard: yShow ? cardVM(yShow) : null,
        xName: ROLES[f.x].name,
        yName: ROLES[f.y].name
      },
      msg: '亮牌！' + ROLES[f.x].name + '【' + skillName(xShow) + '】 vs ' + ROLES[f.y].name + '【' + skillName(yShow) + '】'
    });
    clearTimeout(this.t.reveal);
    this.t.reveal = setTimeout(() => this.closeReveal(), 2000);
  },

  closeReveal: function () {
    this.setData({ reveal: null });
    this.renderTable();
    clearTimeout(this.t.reveal);
    this.t.reveal = setTimeout(() => this.resolveRound(), 600);
  },

  resolveRound: function () {
    const st = this.st, f = st.fight;
    if (!f || f.over) return;
    const xc = f.xSel, yc = f.ySel;
    this.setData({ reveal: null });

    f.xhp += (xc ? xc.heal : 0);
    f.yhp += (yc ? yc.heal : 0);

    let xOut = 0, yOut = 0;
    if (f.phase === 'top') {
      xOut = xc ? xc.out : 0;
      yOut = yc ? yc.out : 0;
    } else {
      xOut = xc ? xc.out : (f.xTongue ? f.xTongue.mana : 0);
      yOut = yc ? yc.out : (f.yTongue ? f.yTongue.mana : 0);
    }
    const xGuard = (xc && (xc.type === 'defense' || (xc.type === 'tongue' && xc.kind === 'def'))) ? xc.guard : 0;
    const yGuard = (yc && (yc.type === 'defense' || (yc.type === 'tongue' && yc.kind === 'def'))) ? yc.guard : 0;
    const dmgToY = Math.max(0, xOut - yGuard);
    const dmgToX = Math.max(0, yOut - xGuard);
    f.yhp = Math.max(0, f.yhp - dmgToY);
    f.xhp = Math.max(0, f.xhp - dmgToX);

    if (f.phase === 'top') {
      f.xTongue = (xc && xc.type === 'tongue') ? xc : null;
      f.yTongue = (yc && yc.type === 'tongue') ? yc : null;
    } else {
      f.xTongue = null; f.yTongue = null;
    }

    if (xc) removeFromDeck(f.xDeck, xc.id);
    if (yc) removeFromDeck(f.yDeck, yc.id);

    const note = '回合' + f.round + '·' + phaseName(f.phase) + '：' + ROLES[f.x].name + '造成 ' + dmgToY + ' · ' + ROLES[f.y].name + '造成 ' + dmgToX;
    st.log.push(note);
    st.msg = ROLES[f.x].name + '【' + (xc ? xc.name : '不出牌') + '】 vs ' + ROLES[f.y].name + '【' + (yc ? yc.name : '不出牌') + '】 · 伤害 ' + dmgToY + ' / ' + dmgToX;
    this.setData({ log: st.log.slice(-30), msg: st.msg });

    this.renderFight();
    f.xSel = null; f.ySel = null;

    clearTimeout(this.t.flow);
    if (f.xhp <= 0 || f.yhp <= 0) {
      this.t.flow = setTimeout(() => this.endMatch(), 900);
    } else if (f.phase === 'top') {
      f.phase = 'bottom';
      this.t.flow = setTimeout(() => this.startPhase(), 900);
    } else {
      f.phase = 'top';
      if (f.round >= 6) this.t.flow = setTimeout(() => this.endMatch(), 900);
      else { f.round += 1; this.t.flow = setTimeout(() => this.startPhase(), 900); }
    }
  },

  endMatch: function () {
    const st = this.st, f = st.fight;
    if (!f || f.over) return;
    f.over = true;
    st.totals[f.x] += f.xhp;
    st.totals[f.y] += f.yhp;
    st.fightHistory.push({ x: f.x, y: f.y, xhp: f.xhp, yhp: f.yhp });
    const winner = f.xhp >= f.yhp ? f.x : f.y;
    this.setData({ msg: '本场结束：' + ROLES[winner].name + ' 血量更高' });
    const next = st.matchIndex + 1;
    st.matchIndex = next;
    clearTimeout(this.t.flow);
    if (next < 3) {
      this.setData({ arenaMeta: '进入第 ' + (next + 1) + ' 场' });
      this.t.flow = setTimeout(() => this.goMatch(next), 1200);
    } else {
      this.t.flow = setTimeout(() => this.showResult(), 1200);
    }
  },

  skipSpectate: function () {
    const f = this.st.fight;
    if (!f || f.over) return;
    this.toast('已跳过观战');
    this.endMatch();
  },

  /* ---------- 战斗渲染 ---------- */

  renderFight: function () {
    const st = this.st, f = st.fight;
    if (!f) return;
    const playerKey = ROLES[f.x].isPlayer ? f.x : f.y;
    const oppKey = playerKey === f.x ? f.y : f.x;
    const oppDeck = oppKey === f.x ? f.xDeck : f.yDeck;
    const playerDeck = playerKey === f.x ? f.xDeck : f.yDeck;
    const opp = {
      key: oppKey, name: ROLES[oppKey].name,
      hp: oppKey === f.x ? f.xhp : f.yhp,
      bar: Math.min(100, (oppKey === f.x ? f.xhp : f.yhp) * 10),
      deckText: '攻' + oppDeck.attack.length + ' 防' + oppDeck.defense.length + ' 法' + oppDeck.magic.length
    };
    const me = {
      key: playerKey, name: ROLES[playerKey].name + (ROLES[playerKey].isPlayer ? '（我）' : ''),
      hp: playerKey === f.x ? f.xhp : f.yhp,
      bar: Math.min(100, (playerKey === f.x ? f.xhp : f.yhp) * 10),
      deckText: '攻' + playerDeck.attack.length + ' 防' + playerDeck.defense.length + ' 法' + playerDeck.magic.length
    };
    let spectator = null;
    const pair = st.order[st.matchIndex] || [f.x, f.y];
    const sKey = ['A', 'B', 'C'].filter((k) => pair.indexOf(k) < 0)[0];
    if (sKey) {
      spectator = { key: sKey, name: ROLES[sKey].name, total: st.totals[sKey] || 0 };
    }
    this.setData({
      opp: opp,
      me: me,
      spectator: spectator,
      roundTag: '回合 ' + f.round + ' / 6 · 【' + phaseName(f.phase) + '】'
    });
    this.renderPlayZone();
    this.renderTable();
  },

  renderPlayZone: function () {
    const st = this.st, f = st.fight;
    if (!f) return;
    const ps = playerSlotOf(f);
    if (!ps) {
      this.setData({ showHand: false, hand: [], playTitle: '观战中', playHint: 'AI 对战自动进行，你可在场边观看' });
      return;
    }
    const deck = ps === 'x' ? f.xDeck : f.yDeck;
    const pool = availablePool(deck, f.phase);
    const title = '待选手牌 · 【' + phaseName(f.phase) + '】' + (f.phase === 'top' ? '攻防通卡牌组' : '法术牌组');
    const tongue = f[ps + 'Tongue'];
    if (f.phase === 'bottom' && tongue) {
      f[ps + 'Locked'] = true;
      this.setData({ showHand: false, hand: [], playTitle: title, playHint: '通卡【' + tongue.name + '】的法术能力生效，本半场免出牌，法术直接触发' });
      return;
    }
    if (pool.length === 0) {
      f[ps + 'Locked'] = true;
      this.setData({ showHand: false, hand: [], playTitle: title, playHint: '本半场无牌可出，自动跳过' });
      return;
    }
    const selId = f[ps + 'Sel'] ? f[ps + 'Sel'].id : null;
    const hand = pool.map((c) => cardVM(c, c.id === selId));
    this.setData({
      showHand: true,
      hand: hand,
      playTitle: title,
      playHint: '点选一张卡牌，双方同时亮牌结算'
    });
  },

  onHandTap: function (e) {
    const f = this.st.fight;
    if (!f || f.over) return;
    const ps = playerSlotOf(f);
    if (!ps || f[ps + 'Locked']) return;
    const id = e.currentTarget.dataset.id;
    const deck = ps === 'x' ? f.xDeck : f.yDeck;
    const pool = availablePool(deck, f.phase);
    const card = pool.filter((c) => c.id === id)[0];
    if (!card) return;
    if (f[ps + 'Sel'] && f[ps + 'Sel'].id === card.id) {
      f[ps + 'Sel'] = null;
      this.setData({ msg: '已取消选择，请点选一张卡牌' });
    } else {
      f[ps + 'Sel'] = card;
      this.setData({ msg: '已选 ' + card.name + '，点「确认出牌」结算' });
    }
    this.renderPlayZone();
  },

  confirmPlay: function () {
    const f = this.st.fight;
    if (!f || f.over) return;
    const ps = playerSlotOf(f);
    if (!ps || f[ps + 'Locked']) return;
    if (!f[ps + 'Sel']) { this.toast('请先选择一张牌'); return; }
    f[ps + 'Locked'] = true;
    this.maybeResolve();
  },

  renderTable: function () {
    const f = this.st.fight;
    if (!f) return;
    const playerKey = ROLES[f.x].isPlayer ? f.x : f.y;
    const oppKey = playerKey === f.x ? f.y : f.x;
    if (!f.history.length) { this.setData({ table: { has: false } }); return; }
    const cellOf = (key, h) => {
      const c = key === f.x ? h.xCard : h.yCard;
      const t = key === f.x ? h.xTongue : h.yTongue;
      if (t && !c) return { text: '通·法', card: null, current: !!h.current };
      if (c) return { text: '', card: cardVM(c), current: !!h.current };
      return { text: '不出', card: null, current: !!h.current };
    };
    const oppRow = f.history.map((h) => cellOf(oppKey, h));
    const meRow = f.history.map((h) => cellOf(playerKey, h));
    this.setData({
      table: {
        has: true,
        oppName: ROLES[oppKey].name,
        meName: ROLES[playerKey].name,
        oppRow: oppRow,
        meRow: meRow
      }
    });
  },

  /* ===================== 结算 ===================== */

  showResult: function () {
    const st = this.st;
    const entries = ['A', 'B', 'C'].map((k) => ({ key: k, total: st.totals[k] || 0 }))
      .sort((a, b) => b.total - a.total);
    const champ = entries[0].key;
    const ranks = entries.map((e) => ({
      key: e.key,
      name: ROLES[e.key].name,
      isMe: ROLES[e.key].isPlayer,
      total: e.total
    }));
    const isWin = champ === 'A';
    const prev = st.score;
    const nextScore = isWin ? prev + WIN_GAIN : Math.max(0, prev - LOSE_LOSS);
    st.score = nextScore;
    wx.setStorageSync('shanhai_score', nextScore);
    const pr = rankOf(prev), nr = rankOf(nextScore);
    const pi = RANKS.map((r) => r.n).indexOf(pr.n);
    const ni = RANKS.map((r) => r.n).indexOf(nr.n);
    const delta = ni > pi ? 'up' : (ni < pi ? 'down' : 'same');
    const changeText = isWin
      ? (delta === 'up' ? '积分达标，段位晋升！' : '积分增加')
      : (delta === 'down' ? '积分不足门槛，段位下降' : '积分扣除');
    this.setData({
      view: 'result',
      result: {
        title: champ === 'A' ? '胜利！冠军是你' : '本局冠军：' + ROLES[champ].name,
        sub: '三人累计血量排行，最高者获胜',
        ranks: ranks,
        resultText: isWin ? '胜利 +' + WIN_GAIN : '失败 -' + LOSE_LOSS,
        scoreFrom: prev,
        scoreTo: nextScore,
        rankFrom: pr.n,
        rankTo: nr.n,
        changeText: changeText,
        trophy: BEAST_URI['b-trophy']
      }
    });
  },

  backHome: function () {
    const st = this.st;
    st.totals = { A: 0, B: 0, C: 0 };
    st.order = [];
    st.matchIndex = 0;
    st.pendingMatch = null;
    st.fightHistory = [];
    st.fight = null;
    st.swapSel = null;
    st.dice = null;
    st.isWatch = false;
    st.msg = '';
    st.log = [];
    this.setData({ reveal: null, log: [], msg: '', result: null });
    this.resetToPickA();
    this.toast('已返回首页');
  }
});
