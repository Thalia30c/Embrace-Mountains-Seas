// pages/battle/battle.js
// 对战页：展示双方状态与手牌，出牌/抽牌/过牌
//
// 当前是本地轮询版（单机演示）。改成实时对战时，
// 把 pollState 换成 WebSocket 推送即可，页面结构不用动。

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    battleId: null,
    state: null,
    myHand: [],
    players: [],
    currentTurnUserId: null,
    isMyTurn: false,
    myUserId: null,
    selectedCardId: null,
    finished: false,
    winnerName: '',
    loadFailed: false
  },

  onLoad(options) {
    this.setData({ battleId: options.id });
    this.timer = null;
  },

  onShow() {
    this.pollState();
    // 每 2 秒拉一次状态
    this.timer = setInterval(() => this.pollState(), 2000);
  },

  onHide() {
    this.clearTimer();
  },

  onUnload() {
    this.clearTimer();
  },

  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },

  /** 拉取对局状态 */
  pollState() {
    const battleId = this.data.battleId;
    if (!battleId) return;
    api.battleState(battleId).then((state) => {
      const myId = getAppInstance().globalData.userInfo ? getAppInstance().globalData.userInfo.userId : null;
      // 后端对本人返回 hand 数组、对他人返回 handCount，这里统一成 handCount
      // （WXML 表达式不支持 !== 等复杂写法，复杂计算放在 JS 里做）
      const players = (state.players || []).map((p) => ({
        ...p,
        handCount: typeof p.handCount === 'number'
          ? p.handCount
          : (p.hand ? p.hand.length : 0)
      }));
      this.setData({
        state: state,
        myHand: state.myHand || [],
        players: players,
        currentTurnUserId: state.currentTurnUserId,
        myUserId: myId,
        isMyTurn: state.currentTurnUserId === myId,
        finished: state.status === 'finished',
        winnerName: state.winnerName || '',
        loadFailed: false
      });
    }).catch(() => {
      this.setData({ loadFailed: true });
    });
  },

  /** 选中卡牌 */
  onSelectCard(e) {
    const cardId = e.currentTarget.dataset.cardid;
    this.setData({ selectedCardId: cardId });
  },

  /** 出牌 */
  async onPlay() {
    const cardId = this.data.selectedCardId;
    if (!cardId) {
      wx.showToast({ title: '请先选择一张卡', icon: 'none' });
      return;
    }
    // 攻击卡需要选目标，这里简化为自动选第一个对手
    const card = this.data.myHand.find((c) => c.id === cardId);
    let targetUserId = null;
    if (card && card.type === 'attack') {
      const opponent = this.data.players.find((p) => p.userId !== this.data.myUserId);
      targetUserId = opponent ? opponent.userId : null;
    }
    await api.battlePlay(this.data.battleId, cardId, targetUserId);
    this.setData({ selectedCardId: null });
    this.pollState();
  },

  /** 抽牌 */
  async onDraw() {
    await api.battleDraw(this.data.battleId);
    this.pollState();
  },

  /** 过牌 */
  async onPass() {
    await api.battlePass(this.data.battleId);
    this.pollState();
  },

  /** 认输 */
  async onSurrender() {
    const confirm = await new Promise((resolve) => {
      wx.showModal({
        title: '确认认输',
        content: '认输将直接判负，确定吗？',
        success: (res) => resolve(res.confirm)
      });
    });
    if (!confirm) return;
    await api.battleSurrender(this.data.battleId);
    this.pollState();
  },

  /** 对局结束后返回大厅 */
  goHall() {
    wx.redirectTo({ url: '/pages/hall/hall' });
  }
});
