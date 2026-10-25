// pages/index/index.js
// 首页：入口导航 + 玩家信息展示

const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    userInfo: null,
    isLogin: false
  },

  onLoad() {
    this.checkLogin();
  },

  onShow() {
    this.checkLogin();
  },

  /** 检查登录状态并拉取最新用户信息 */
  checkLogin() {
    if (!app.globalData.token) {
      this.setData({ userInfo: null, isLogin: false });
      return;
    }
    this.setData({ isLogin: true, userInfo: app.globalData.userInfo });
    api.userInfo().then((user) => {
      app.globalData.userInfo = user;
      wx.setStorageSync('userInfo', user);
      this.setData({ userInfo: user });
    }).catch(() => {
      // 令牌失效已在 request.js 中统一处理
    });
  },

  /** 去登录 */
  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  /** 去大厅 */
  goHall() {
    wx.navigateTo({ url: '/pages/hall/hall' });
  },

  /** 去牌组 */
  goDeck() {
    wx.navigateTo({ url: '/pages/deck/deck' });
  },

  /** 去个人中心 */
  goProfile() {
    wx.navigateTo({ url: '/pages/profile/profile' });
  }
});
