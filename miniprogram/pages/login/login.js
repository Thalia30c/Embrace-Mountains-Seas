// pages/login/login.js
// 登录页：账号密码登录 + 微信一键登录
// 字段与 user 表对应：user_account（账号）、user_pwd（密码）、username（用户名/昵称）

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

// 输入字符类型约束（与 user 表字段一致）
const ACCOUNT_RE = /^[A-Za-z0-9_]*$/; // user_account varchar(32)
const PWD_RE = /^[A-Za-z0-9]*$/;     // user_pwd varchar(64)
const MIN_PWD_LEN = 6;               // 密码最短位数

Page({
  data: {
    userAccount: '', // 账号
    userPwd: '',     // 密码
    nickname: '',    // 用户名（注册时填写）
    minPwdLen: MIN_PWD_LEN,
    showPwd: false,
    isRegister: false,
    inputError: false
  },

  onLoad() {
    // 已经登录过（本地有 token），直接进入首页，跳过登录页
    if (getAppInstance().globalData.token) {
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  onUnload() {
    if (this._errTimer) {
      clearTimeout(this._errTimer);
      this._errTimer = null;
    }
  },

  /** 账号输入：出现非法字符立即重置页面并提示 */
  onAccountInput(e) {
    const value = e.detail.value;
    if (!ACCOUNT_RE.test(value)) {
      this.handleInputError();
      return;
    }
    this.setData({ userAccount: value });
  },

  /** 密码输入：出现非法字符立即重置页面并提示 */
  onPwdInput(e) {
    const value = e.detail.value;
    if (!PWD_RE.test(value)) {
      this.handleInputError();
      return;
    }
    this.setData({ userPwd: value });
  },

  /** 用户名（昵称）允许中文，只做长度限制 */
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  /** 输入错误：提示 + 清空重填（刷新页面状态）+ 背景转浅红棕色 */
  handleInputError() {
    wx.showToast({ title: '输入错误，请重新输入', icon: 'none', duration: 2000 });

    if (this._errTimer) {
      clearTimeout(this._errTimer);
    }

    // 先清空所有输入（页面刷新），再置为错误态，保证抖动动画与红棕背景每次都能触发
    this.setData(
      { userAccount: '', userPwd: '', nickname: '', showPwd: false, inputError: false },
      () => this.setData({ inputError: true })
    );

    this._errTimer = setTimeout(() => {
      this.setData({ inputError: false });
      this._errTimer = null;
    }, 2600);
  },

  /** 密码失焦：长度不足时提示最短位数 */
  onPwdBlur() {
    const len = (this.data.userPwd || '').length;
    if (len > 0 && len < MIN_PWD_LEN) {
      wx.showToast({ title: `密码至少 ${MIN_PWD_LEN} 位`, icon: 'none' });
    }
  },

  /** 明文 / 密文切换 */
  togglePwdShow() {
    this.setData({ showPwd: !this.data.showPwd });
  },

  /** 切换登录/注册 */
  toggleMode() {
    this.setData({ isRegister: !this.data.isRegister, inputError: false });
  },

  /** 账号密码登录 */
  async onLogin() {
    const { userAccount, userPwd, isRegister, nickname } = this.data;
    if (!userAccount || !userPwd) {
      wx.showToast({ title: '请填写账号和密码', icon: 'none' });
      return;
    }
    if (userPwd.length < MIN_PWD_LEN) {
      wx.showToast({ title: `密码至少 ${MIN_PWD_LEN} 位`, icon: 'none' });
      return;
    }
    try {
      let data;
      if (isRegister) {
        data = await api.register(userAccount, userPwd, nickname);
      } else {
        data = await api.login(userAccount, userPwd);
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
