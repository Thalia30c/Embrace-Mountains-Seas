// pages/login/login.js
// 登录页：用户名密码登录 + 微信一键登录

const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    isRegister: false,
    nickname: ''
  },

  /** 输入框绑定 */
  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  /** 切换登录/注册 */
  toggleMode() {
    this.setData({ isRegister: !this.data.isRegister });
  },

  /** 用户名密码登录 */
  async onLogin() {
    const { username, password, isRegister, nickname } = this.data;
    if (!username || !password) {
      wx.showToast({ title: '请填写用户名和密码', icon: 'none' });
      return;
    }
    try {
      let data;
      if (isRegister) {
        data = await api.register(username, password, nickname);
      } else {
        data = await api.login(username, password);
      }
      app.setLogin(data.token, {
        userId: data.userId,
        nickname: data.nickname,
        avatar: data.avatar,
        coins: data.coins,
        rank: data.rank
      });
      wx.showToast({ title: isRegister ? '注册成功' : '登录成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 800);
    } catch (e) {
      // 错误提示已在 request.js 统一处理
    }
  },

  /** 微信一键登录 */
  async onWxLogin() {
    try {
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject
        });
      });
      const data = await api.wxLogin(loginRes.code);
      app.setLogin(data.token, {
        userId: data.userId,
        nickname: data.nickname,
        avatar: data.avatar,
        coins: data.coins,
        rank: data.rank
      });
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 800);
    } catch (e) {
      // 错误提示已在 request.js 统一处理
    }
  }
});
