// utils/api.js
// 接口清单：页面只关心"调哪个业务接口"，具体 URL 集中在这里

const { get, post, del } = require('./request');

module.exports = {
  // ---------------- 用户与认证 ----------------
  login: (username, password) => post('/user/login', { username, password }),
  register: (username, password, nickname) => post('/user/register', { username, password, nickname }),
  wxLogin: (code, nickname, avatar) => post('/user/wxLogin', { code, nickname, avatar }),
  userInfo: () => get('/user/info'),
  updateProfile: (nickname, avatar) => post('/user/profile', null, { nickname, avatar }),
  ranking: (limit = 20) => get('/user/ranking', { limit }),

  // ---------------- 卡牌 ----------------
  cardPage: (params) => get('/card/page', params),
  cardList: () => get('/card/list'),
  cardDetail: (id) => get('/card/' + id),
  myCards: () => get('/card/mine'),

  // ---------------- 牌组 ----------------
  // 当前登录用户仓库里的卡牌编号列表（来自 warehouse.card_list）
  myWarehouseCards: (uid) => get('/warehouse/mine', { uid }),
  // 读取当前用户卡组编队（四组已放置的卡牌编号 + 仓库全部卡牌）
  deckLoad: (uid) => get('/warehouse/deck', { uid }),
  // 锁卡：把四组卡组结果写回 warehouse 表
  deckLock: (payload) => post('/warehouse/deck', payload),
  deckList: () => get('/deck/list'),
  deckCreate: (name) => post('/deck/create', null, { name }),
  deckDelete: (id) => del('/deck/' + id),
  deckSetDefault: (id) => post('/deck/' + id + '/default'),
  deckAddCard: (deckId, cardId) => post('/deck/' + deckId + '/card', null, { cardId }),
  deckRemoveCard: (deckId, cardId) => del('/deck/' + deckId + '/card/' + cardId),

  // ---------------- 房间 ----------------
  roomList: () => get('/room/list'),
  roomCreate: (data) => post('/room/create', data),
  roomJoin: (roomNo, password) => post('/room/join', null, { roomNo, password }),
  roomLeave: (id) => post('/room/' + id + '/leave'),
  roomStart: (id) => post('/room/' + id + '/start'),
  roomDetail: (id) => get('/room/' + id),

  // ---------------- 对战 ----------------
  battleState: (id) => get('/battle/' + id + '/state'),
  battlePlay: (battleId, cardId, targetUserId) =>
    post('/battle/play', { battleId, cardId, targetUserId }),
  battleDraw: (id) => post('/battle/' + id + '/draw'),
  battlePass: (id) => post('/battle/' + id + '/pass'),
  battleSurrender: (id) => post('/battle/' + id + '/surrender'),
  battleDetails: (id) => get('/battle/' + id + '/details')
};
