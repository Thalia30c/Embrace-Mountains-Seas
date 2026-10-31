/* ============ 山海异兽·战略博弈战斗 · 逻辑 ============ */
const api = require('../../utils/api');

const ROLES = {
  A: { key: 'A', name: '甲', isPlayer: true },
  B: { key: 'B', name: '乙', isPlayer: false },
  C: { key: 'C', name: '丙', isPlayer: false }
};
const TYPE_LABEL = { attack: '攻', defense: '防', magic: '法', tongue: '通' };

/* 兜底卡组：接口不可用（后端未启动/未登录）时使用，保证功能仍可跑通 */
const FALLBACK_CARDS = [
  { id: 'atk1', name: '玄甲犀', type: 'attack', out: 5, guard: 0, heal: 0 },
  { id: 'atk2', name: '裂山貔', type: 'attack', out: 4, guard: 1, heal: 0 },
  { id: 'atk3', name: '噬魂狰', type: 'attack', out: 6, guard: 0, heal: 0 },
  { id: 'atk4', name: '赤焰猁', type: 'attack', out: 6, guard: 1, heal: 0 },
  { id: 'atk5', name: '玄岩犼', type: 'attack', out: 5, guard: 1, heal: 0 },
  { id: 'def1', name: '镇海龟', type: 'defense', out: 1, guard: 4, heal: 0 },
  { id: 'def2', name: '玄武', type: 'defense', out: 1, guard: 5, heal: 0 },
  { id: 'def3', name: '金甲兽', type: 'defense', out: 3, guard: 3, heal: 0 },
  { id: 'def4', name: '灵犀', type: 'defense', out: 1, guard: 4, heal: 2 },
  { id: 'def5', name: '玄壁貔', type: 'defense', out: 2, guard: 5, heal: 0 },
  { id: 'mag1', name: '回春鹤', type: 'magic', out: 0, guard: 0, heal: 4 },
  { id: 'mag2', name: '噬灵魈', type: 'magic', out: 5, guard: 0, heal: 0 },
  { id: 'mag3', name: '天雷彘', type: 'magic', out: 7, guard: 0, heal: 0 },
  { id: 'mag4', name: '玄冰貉', type: 'magic', out: 4, guard: 0, heal: 2 },
  { id: 'mag5', name: '炎符鹤', type: 'magic', out: 5, guard: 0, heal: 1 },
  { id: 'tongA1', name: '炎雷獠', type: 'tongue', kind: 'atk', out: 5, guard: 0, heal: 0, mana: 4, sub: '攻·法' },
  { id: 'tongA2', name: '雷霆貔', type: 'tongue', kind: 'atk', out: 4, guard: 0, heal: 0, mana: 6, sub: '攻·法' },
  { id: 'tongD1', name: '玄法龟', type: 'tongue', kind: 'def', out: 1, guard: 4, heal: 0, mana: 4, sub: '防·法' },
  { id: 'tongD2', name: '灵法犀', type: 'tongue', kind: 'def', out: 1, guard: 5, heal: 2, mana: 3, sub: '防·法' }
];

/* 实际使用的卡库：启动时用数据库卡牌覆盖，失败则保持兜底卡 */
let CARD_DB = FALLBACK_CARDS.slice();
let cardFromDb = false;

/* 段位表：与数据库 game_rank 表一致（rank_id → need_total_score） */
const RANKS = [
  { id: 1, n: '山稚三段', min: 0 }, { id: 2, n: '山稚二段', min: 100 }, { id: 3, n: '山稚一段', min: 200 },
  { id: 4, n: '山精三段', min: 350 }, { id: 5, n: '山精二段', min: 500 }, { id: 6, n: '山精一段', min: 650 },
  { id: 7, n: '山巫三段', min: 850 }, { id: 8, n: '山巫二段', min: 1050 }, { id: 9, n: '山巫一段', min: 1250 },
  { id: 10, n: '灵狩三段', min: 1500 }, { id: 11, n: '灵狩二段', min: 1750 }, { id: 12, n: '灵狩一段', min: 2000 },
  { id: 13, n: '荒伯三段', min: 2300 }, { id: 14, n: '荒伯二段', min: 2600 }, { id: 15, n: '荒伯一段', min: 2900 },
  { id: 16, n: '山君三段', min: 3250 }, { id: 17, n: '山君二段', min: 3600 }, { id: 18, n: '山君一段', min: 3950 },
  { id: 19, n: '山海尊一段', min: 4500 }
];
const WIN_GAIN = 30, LOSE_LOSS = 20;

/* 聊天表情：内联 SVG 图标（不使用 emoji） */
const CHAT_ICONS = [
  { key: '笑', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" fill="none" stroke="#f6e6b0" stroke-width="1.6"/><circle cx="9" cy="10" r="1" fill="#f6e6b0"/><circle cx="15" cy="10" r="1" fill="#f6e6b0"/><path d="M8.5 14.6a4.6 4.6 0 0 0 7 0" fill="none" stroke="#f6e6b0" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  { key: '泪', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" fill="none" stroke="#f6e6b0" stroke-width="1.6"/><circle cx="9" cy="10" r="1" fill="#f6e6b0"/><circle cx="15" cy="10" r="1" fill="#f6e6b0"/><path d="M8.2 11.6c1.4 1.6 1.9 2.6 1 3.4-.7.6-1.7.2-2-.8-.3-1 .1-1.9 1-2.6z" fill="#7fd0ff"/><path d="M8.6 15.4a4.4 4.4 0 0 0 6.8 0" fill="none" stroke="#f6e6b0" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  { key: '火', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 3c4 4 6 6.4 6 10a6 6 0 0 1-12 0c0-2.4 1.2-4.2 3.6-6.6.6 1.6 1.3 2.5 2.4 2.9C11.4 6.9 11.6 4.8 12 3z" fill="#e67e22"/><path d="M12 10.6c1.4 1.6 2.2 2.7 2.2 3.9a2.2 2.2 0 0 1-4.4 0c0-1.1.8-2.3 2.2-3.9z" fill="#f6e6b0"/></svg>' },
  { key: '刃', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 2l2 4.4v8.2h-4V6.4z" fill="#e9dbb0"/><path d="M8.4 15.2h7.2" stroke="#b89b58" stroke-width="1.8" stroke-linecap="round"/><path d="M12 16v4.4" stroke="#b89b58" stroke-width="1.8" stroke-linecap="round"/></svg>' },
  { key: '龙', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><text x="12" y="18" font-size="16" text-anchor="middle" fill="#f6e6b0" font-family="STKaiti,KaiTi,serif">龙</text></svg>' },
  { key: '曜', svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 2.5l2.2 5.6 5.8 2.2-5.8 2.2L12 18.3 9.8 12.5 4 10.3l5.8-2.2z" fill="#f6e6b0"/></svg>' }
].map(i => ({ key: i.key, src: 'data:image/svg+xml,' + encodeURIComponent(i.svg) }));

/* 全局状态（不入 data，渲染时映射到 data） */
const state = {
  view: 'picka',
  deck: { attack: [], defense: [], magic: [], reserve: [] },
  pickSel: null,
  swapSel: null,
  order: [], matchIndex: 0, pendingMatch: null,
  totals: { A: 0, B: 0, C: 0 },
  firstX: null, firstY: null, firstZ: null,
  fight: null, fightHistory: [], score: 100, uid: null
};

function clone(o) { return JSON.parse(JSON.stringify(o)); }
function rankOf(score) { let r = RANKS[0]; RANKS.forEach(k => { if (score >= k.min) r = k; }); return r; }
/* ---------- 数据库卡牌 → 战斗卡牌 ----------
 * card 表字段映射规则：
 *   damage_or_shield 为纯数字(1-6)            → 攻击卡，out = 该数字
 *   damage_or_shield 为 H数字(H1-H5)          → 防御卡，guard = 该数字，out = 1
 *   两者皆无（法术/被动卡）                    → 法术卡，按 passive_skill 的效果类型折算：
 *        H 治疗 → heal   /  D 防御增益、N 减益 → guard  /  其余 → out
 *   is_compound = 1（同时有主动+被动技能）     → 通卡，kind 由攻/防决定，mana 由 hp 折算
 */
function numOf(v) { const n = parseInt(v, 10); return isNaN(n) ? 0 : n; }
function mapDbCards(list, skills) {
  const skillMap = {};
  (skills || []).forEach(s => { skillMap[s.code] = s; });
  return (list || []).map(raw => {
    const rawDos = raw.damageOrShield;
    const dos = (rawDos === null || rawDos === undefined || rawDos === 'null' || rawDos === '无') ? '' : String(rawDos);
    const dbId = raw.id !== undefined ? raw.id : raw.card;
    const card = {
      id: 'db' + dbId, dbId: dbId,
      name: raw.cName || ('卡牌' + dbId),
      level: raw.cLevel || '', hp: raw.hp || 0,
      skillName: raw.skillName || '', passiveCode: raw.passiveCode || '',
      type: 'magic', out: 0, guard: 0, heal: 0
    };
    if (/^\d+$/.test(dos)) {
      card.type = 'attack'; card.out = numOf(dos);
    } else if (/^H\d+$/i.test(dos)) {
      card.type = 'defense'; card.guard = numOf(dos.slice(1)); card.out = 1;
    } else {
      const sk = raw.passiveCode ? skillMap[raw.passiveCode] : null;
      const et = sk ? sk.effectType : '';
      const ev = sk ? (sk.effectValue || 0) : 0;
      if (et === 'H') card.heal = ev || 2;
      else if (et === 'D' || et === 'N') card.guard = ev || 1;
      else card.out = Math.max(2, ev);
    }
    const compound = raw.isCompound === 1 || raw.isCompound === true || raw.compound === true;
    if (compound) {
      card.kind = (card.type === 'defense') ? 'def' : 'atk';
      card.type = 'tongue';
      card.mana = Math.max(3, Math.min(6, Math.round((card.hp || 8) / 3)));
      card.sub = card.kind === 'atk' ? '攻·法' : '防·法';
    } else {
      card.sub = card.level || '';   // 非通卡副标题显示品级（黄 / 玄）
    }
    return card;
  }).filter(c => !!c.name);
}

/* 卡强度：用于默认卡组取该类型里最强的前 3 张 */
function strength(c) { return (c.out || 0) + (c.guard || 0) + (c.heal || 0) + (c.mana || 0); }
function topOf(list, n) { return list.slice().sort((a, b) => strength(b) - strength(a)).slice(0, n); }

/** 默认卡组：与具体卡库无关，攻/防/法各取最强的 3 张，备用放通卡（≤2）+ 补位 */
function defaultDeck() {
  const atk = topOf(CARD_DB.filter(c => c.type === 'attack'), 3);
  const def = topOf(CARD_DB.filter(c => c.type === 'defense'), 3);
  const mag = topOf(CARD_DB.filter(c => c.type === 'magic'), 3);
  const tongs = topOf(CARD_DB.filter(c => c.type === 'tongue'), 2);
  const used = {};
  [].concat(atk, def, mag, tongs).forEach(c => { used[c.id] = 1; });
  const res = tongs.slice(0, 3);
  topOf(CARD_DB.filter(c => !used[c.id]), 3 - res.length).forEach(c => res.push(c));
  return { attack: atk, defense: def, magic: mag, reserve: res };
}
/** AI 卡组：从当前卡库里随机抽 攻3/防3/法3 */
function aiDeck() {
  const pick = (kind, n) => {
    const pool = CARD_DB.filter(c => c.type === kind).slice();
    const out = [];
    while (out.length < n && pool.length) {
      out.push(pool.splice(Math.floor(Math.random() * pool.length), 1)[0]);
    }
    return out;
  };
  return { attack: pick('attack', 3), defense: pick('defense', 3), magic: pick('magic', 3) };
}
function isInDeck(id) {
  return ['attack', 'defense', 'magic', 'reserve'].some(g => state.deck[g].some(c => c.id === id));
}
function tongCount() {
  return ['attack', 'defense', 'magic', 'reserve'].reduce((s, g) => s + state.deck[g].filter(c => c.type === 'tongue').length, 0);
}
function canPlace(card, group) {
  if (group === 'attack') return card.type === 'attack' || (card.type === 'tongue' && card.kind === 'atk');
  if (group === 'defense') return card.type === 'defense' || (card.type === 'tongue' && card.kind === 'def');
  if (group === 'magic') return card.type === 'magic';
  return true;
}
function phaseName(phase) { return phase === 'top' ? '争锋' : '运玄'; }

Page({
  data: {
    view: 'picka',
    pool: [], deck: { attack: [], defense: [], magic: [], reserve: [] }, counts: {},
    pickaRing: 120, pickaDanger: false,
    swapTitle: '', swapRing: 20, swapDanger: false, swapDeck: { attack: [], defense: [], magic: [], reserve: [] },
    arenaTitle: '', arenaMeta: '',
    dicePhase: false, diceSpinning: false, diceResult: null, rollDone: false, diceTransform: '',
    fightShown: false, spectator: null,
    opp: {}, me: {}, roundTag: '', timerRing: 30, timerDanger: false,
    tableOpp: [], tableMe: [], tableScroll: 0, battleMsg: '',
    handShown: false, handEmpty: false, handEmptyMsg: '', handPool: [], playTitle: '',
    statusShown: false, battleLogs: [],
    chatShown: false, chatLogs: [], chatInput: '', emojis: CHAT_ICONS,
    cornerBtns: false,
    safeTop: 24, safeBottom: 28, viewH: 1334,
    loading: false, cardSource: '',
    reveal: false, revealTitle: '', revealX: { card: null, who: '' }, revealY: { card: null, who: '' },
    trophy: '', resultTitle: '', resultSub: '', rankRows: [], scoreLines: [], rankChange: '', rankCls: '',
    rankInfo: null, rankFx: null, rankSync: '',
    toastMsg: ''
  },

  onLoad() {
    /* 自定义导航栏：按机型状态栏高度补安全区（rpx） */
    try {
      const sys = wx.getSystemInfoSync();
      const w = sys.windowWidth || 375;
      const sb = sys.statusBarHeight || 20;
      const safeTop = Math.round(sb * 750 / w) + 24;
      const safeBottom = sys.screenHeight && sys.safeArea
        ? Math.round((sys.screenHeight - sys.safeArea.bottom) * 750 / w) + 28
        : 28;
      /* 页面高度用真实可用窗口高度（rpx），避免 100vh 与状态栏/安全区叠加导致整页能滑动 */
      const viewH = Math.round((sys.windowHeight || 667) * 750 / w);
      this.setData({ safeTop: safeTop, safeBottom: safeBottom, viewH: viewH });
      /* 便于排查贴合度问题：打印真实视口参数 */
      console.log('[山海战斗] 视口 w=' + sys.windowWidth + ' h=' + sys.windowHeight,
        '| 屏幕 ' + sys.screenWidth + 'x' + sys.screenHeight,
        '| 状态栏=' + sys.statusBarHeight,
        '| 底部安全区=' + (sys.safeArea && sys.screenHeight ? (sys.screenHeight - sys.safeArea.bottom) : 'n/a'));
    } catch (e) { }
    /* 积分基数：优先用登录后从数据库拿到的积分，其次本地缓存，都没有才兜底 100 */
    const ui = (getApp() && getApp().globalData.userInfo) || {};
    state.uid = ui.userId || ui.uid || null;
    const dbScore = (typeof ui.score === 'number' && ui.score >= 0) ? ui.score : null;
    const saved = wx.getStorageSync('shanhai_score');
    state.score = dbScore !== null ? dbScore : ((typeof saved === 'number' && saved >= 0) ? saved : 100);
    this.showView('picka');
    this.loadCards();
  },

  /* ---------- 从数据库加载卡牌 ----------
   * 1) /card/list 取全量卡（含 passive_code 等完整字段）
   * 2) /skill/list 取被动技能（法术卡的数值来源）
   * 3) 已登录则再取 /warehouse/cards，优先只用我仓库里的卡
   * 任一环节失败都自动回退到 FALLBACK_CARDS，保证页面可用
   */
  loadCards() {
    const app = getApp();
    const ui = app.globalData.userInfo || {};
    const uid = ui.userId || ui.uid;
    this.setData({ loading: true });

    const skillP = api.skillList().then(s => s || []).catch(() => []);
    const cardP = api.cardList().catch(() => null);

    Promise.all([cardP, skillP])
      .then(([cards, skills]) => {
        if (!cards || !cards.length) { return null; }   // 拿不到就保持兜底卡
        const ownedP = uid ? api.ownedCards(uid).catch(() => null) : Promise.resolve(null);
        return ownedP.then(owned => {
          let list = cards;
          if (owned && owned.length) {
            const ids = {};
            owned.forEach(o => { ids[o.card !== undefined ? o.card : o.id] = 1; });
            const mine = cards.filter(c => ids[c.id]);
            if (mine.length >= 9) { list = mine; }      // 仓库卡够组一套才启用
          }
          const mapped = mapDbCards(list, skills);
          if (mapped.length) {
            CARD_DB = mapped;
            cardFromDb = true;
            this.setData({ cardSource: '数据库 · ' + mapped.length + ' 张' });
          }
        });
      })
      .catch(() => { })
      .then(() => {
        this.setData({ loading: false });
        /* 便于验证：控制台输出卡牌来源与前几张卡 */
        console.log('[山海战斗] 卡源 =', cardFromDb ? '数据库' : '本地兜底',
          '| 数量 =', CARD_DB.length,
          '| 示例 =', CARD_DB.slice(0, 6).map(c => c.name + '/' + c.type).join('、'));
        if (!cardFromDb) { this.toast('未读到数据库卡牌，已使用本地卡组'); }
        this.initPickA();
        this.startPickACountdown();
      });
  },
  onUnload() {
    this.clearTimers();
  },

  /* ---------- 工具 ---------- */
  clearTimers() {
    if (this._timers) this._timers.forEach(t => { clearTimeout(t); clearInterval(t); });
    this._timers = [];
    if (this._pickaT) { clearInterval(this._pickaT); this._pickaT = null; }
    if (this._swapT) { clearInterval(this._swapT); this._swapT = null; }
    if (this._fightT) { clearInterval(this._fightT); this._fightT = null; }
    if (this._aiT) { clearTimeout(this._aiT); this._aiT = null; }
  },
  _later(fn, ms) {
    if (!this._timers) this._timers = [];
    this._timers.push(setTimeout(() => { fn(); }, ms));
  },
  showView(name) {
    state.view = name;
    this.setData({ view: name });
  },
  toast(msg) {
    this.setData({ toastMsg: msg });
    if (this._toastT) clearTimeout(this._toastT);
    this._toastT = setTimeout(() => this.setData({ toastMsg: '' }), 1500);
  },

  /* ---------- 卡牌渲染对象 ---------- */
  renderCard(c, selected) {
    const t = c.type;
    let stat = [];
    if (t === 'attack') stat = [{ k: '攻', v: c.out }];
    if (t === 'defense') stat = [{ k: '防', v: c.guard }, { k: '攻', v: c.out }];
    if (t === 'magic') stat = c.out ? [{ k: '法', v: c.out }] : [{ k: '疗', v: c.heal }];
    if (t === 'tongue') stat = (c.kind === 'atk') ? [{ k: '攻', v: c.out }, { k: '法', v: c.mana }] : [{ k: '防', v: c.guard }, { k: '法', v: c.mana }];
    return {
      id: c.id, name: c.name, badge: TYPE_LABEL[t], sub: c.sub || '',
      beastChar: c.name ? c.name.charAt(0) : '',
      img: c.dbId ? ('/photo/' + c.dbId + '.png') : '',   // 数据库卡牌图片（photo/{id}.png）
      type: { attack: 'atk', defense: 'def', magic: 'mag', tongue: 'tong' }[t],
      stat: stat, selected: !!selected, isEmpty: false
    };
  },
  emptySlot(id) { return { id: id, name: '', badge: '', sub: '', beastChar: '', type: '', stat: [], selected: false, isEmpty: true }; },
  renderGroup(deck, sel) {
    const arr = [];
    for (let i = 0; i < 3; i++) {
      arr.push(deck[i] ? this.renderCard(deck[i], sel && sel.group === 'g' && sel.index === i) : this.emptySlot('slot-' + i));
    }
    return arr;
  },

  /* ---------- 界面A：仓库配卡 ---------- */
  initPickA() {
    state.deck = defaultDeck();
    state.pickSel = null;
    this.renderPickA();
  },
  renderPickA() {
    const pool = CARD_DB.filter(c => !isInDeck(c.id)).map(c => this.renderCard(c, state.pickSel === c.id));
    const deck = {};
    ['attack', 'defense', 'magic', 'reserve'].forEach(g => {
      deck[g] = [];
      for (let i = 0; i < 3; i++) {
        deck[g].push(state.deck[g][i] ? this.renderCard(state.deck[g][i]) : this.emptySlot('slot-' + g + i));
      }
    });
    const counts = {};
    ['attack', 'defense', 'magic', 'reserve'].forEach(g => { counts[g] = state.deck[g].length + '/3'; });
    this.setData({ pool, deck, counts });
  },
  onPoolTap(e) {
    const id = e.currentTarget.dataset.id;
    state.pickSel = (state.pickSel === id) ? null : id;
    this.renderPickA();
  },
  onSlotTap(e) {
    const g = e.currentTarget.dataset.g;
    const idx = e.currentTarget.dataset.i;
    if (state.deck[g][idx]) { state.deck[g].splice(idx, 1); state.pickSel = null; this.renderPickA(); return; }
    if (!state.pickSel) { this.toast('请先点选一张仓库卡'); return; }
    const card = CARD_DB.find(c => c.id === state.pickSel);
    if (!canPlace(card, g)) { this.toast('该卡不能放入此组'); return; }
    if (card.type === 'tongue' && tongCount() >= 2) { this.toast('通卡最多携带 2 张'); return; }
    state.deck[g].push(card); state.pickSel = null; this.renderPickA();
  },
  defaultPickA() { this.initPickA(); this.toast('已重置为默认配置'); },
  startPickACountdown() {
    let t = 120;
    this.setData({ pickaRing: t, pickaDanger: false });
    if (this._pickaT) clearInterval(this._pickaT);
    this._pickaT = setInterval(() => {
      t--;
      this.setData({ pickaRing: Math.max(0, t), pickaDanger: t <= 10 });
      if (t <= 0) { clearInterval(this._pickaT); this.confirmPickA(); }
    }, 1000);
  },

  /* ---------- 掷骰与对阵 ---------- */
  confirmPickA() {
    if (this._pickaT) clearInterval(this._pickaT);
    state.order = []; state.matchIndex = 0;
    this.showView('battle');
    this.setData({
      dicePhase: true, fightShown: false, diceResult: null, rollDone: false,
      arenaTitle: '对局准备', arenaMeta: '三人掷骰，点数最高两人进入首轮对战',
      spectator: null, cornerBtns: false, statusShown: false, chatShown: false
    });
  },
  rollDice() {
    const dice = {};
    ['A', 'B', 'C'].forEach(k => dice[k] = Math.floor(Math.random() * 6) + 1);
    /* 累计圈数让骰子每次都向前多转两圈，最终正面停在「我」的点数
       面分布：前3/后2/右5/左1/上6/下4 → 各点数的基础朝向角 */
    this._diceTurns = (this._diceTurns || 0) + 1;
    const base = { 1: [0, 90], 2: [0, 180], 3: [0, 0], 4: [90, 0], 5: [0, -90], 6: [-90, 0] }[dice.A];
    const extra = 720 * this._diceTurns;
    this.setData({
      diceSpinning: true,
      diceTransform: 'rotateX(' + (base[0] + extra) + 'deg) rotateY(' + (base[1] + extra) + 'deg)'
    });
    this._later(() => {
      this.setData({ diceSpinning: false, diceResult: dice, rollDone: true });
      const list = ['A', 'B', 'C'].sort((a, b) => dice[b] - dice[a]);
      state.firstX = list[0]; state.firstY = list[1]; state.firstZ = list[2];
      state.order = [[list[0], list[1]]];
      this.setData({ arenaMeta: ROLES[list[0]].name + ' 与 ' + ROLES[list[1]].name + ' 首轮对战，' + ROLES[list[2]].name + ' 观战' });
      this._later(() => this.goMatch(0), 5000);
    }, 1250);
  },
  ensureMatchOrder(mi) {
    if (state.order[mi]) return;
    if (mi === 1) {
      const f = state.fightHistory[0];
      const high = (f.xhp >= f.yhp) ? f.x : f.y;
      state.order[1] = [high, state.firstZ];
    } else if (mi === 2) {
      const s1Low = (state.fightHistory[0].xhp >= state.fightHistory[0].yhp) ? state.firstY : state.firstX;
      state.order[2] = [state.firstZ, s1Low];
    }
  },
  goMatch(mi) {
    this.ensureMatchOrder(mi);
    const pair = state.order[mi];
    const hasPlayer = pair.some(k => ROLES[k].isPlayer);
    if (hasPlayer) { this.openSwapB(mi); } else { this.startWatch(mi); }
  },

  /* ---------- 界面B：组内交换 ---------- */
  openSwapB(matchIndex) {
    state.pendingMatch = matchIndex;
    state.swapSel = null;
    this.setData({ swapTitle: '第 ' + (matchIndex + 1) + ' 场 · 组内交换', dicePhase: false, fightShown: false });
    this.renderSwapB();
    this.showView('pickb');
    if (this._swapT) clearInterval(this._swapT);
    let t = 20;
    this.setData({ swapRing: t, swapDanger: false });
    this._swapT = setInterval(() => {
      t--;
      this.setData({ swapRing: Math.max(0, t), swapDanger: t <= 5 });
      if (t <= 0) { clearInterval(this._swapT); this.beginMatch(state.order[matchIndex]); }
    }, 1000);
  },
  renderSwapB() {
    const swapDeck = {};
    ['attack', 'defense', 'magic', 'reserve'].forEach(g => {
      swapDeck[g] = [];
      for (let i = 0; i < 3; i++) {
        const sel = state.swapSel && state.swapSel.group === g && state.swapSel.index === i;
        swapDeck[g].push(state.deck[g][i] ? this.renderCard(state.deck[g][i], sel) : this.emptySlot('swap-' + g + i));
      }
    });
    this.setData({ swapDeck });
  },
  onSwapTap(e) {
    const g = e.currentTarget.dataset.g;
    const idx = e.currentTarget.dataset.i;
    const key = { group: g, index: idx };
    if (!state.swapSel) { if (state.deck[g][idx]) { state.swapSel = key; this.renderSwapB(); } return; }
    const a = state.swapSel, b = key;
    if (a.group === b.group && a.index === b.index) { state.swapSel = null; this.renderSwapB(); return; }
    this.doSwap(a, b);
  },
  doSwap(a, b) {
    const ca = state.deck[a.group][a.index], cb = state.deck[b.group][b.index];
    if (ca && !canPlace(ca, b.group)) { this.toast('不能放入目标组'); state.swapSel = null; this.renderSwapB(); return; }
    if (cb && !canPlace(cb, a.group)) { this.toast('目标卡不能放入此组'); state.swapSel = null; this.renderSwapB(); return; }
    let afterTong = tongCount()
      - (ca && ca.type === 'tongue' ? 1 : 0) - (cb && cb.type === 'tongue' ? 1 : 0)
      + (ca && canPlace(ca, b.group) ? (ca.type === 'tongue' ? 1 : 0) : 0)
      + (cb && canPlace(cb, a.group) ? (cb.type === 'tongue' ? 1 : 0) : 0);
    if (afterTong > 2) { this.toast('通卡最多携带 2 张'); state.swapSel = null; this.renderSwapB(); return; }
    state.deck[a.group][a.index] = cb;
    state.deck[b.group][b.index] = ca;
    state.swapSel = null;
    this.renderSwapB();
  },

  /* ---------- 战斗 ---------- */
  beginMatchFromSwap() {
    if (this._swapT) clearInterval(this._swapT);
    this.beginMatch(state.order[state.pendingMatch]);
  },
  fightDeck(roleKey) {
    if (ROLES[roleKey].isPlayer) {
      return clone({ attack: state.deck.attack, defense: state.deck.defense, magic: state.deck.magic });
    }
    const d = aiDeck();
    return clone({ attack: d.attack, defense: d.defense, magic: d.magic });
  },
  availablePool(deck, phase) { return phase === 'top' ? deck.attack.concat(deck.defense) : deck.magic; },
  removeFromDeck(deck, cardId) {
    ['attack', 'defense', 'magic'].forEach(g => {
      const i = deck[g].findIndex(c => c.id === cardId);
      if (i >= 0) deck[g].splice(i, 1);
    });
  },
  beginMatch(pair) {
    state.fight = {
      x: pair[0], y: pair[1], xhp: 10, yhp: 10,
      xDeck: this.fightDeck(pair[0]), yDeck: this.fightDeck(pair[1]),
      round: 1, phase: 'top', xSel: null, ySel: null, xLocked: false, yLocked: false,
      xTongue: null, yTongue: null, history: [], over: false
    };
    this.showView('battle');
    this.setData({
      dicePhase: false, fightShown: true, reveal: false, spectator: null,
      arenaTitle: '对局 · 第 ' + (state.matchIndex + 1) + ' 场',
      arenaMeta: ROLES[pair[0]].name + ' vs ' + ROLES[pair[1]].name,
      cornerBtns: true, statusShown: false, chatShown: false, battleLogs: [],
      battleMsg: ''
    });
    this.renderFight();
    this.startPhase();
  },
  startWatch(mi) {
    state.pendingMatch = mi;
    this.beginMatch(state.order[mi]);
    this.setData({
      spectator: this.buildSpectator(), handShown: false,
      statusShown: false, cornerBtns: false, chatShown: true,
      arenaMeta: '观战中：' + ROLES[state.fight.x].name + ' vs ' + ROLES[state.fight.y].name
    });
  },
  buildSpectator() {
    const pair = state.order[state.matchIndex];
    if (!pair) return null;
    const key = ['A', 'B', 'C'].find(k => pair.indexOf(k) < 0);
    if (!key) return null;
    return { key, name: ROLES[key].name, hp: state.totals[key] || 0 };
  },
  renderFight() {
    const f = state.fight;
    const playerKey = ROLES[f.x].isPlayer ? f.x : f.y;
    const oppKey = playerKey === f.x ? f.y : f.x;
    const oppDeck = oppKey === f.x ? f.xDeck : f.yDeck;
    const playerDeck = playerKey === f.x ? f.xDeck : f.yDeck;
    const oppHp = oppKey === f.x ? f.xhp : f.yhp;
    const meHp = playerKey === f.x ? f.xhp : f.yhp;
    this.setData({
      opp: { key: oppKey, name: ROLES[oppKey].name, hp: oppHp, hpPct: oppHp * 10, deck: '攻' + oppDeck.attack.length + ' 防' + oppDeck.defense.length + ' 法' + oppDeck.magic.length },
      me: { key: playerKey, name: ROLES[playerKey].name, hp: meHp, hpPct: meHp * 10, deck: '攻' + playerDeck.attack.length + ' 防' + playerDeck.defense.length + ' 法' + playerDeck.magic.length },
      roundTag: '回合 ' + f.round + ' / 6 · 【' + phaseName(f.phase) + '】'
    });
    this.renderHand();
    this.renderSpectator();
  },
  renderSpectator() {
    if (!state.fight || !state.order[state.matchIndex]) return;
    const sp = this.buildSpectator();
    if (sp) this.setData({ spectator: sp });
  },
  startPhase() {
    const f = state.fight; if (f.over) return;
    f.xSel = null; f.ySel = null; f.xLocked = false; f.yLocked = false;
    this.renderFight();
    const playerSlot = ROLES[f.x].isPlayer ? 'x' : (ROLES[f.y].isPlayer ? 'y' : null);
    if (playerSlot) {
      const deck = playerSlot === 'x' ? f.xDeck : f.yDeck;
      if (this.availablePool(deck, f.phase).length === 0 || (f.phase === 'bottom' && f[playerSlot + 'Tongue'])) f[playerSlot + 'Locked'] = true;
    }
    this.renderPlayZone();
    if (this._fightT) clearInterval(this._fightT);
    let t = 30;
    this.setData({ timerRing: t, timerDanger: false });
    this._fightT = setInterval(() => {
      t--;
      this.setData({ timerRing: Math.max(0, t), timerDanger: t <= 5 });
      if (t <= 0) {
        clearInterval(this._fightT);
        ['x', 'y'].forEach(slot => {
          if (!f[slot + 'Locked']) {
            const deck = slot === 'x' ? f.xDeck : f.yDeck;
            const pool = this.availablePool(deck, f.phase);
            if (pool.length > 0) f[slot + 'Sel'] = pool[Math.floor(Math.random() * pool.length)];
            f[slot + 'Locked'] = true;
          }
        });
        this.maybeResolve();
      }
    }, 1000);
    this.scheduleAI();
  },
  renderPlayZone() {
    const f = state.fight;
    const playerSlot = ROLES[f.x].isPlayer ? 'x' : (ROLES[f.y].isPlayer ? 'y' : null);
    if (!playerSlot) { this.setData({ handShown: false }); return; }
    this.setData({ handShown: true });
    const deck = playerSlot === 'x' ? f.xDeck : f.yDeck;
    const pool = this.availablePool(deck, f.phase);
    this.setData({ playTitle: '待选手牌 · 【' + phaseName(f.phase) + '】' + (f.phase === 'top' ? '攻防通卡' : '法术') + '牌组' });
    const tongue = f[playerSlot + 'Tongue'];
    if (f.phase === 'bottom' && tongue) {
      this.setData({ handEmpty: true, handEmptyMsg: '通卡【' + tongue.name + '】的法术能力生效，本半场免出牌，法术直接触发', handPool: [] });
      f[playerSlot + 'Locked'] = true;
      return;
    }
    if (pool.length === 0) {
      this.setData({ handEmpty: true, handEmptyMsg: '本半场无牌可出，自动跳过', handPool: [] });
      f[playerSlot + 'Locked'] = true;
      return;
    }
    this.setData({ handEmpty: false, handPool: pool.map(c => this.renderCard(c, false)) });
    this.setData({ battleMsg: '请点选一张卡牌' });
  },
  onSelectHand(e) {
    const f = state.fight; if (!f || f.over) return;
    const ps = ROLES[f.x].isPlayer ? 'x' : (ROLES[f.y].isPlayer ? 'y' : null);
    if (!ps || f[ps + 'Locked']) return;
    const i = e.currentTarget.dataset.i;
    const deck = ps === 'x' ? f.xDeck : f.yDeck;
    const pool = this.availablePool(deck, f.phase);
    const c = pool[i];
    if (f[ps + 'Sel'] && f[ps + 'Sel'].id === c.id) {
      f[ps + 'Sel'] = null;
      this.setData({ handPool: pool.map(cd => this.renderCard(cd, false)), battleMsg: '已取消选择，请点选一张卡牌' });
    } else {
      f[ps + 'Sel'] = c;
      this.setData({ handPool: pool.map(cd => this.renderCard(cd, cd.id === c.id)), battleMsg: '已选 ' + c.name + '，点「确认出牌」结算' });
    }
  },
  onConfirm() {
    const f = state.fight; if (!f || f.over) return;
    const ps = ROLES[f.x].isPlayer ? 'x' : (ROLES[f.y].isPlayer ? 'y' : null);
    if (!ps || f[ps + 'Locked']) return;
    if (!f[ps + 'Sel']) { this.toast('请先选择一张牌'); return; }
    f[ps + 'Locked'] = true;
    this.maybeResolve();
  },
  scheduleAI() {
    if (this._aiT) clearTimeout(this._aiT);
    this._aiT = setTimeout(() => {
      const f = state.fight; if (f.over) return;
      ['x', 'y'].forEach(slot => {
        const key = f[slot];
        if (ROLES[key].isPlayer || f[slot + 'Locked']) return;
        if (f.phase === 'bottom' && f[slot + 'Tongue']) { f[slot + 'Locked'] = true; return; }
        const deck = slot === 'x' ? f.xDeck : f.yDeck;
        const pool = this.availablePool(deck, f.phase);
        if (pool.length === 0) { f[slot + 'Locked'] = true; }
        else { f[slot + 'Sel'] = pool[Math.floor(Math.random() * pool.length)]; f[slot + 'Locked'] = true; }
      });
      this.maybeResolve();
    }, 900);
  },
  maybeResolve() {
    const f = state.fight;
    if (f.over || !(f.xLocked && f.yLocked)) return;
    if (this._fightT) clearInterval(this._fightT);
    if (this._aiT) clearTimeout(this._aiT);
    this.showReveal();
  },
  showReveal() {
    const f = state.fight;
    const xc = f.xSel, yc = f.ySel;
    const xTongue = (f.phase === 'bottom' && f.xTongue && !xc) ? f.xTongue : null;
    const yTongue = (f.phase === 'bottom' && f.yTongue && !yc) ? f.yTongue : null;
    f.history.forEach(h => h.current = false);
    f.history.push({ phase: f.phase, xCard: xc ? clone(xc) : null, yCard: yc ? clone(yc) : null, current: true, xTongue: xTongue ? clone(xTongue) : null, yTongue: yTongue ? clone(yTongue) : null });
    const xShow = xc || xTongue, yShow = yc || yTongue;
    this.setData({
      reveal: true,
      revealTitle: '亮牌 · ' + phaseName(f.phase),
      revealX: { card: xShow ? this.renderCard(xShow) : null, who: ROLES[f.x].name },
      revealY: { card: yShow ? this.renderCard(yShow) : null, who: ROLES[f.y].name },
      battleMsg: '亮牌！ ' + ROLES[f.x].name + '【' + this.skillName(xc || xTongue) + '】 vs ' + ROLES[f.y].name + '【' + this.skillName(yc || yTongue) + '】'
    });
    this._later(() => this.closeReveal(), 2000);
  },
  skillName(card) {
    if (!card) return '不出牌';
    if (card.type === 'tongue') return card.name + '·' + (card.kind === 'atk' ? '攻' : '防') + '·法';
    return card.name + '·' + ({ attack: '攻', defense: '防', magic: '法' }[card.type]);
  },
  closeReveal() {
    this.setData({ reveal: false });
    this.renderHand();
    this._later(() => this.resolveRound(), 600);
  },
  renderHand() {
    const f = state.fight;
    if (!f) return;
    const playerKey = ROLES[f.x].isPlayer ? f.x : f.y;
    const oppKey = playerKey === f.x ? f.y : f.x;
    const cardFor = (key, h) => h[key === f.x ? 'xCard' : 'yCard'];
    const tongueFor = (key, h) => h[key === f.x ? 'xTongue' : 'yTongue'];
    if (!f.history.length) { this.setData({ tableOpp: [], tableMe: [] }); return; }
    /* 桌面展示最近 6 回合（行内可横向滑动，不会被挤压），完整历史看战斗记录 */
    const buildRow = (key) => f.history.slice(-6).map(h => {
      const cur = h.current;
      const c = cardFor(key, h), t = tongueFor(key, h);
      if (t && !c) return { card: null, pass: '通·法', revealed: cur };
      return c ? { card: this.renderCard(c), pass: '', revealed: cur } : { card: null, pass: '不出', revealed: cur };
    });
    const rows = [buildRow(oppKey), buildRow(playerKey)];
    /* 默认停在最新一张（行尾）：给一个会被裁剪到最大值的滚动位置，数值随张数变化以触发滚动 */
    this.setData({
      tableOpp: rows[0], tableMe: rows[1],
      tableScroll: 9000 + rows[0].length
    });
  },
  resolveRound() {
    const f = state.fight;
    const xc = f.xSel, yc = f.ySel;
    f.xhp += (xc ? xc.heal : 0); f.yhp += (yc ? yc.heal : 0);
    let xOut = 0, yOut = 0;
    if (f.phase === 'top') { xOut = xc ? xc.out : 0; yOut = yc ? yc.out : 0; }
    else { xOut = xc ? xc.out : (f.xTongue ? f.xTongue.mana : 0); yOut = yc ? yc.out : (f.yTongue ? f.yTongue.mana : 0); }
    const xGuard = (xc && (xc.type === 'defense' || (xc.type === 'tongue' && xc.kind === 'def'))) ? xc.guard : 0;
    const yGuard = (yc && (yc.type === 'defense' || (yc.type === 'tongue' && yc.kind === 'def'))) ? yc.guard : 0;
    const dmgToY = Math.max(0, xOut - yGuard);
    const dmgToX = Math.max(0, yOut - xGuard);
    f.yhp = Math.max(0, f.yhp - dmgToY);
    f.xhp = Math.max(0, f.xhp - dmgToX);
    if (f.phase === 'top') { f.xTongue = (xc && xc.type === 'tongue') ? xc : null; f.yTongue = (yc && yc.type === 'tongue') ? yc : null; }
    else { f.xTongue = null; f.yTongue = null; }
    if (xc) this.removeFromDeck(f.xDeck, xc.id);
    if (yc) this.removeFromDeck(f.yDeck, yc.id);
    const note = '回合' + f.round + '·' + phaseName(f.phase) + '：' + ROLES[f.x].name + ' 造成' + dmgToY + ' · ' + ROLES[f.y].name + ' 造成' + dmgToX;
    this.setData({ battleLogs: (this.data.battleLogs || []).concat([note]), battleMsg: ROLES[f.x].name + '【' + (xc ? xc.name : '不出牌') + '】 vs ' + ROLES[f.y].name + '【' + (yc ? yc.name : '不出牌') + '】' });
    f.xSel = null; f.ySel = null;
    if (f.xhp <= 0 || f.yhp <= 0) {
      this._later(() => this.endMatch(), 900);
    } else if (f.phase === 'top') {
      f.phase = 'bottom'; this._later(() => this.startPhase(), 900);
    } else {
      f.phase = 'top';
      if (f.round >= 6) { this._later(() => this.endMatch(), 900); }
      else { f.round++; this._later(() => this.startPhase(), 900); }
    }
  },
  endMatch() {
    const f = state.fight; f.over = true;
    state.totals[f.x] += f.xhp;
    state.totals[f.y] += f.yhp;
    state.fightHistory.push({ x: f.x, y: f.y, xhp: f.xhp, yhp: f.yhp });
    const winner = f.xhp >= f.yhp ? f.x : f.y;
    this.setData({ battleMsg: '本场结束：' + ROLES[winner].name + ' 血量更高' });
    const next = state.matchIndex + 1;
    state.matchIndex = next;
    if (next < 3) {
      this.setData({ arenaMeta: '进入第 ' + (next + 1) + ' 场' });
      this._later(() => this.goMatch(next), 1200);
    } else {
      this._later(() => this.showResult(), 1200);
    }
  },

  /* ---------- 结算 ---------- */
  showResult() {
    this.showView('result');
    const entries = ['A', 'B', 'C'].map(k => ({ key: k, total: state.totals[k] || 0 })).sort((a, b) => b.total - a.total);
    const champ = entries[0].key;
    const rankRows = entries.map((e, i) => ({
      no: i + 1, first: i === 0,
      nm: ROLES[e.key].name + (ROLES[e.key].isPlayer ? '（我）' : ''),
      hp: e.total + ' 血'
    }));
    const trophySvg = champ === 'A'
      ? '<svg viewBox="0 0 48 48"><path d="M12 6h24v9a12 12 0 0 1-24 0z" fill="#f6e6b0"/><rect x="20" y="27" width="8" height="7" fill="#b89b58"/><rect x="15" y="33" width="18" height="3" fill="#b89b58"/><rect x="22" y="36" width="4" height="6" fill="#b89b58"/></svg>'
      : '<svg viewBox="0 0 48 48"><path d="M12 6h24v9a12 12 0 0 1-24 0z" fill="#8a6a2f"/><rect x="20" y="27" width="8" height="7" fill="#b89b58"/><rect x="15" y="33" width="18" height="3" fill="#b89b58"/><rect x="22" y="36" width="4" height="6" fill="#b89b58"/></svg>';
    const isWin = champ === 'A';
    const prev = state.score, nextScore = isWin ? prev + WIN_GAIN : Math.max(0, prev - LOSE_LOSS);
    state.score = nextScore;
    const pr = rankOf(prev), nr = rankOf(nextScore);
    const delta = nr.id > pr.id ? 'up' : (nr.id < pr.id ? 'down' : 'same');
    const rankChange = isWin
      ? (delta === 'up' ? '积分达标，段位晋升！' : '积分增加')
      : (delta === 'down' ? '积分不足门槛，段位下降' : '积分扣除');
    this.setData({
      trophy: 'data:image/svg+xml,' + encodeURIComponent(trophySvg),
      resultTitle: champ === 'A' ? '胜利！冠军是你' : '本局冠军：' + ROLES[champ].name,
      resultSub: '三人两场累计剩余血量，最高者获胜',
      rankRows,
      scoreLines: [
        { k: '本局结果', v: (isWin ? '胜利 +' + WIN_GAIN : '失败 -' + LOSE_LOSS) },
        { k: '我的积分', v: prev + ' → ' + nextScore },
        { k: '段位', v: pr.n + ' → ' + nr.n }
      ],
      rankChange: rankChange,
      rankCls: delta === 'down' ? 'down' : 'up',
      rankInfo: this.buildRankInfo(nextScore, pr, nr)
    });
    /* 上报数据库：后端累加积分并回算段位，成功后用库里的真实数据刷新进度条 */
    this.reportScore(isWin ? WIN_GAIN : -LOSE_LOSS, nextScore, delta);
  },

  /** 段位进度条数据：当前段位、下一级、还差多少分、段内进度百分比 */
  buildRankInfo(score, prevRank, rank) {
    const cur = rank || rankOf(score || 0);
    const idx = RANKS.findIndex(r => r.id === cur.id);
    const up = idx >= 0 && idx < RANKS.length - 1 ? RANKS[idx + 1] : null;
    const base = cur.min || 0;
    const target = up ? up.min : base;
    let pct = up ? Math.round((score - base) * 100 / Math.max(1, target - base)) : 100;
    pct = Math.max(0, Math.min(100, pct));
    return {
      rank: cur.n,
      prevRank: prevRank ? prevRank.n : cur.n,
      nextRank: up ? up.n : '',
      toNext: up ? Math.max(0, target - score) : 0,
      progress: pct,
      full: !up || pct >= 100
    };
  },

  /** 结算积分上报数据库（失败则回落为本地记录，不影响流程） */
  reportScore(delta, nextScore, localDelta) {
    if (!state.uid) {
      this.setData({ rankSync: '未登录，积分仅本地记录' });
      return;
    }
    this.setData({ rankSync: '同步中…' });
    api.scoreReport(state.uid, delta).then(d => {
      if (!d) return;
      state.score = typeof d.score === 'number' ? d.score : nextScore;
      const app = getApp();
      if (app && app.globalData.userInfo) app.globalData.userInfo.score = state.score;
      this.setData({
        rankInfo: {
          rank: d.rank || '',
          prevRank: d.prevRank || d.rank || '',
          nextRank: (d.nextRank && d.nextRank !== '已是最高段位') ? d.nextRank : '',
          toNext: d.toNext || 0,
          progress: d.progress || 0,
          full: !d.nextRank || d.nextRank === '已是最高段位' || (d.progress >= 100)
        },
        rankSync: '已同步数据库',
        scoreLines: [
          { k: '本局结果', v: (delta > 0 ? '胜利 +' + delta : '失败 ' + delta) },
          { k: '我的积分', v: (d.prevScore != null ? d.prevScore : '') + ' → ' + state.score },
          { k: '段位', v: (d.prevRank || '') + ' → ' + (d.rank || '') }
        ]
      });
      if (d.changed === 'up') this.playRankFx('段位晋升', d.prevRank, d.rank, true);
      else if (d.changed === 'down') this.playRankFx('段位下降', d.prevRank, d.rank, false);
    }).catch(() => {
      /* 后端没连上：保留本地计算的结果 */
      this.setData({ rankSync: '后端未连接，积分仅本地记录' });
    });
  },

  /** 提前关闭段位特效 */
  hideRankFx() { this.setData({ rankFx: null }); },

  /** 升段 / 降段全屏特效：徽章弹出 + 金光扫过 + 粒子 */
  playRankFx(title, prevRank, rank, up) {
    const color = up ? '#f6e6b0' : '#9aa0a6';
    const dark = up ? '#b89b58' : '#5b5f66';
    const svg = '<svg viewBox="0 0 48 48">'
      + '<path d="M12 6h24v9a12 12 0 0 1-24 0z" fill="' + color + '"/>'
      + '<rect x="20" y="27" width="8" height="7" fill="' + dark + '"/>'
      + '<rect x="15" y="33" width="18" height="3" fill="' + dark + '"/>'
      + '<rect x="22" y="36" width="4" height="6" fill="' + dark + '"/></svg>';
    this.setData({ rankFx: { cls: up ? 'up' : 'down', badge: 'data:image/svg+xml,' + encodeURIComponent(svg), title: title, sub: (prevRank || '—') + ' → ' + (rank || '—') } });
    this._later(() => this.setData({ rankFx: null }), 2600);
  },
  backHome() {
    wx.setStorageSync('shanhai_score', state.score);
    this.toast('已返回首页主菜单');
    state.totals = { A: 0, B: 0, C: 0 };
    state.order = []; state.matchIndex = 0; state.pendingMatch = null;
    state.fightHistory = []; state.pickSel = null; state.swapSel = null;
    this.clearTimers();
    this.initPickA();
    this.showView('picka');
    /* 返回功能展示页：优先回退到上一页（不销毁重建 → 不会闪白屏）；
       只有直接从战斗页启动（没有上一页）时才 reLaunch */
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack({ delta: 1 });
    } else {
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  /* ---------- 右下角按钮 / 聊天 ---------- */
  /* 两个面板互斥：同时展开会超出一屏被裁掉 */
  toggleChat() {
    const on = !this.data.chatShown;
    this.setData({ chatShown: on, statusShown: false });
  },
  toggleStatus() {
    const on = !this.data.statusShown;
    this.setData({ statusShown: on, chatShown: false });
  },
  /* 调试探针：打印关键容器实际渲染的边框/圆角/阴影与几何信息（排查幻影边框用） */
  dumpStyles() {
    const sel = ['.container', '.view.active', '.view-box', '.view-bg', '.view-inner', '.arena-stage', '.fight-zone', '.hand-zone', '.corner-btns'];
    const q = wx.createSelectorQuery();
    sel.forEach(s => q.select(s).fields({
      rect: true, size: true,
      computedStyle: ['border-top-width', 'border-top-style', 'border-top-color', 'border-radius', 'box-shadow']
    }));
    q.exec(res => {
      const lines = sel.map((s, i) => {
        const r = res[i];
        if (!r) return s + ' => 未渲染';
        return s + ' => rect[' + Math.round(r.left) + ',' + Math.round(r.top) + ' ' + Math.round(r.width) + 'x' + Math.round(r.height) + ']'
          + ' border[' + r['border-top-width'] + ' ' + r['border-top-style'] + ' ' + r['border-top-color'] + ']'
          + ' radius[' + r['border-radius'] + ']'
          + ' shadow[' + String(r['box-shadow']).slice(0, 60) + ']';
      });
      console.log('[山海战斗][样式探针]\n' + lines.join('\n'));
      wx.setStorageSync('style_dump', lines.join('\n'));
    });
  },
  onChatInput(e) { this.setData({ chatInput: e.detail.value }); },
  sendChat() {
    const txt = (this.data.chatInput || '').trim();
    if (!txt) return;
    const logs = this.data.chatLogs.slice();
    logs.push({ who: '我', txt: txt, parts: this.parseChat(txt), sys: false });
    this.setData({ chatLogs: logs, chatInput: '' });
  },
  /* 把 [笑] 之类的标记解析为「文字片段 + SVG 图标片段」 */
  parseChat(txt) {
    const parts = [];
    const map = {};
    CHAT_ICONS.forEach(i => { map[i.key] = i.src; });
    const re = /\[([^\]\[]{1,6})\]/g;
    let last = 0, m;
    while ((m = re.exec(txt)) !== null) {
      if (m.index > last) parts.push({ t: 'text', v: txt.slice(last, m.index) });
      if (map[m[1]]) parts.push({ t: 'icon', src: map[m[1]] });
      else parts.push({ t: 'text', v: m[0] });
      last = m.index + m[0].length;
    }
    if (last < txt.length) parts.push({ t: 'text', v: txt.slice(last) });
    return parts;
  },
  addEmoji(e) {
    this.setData({ chatInput: (this.data.chatInput || '') + '[' + e.currentTarget.dataset.key + ']' });
  }
});
