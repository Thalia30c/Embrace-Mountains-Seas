// pages/warehouse/warehouse.js
// 仓库页：展示当前用户拥有的全部卡牌，支持按种类 / 等级筛选，每四张一排滑动展示

const api = require('../../utils/api');

const getAppInstance = () => getApp();

// 类型中文名，供 wxml 映射
const KIND_NAME = { attack: '攻击', defense: '防御', magic: '法术' };

Page({
  data: {
    cards: [],            // 该用户拥有的全部卡牌（去重后）
    filteredCards: [],    // 经过种类/等级筛选后用于展示的卡牌
    typeFilter: 'all',    // all / attack / defense / magic
    levelFilter: 'all',   // all / 黄 / 玄
    kindName: KIND_NAME,  // 供 wxml 通过 kindName[kind] 取中文
    loading: true
  },

  onShow() {
    this.loadCards();
  },

  /** 进场加载：读取该用户拥有的全部卡牌详情 */
  loadCards() {
    const userInfo = getAppInstance().globalData.userInfo;
    if (!userInfo || !userInfo.userId) {
      this.setData({ loading: false });
      return;
    }
    api.ownedCards(userInfo.userId)
      .then((data) => {
        // 等级类名需为 ASCII（WXSS 不支持中文类名），这里提前映射好
        const cards = (data || []).map((c) =>
          Object.assign({}, c, {
            lvClass: c.cLevel === '黄' ? 'huang' : c.cLevel === '玄' ? 'xuan' : 'other'
          })
        );
        this.setData({ cards, loading: false });
        this.applyFilter();
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  /** 按种类筛选 */
  onTypeFilter(e) {
    this.setData({ typeFilter: e.currentTarget.dataset.type });
    this.applyFilter();
  },

  /** 按等级筛选 */
  onLevelFilter(e) {
    this.setData({ levelFilter: e.currentTarget.dataset.level });
    this.applyFilter();
  },

  /** 根据两个筛选条件计算展示列表 */
  applyFilter() {
    const { cards, typeFilter, levelFilter } = this.data;
    const filtered = cards.filter((c) => {
      const okType = typeFilter === 'all' || c.kind === typeFilter;
      const okLevel = levelFilter === 'all' || c.cLevel === levelFilter;
      return okType && okLevel;
    });
    this.setData({ filteredCards: filtered });
  }
});
