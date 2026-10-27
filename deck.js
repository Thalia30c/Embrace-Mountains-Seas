// pages/deck/deck.js
// 牌组页：管理我的牌组，增删卡牌

const api = require('../../utils/api');

Page({
  data: {
    decks: [],
    allCards: [],
    showAddCard: false,
    currentDeckId: null,
    showCardPicker: false
  },

  onShow() {
    this.loadDecks();
    this.loadCards();
  },

  /** 加载我的牌组 */
  loadDecks() {
    api.deckList().then((decks) => {
      this.setData({ decks: decks || [] });
    }).catch(() => {});
  },

  /** 加载全部卡牌（选择用） */
  loadCards() {
    api.cardList().then((cards) => {
      this.setData({ allCards: cards || [] });
    }).catch(() => {});
  },

  /** 创建牌组 */
  async onCreate() {
    const name = await new Promise((resolve) => {
      wx.showModal({
        title: '新建牌组',
        editable: true,
        placeholderText: '输入牌组名称',
        success: (res) => resolve(res.confirm ? res.content : null)
      });
    });
    if (!name) return;
    await api.deckCreate(name);
    wx.showToast({ title: '创建成功', icon: 'success' });
    this.loadDecks();
  },

  /** 删除牌组 */
  async onDelete(e) {
    const id = e.currentTarget.dataset.id;
    const confirm = await new Promise((resolve) => {
      wx.showModal({
        title: '删除牌组',
        content: '删除后不可恢复，确定吗？',
        success: (res) => resolve(res.confirm)
      });
    });
    if (!confirm) return;
    await api.deckDelete(id);
    this.loadDecks();
  },

  /** 设为默认牌组 */
  async onSetDefault(e) {
    const id = e.currentTarget.dataset.id;
    await api.deckSetDefault(id);
    wx.showToast({ title: '已设为默认', icon: 'success' });
    this.loadDecks();
  },

  /** 打开选卡面板 */
  openPicker(e) {
    const deckId = e.currentTarget.dataset.id;
    this.setData({ showCardPicker: true, currentDeckId: deckId });
  },

  closePicker() {
    this.setData({ showCardPicker: false });
  },

  noop() {},

  /** 往牌组加卡 */
  async onAddCard(e) {
    const cardId = e.currentTarget.dataset.cardid;
    await api.deckAddCard(this.data.currentDeckId, cardId);
    wx.showToast({ title: '已加入', icon: 'success' });
    this.loadDecks();
  },

  /** 从牌组移除卡 */
  async onRemoveCard(e) {
    const deckId = e.currentTarget.dataset.deckid;
    const cardId = e.currentTarget.dataset.cardid;
    await api.deckRemoveCard(deckId, cardId);
    this.loadDecks();
  }
});
