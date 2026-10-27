// pages/index/index.js
// 首页：入口导航 + 玩家信息展示

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

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
    if (!getAppInstance().globalData.token) {
      this.setData({ userInfo: null, isLogin: false });
      // 未登录（含启动首屏直接进 index 的情况）跳转到登录页
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.setData({ isLogin: true, userInfo: getAppInstance().globalData.userInfo });
    api.userInfo().then((user) => {
      getAppInstance().globalData.userInfo = user;
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
