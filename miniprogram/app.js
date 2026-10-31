// app.js
// 小程序入口，全局状态放这里

const { BASE_URL } = require('./utils/config');

// 全局登录守卫：除登录页外，任意页面 onShow 时若发现未登录（无 userInfo），自动跳转登录页
const _originPage = Page;
Page = function (options) {
  const originOnShow = options.onShow;
  options.onShow = function () {
    const pages = getCurrentPages();
    const route = pages.length ? pages[pages.length - 1].route : '';
    const isLoginPage = route.indexOf('pages/login/login') !== -1;
    const app = getApp();
    if (!isLoginPage && !(app && app.globalData.userInfo)) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    if (originOnShow) {
      originOnShow.call(this);
    }
  };
  return _originPage(options);
};

App({
  globalData: {
    // 后端服务地址，与 utils/config.js 保持一致（request.js 实际用的是 config）
    baseUrl: BASE_URL,
    // 登录令牌
    token: '',
    // 当前登录用户信息
    userInfo: null
  },

  onLaunch() {
    // 启动时把本地缓存的 token 和用户信息读回来，免去重复登录
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo || null;
    }
  },

  /** 全局脚本错误：屏幕上直接弹出（真机上没有控制台，先让错误可见） */
  onError(msg) {
    console.error('[全局脚本错误]', msg);
    try {
      wx.showToast({ title: '脚本错误：' + String(msg).slice(0, 60), icon: 'none', duration: 8000 });
    } catch (e) { /* 忽略 */ }
  },

  /** 未捕获的 Promise 异常：同样弹出来 */
  onUnhandledRejection(res) {
    console.error('[未处理异常]', res && res.reason);
    try {
      const msg = res && res.reason && res.reason.message ? res.reason.message : String(res && res.reason);
      wx.showToast({ title: '异常：' + String(msg).slice(0, 60), icon: 'none', duration: 8000 });
    } catch (e) { /* 忽略 */ }
  },

  /** 保存登录态 */
  setLogin(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },

  /** 清除登录态 */
  clearLogin() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  }
});
