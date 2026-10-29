// pages/profile/profile.js
// 个人中心：昵称/账号/金币/段位展示 + 功能按钮 + 退出登录

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    nickname: '',
    userAccount: '',
    avatarChar: '客',
    stats: [],
    row1: [
      { name: '活动', icon: '活' },
      { name: '兑换码', icon: '兑' },
      { name: '设置', icon: '设' },
      { name: '服饰', icon: '饰' }
    ],
    row2: [
      { name: '客服', icon: '客' },
      { name: '用户协议', icon: '约' },
      { name: '隐私条款', icon: '隐' }
    ],
    showSoon: false,
    soonName: ''
  },

  onShow() {
    this.checkLogin();
  },

  onHide() {
    this.clearSoonTimer();
  },

  onUnload() {
    this.clearSoonTimer();
  },

  /**
   * 登录数据缺失直接回登录页；
   * 有数据则先用本地缓存渲染，再拉数据库最新值
   */
  checkLogin() {
    const app = getAppInstance();
    const { token, userInfo } = app.globalData;
    if (!token || !userInfo || !userInfo.userId) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.applyUserInfo(userInfo);
    api.userInfo(true).then((user) => {
      if (!user || !user.userId) {
        this.backToLogin();
        return;
      }
      app.globalData.userInfo = user;
      wx.setStorageSync('userInfo', user);
      this.applyUserInfo(user);
    }).catch(() => {
      // 令牌失效（40002）已在 request.js 中统一清登录态并跳登录页
    });
  },

  /** 用用户信息填充页面 */
  applyUserInfo(u) {
    const nickname = u.nickname || u.userAccount || '旅人';
    this.setData({
      nickname,
      userAccount: u.userAccount || '',
      avatarChar: nickname.charAt(0),
      stats: [
        { key: 'coins', name: '金币', icon: '金', value: u.coins != null ? u.coins : (u.score != null ? u.score : 0) },
        // 胜场 / 败场暂未实现，先占位
        { key: 'win', name: '胜场', icon: '胜', value: '--' },
        { key: 'lose', name: '败场', icon: '败', value: '--' },
        { key: 'rank', name: '段位', icon: '段', value: u.rank || '未定' }
      ]
    });
  },

  /** 功能按钮：弹出“即将开放”提示，1.5 秒后自动消失 */
  onFuncTap(e) {
    const name = e.currentTarget.dataset.name;
    this.clearSoonTimer();
    this.setData({ showSoon: true, soonName: name });
    this._soonTimer = setTimeout(() => {
      this.setData({ showSoon: false });
      this._soonTimer = null;
    }, 1500);
  },

  /** 点击遮罩立即关闭弹窗 */
  hideSoon() {
    this.clearSoonTimer();
    this.setData({ showSoon: false });
  },

  clearSoonTimer() {
    if (this._soonTimer) {
      clearTimeout(this._soonTimer);
      this._soonTimer = null;
    }
  },

  /** 退出登录：确认后清除登录态并返回登录页 */
  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      confirmColor: '#7c3f16',
      success: (res) => {
        if (!res.confirm) return;
        getAppInstance().clearLogin();
        wx.reLaunch({ url: '/pages/login/login' });
      }
    });
  },

  /** 登录数据异常时清除登录态回到登录页 */
  backToLogin() {
    getAppInstance().clearLogin();
    wx.reLaunch({ url: '/pages/login/login' });
  }
});
