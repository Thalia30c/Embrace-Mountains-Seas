// pages/index/index.js
// 山海异兽卡牌对战系统 · 首页总览
// 布局参照设计稿参考实现；资源栏数据来自后端真实接口
// （/user/{uid}、/card/list、/warehouse/{id}/cards）

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    userInfo: null,
    myCardCount: 0,
    cardTotal: 0
  },

  onLoad() {
    this.checkLogin();
  },

  onShow() {
    this.checkLogin();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => wx.stopPullDownRefresh());
  },

  /** 校验登录态，已登录则拉取首页数据 */
  checkLogin() {
    const app = getAppInstance();
    if (!app.globalData.token) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.setData({ userInfo: app.globalData.userInfo || {} });
    this.loadData();
  },

  /** 并行拉取首页数据，任一接口失败不影响其它模块展示 */
  loadData() {
    const app = getAppInstance();
    const userInfo = app.globalData.userInfo || {};

    const tasks = [
      // 以数据库为准刷新积分等信息
      userInfo.userId ? api.userDetail(userInfo.userId)
        .then((user) => {
          if (!user) return;
          const merged = Object.assign({}, userInfo, {
            score: user.score,
            gameLevel: user.gameLevel,
            warehouseId: user.warehouseId,
            nickname: user.username || userInfo.nickname
          });
          app.globalData.userInfo = merged;
          wx.setStorageSync('userInfo', merged);
          this.setData({ userInfo: merged });
        })
        .catch(() => {}) : Promise.resolve(),

      // 图鉴卡牌总数
      api.cardList()
        .then((list) => this.setData({ cardTotal: (list || []).length }))
        .catch(() => {}),

      // 我的仓库卡牌数
      userInfo.warehouseId ? api.warehouseCards(userInfo.warehouseId)
        .then((ids) => this.setData({ myCardCount: (ids || []).length }))
        .catch(() => {}) : Promise.resolve()
    ];

    return Promise.all(tasks);
  },

  /** 抽卡（唤灵）：页面待开发 */
  onGacha() {
    wx.showToast({ title: '唤灵卡池即将开放', icon: 'none' });
  },

  /** 战斗 → 对战大厅 */
  goHall() {
    wx.navigateTo({ url: '/pages/hall/hall' });
  },

  /** 卡组 */
  goDeck() {
    wx.navigateTo({ url: '/pages/deck/deck' });
  },

  /** 商店：页面待开发 */
  onShop() {
    wx.showToast({ title: '商店即将开放', icon: 'none' });
  },

  /** 个人 */
  goProfile() {
    wx.navigateTo({ url: '/pages/profile/profile' });
  }
});
