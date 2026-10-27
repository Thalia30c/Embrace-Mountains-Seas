// pages/login/login.js
// 登录页：用户名密码登录 + 微信一键登录

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    username: '',
    password: '',
    isRegister: false,
    nickname: ''
  },

  onLoad() {
    // 已经登录过（本地有 token），直接进入首页，跳过登录页
    if (getAppInstance().globalData.token) {
      wx.reLaunch({ url: '/pages/index/index' });
    }
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
      getAppInstance().setLogin(this.buildToken(data), this.buildUserInfo(data));
      wx.showToast({ title: isRegister ? '注册成功' : '登录成功', icon: 'success' });
      setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 800);
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
      getAppInstance().setLogin(this.buildToken(data), this.buildUserInfo(data));
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 800);
    } catch (e) {
      // 错误提示已在 request.js 统一处理
    }
  },

  /**
   * 后端当前不返回 token，这里兜底生成一个本地令牌，
   * 否则 globalData.token 为空，一进首页就会被判定为未登录踢回来
   */
  buildToken(data) {
    return data.token || 'local-' + (data.userId || Date.now());
  },

  /** 保存后端带回的完整用户信息，总览页要用 warehouseId / score / gameLevel */
  buildUserInfo(data) {
    return {
      userId: data.userId,
      userAccount: data.userAccount,
      nickname: data.nickname || data.userAccount || '玩家',
      avatar: data.avatar || '',
      rank: data.rank || '',
      score: data.score || 0,
      gameLevel: data.gameLevel || 1,
      warehouseId: data.warehouseId || 0
    };
  }
});
