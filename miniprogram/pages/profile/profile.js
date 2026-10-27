// pages/profile/profile.js
// 个人中心：我的信息、战绩、排行榜

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    userInfo: null,
    ranking: [],
    isLogin: false
  },

  onShow() {
    this.loadUser();
    this.loadRanking();
  },

  /** 加载用户信息 */
  loadUser() {
    if (!getAppInstance().globalData.token) {
      this.setData({ userInfo: null, isLogin: false });
      return;
    }
    this.setData({ isLogin: true });
    // 后端没有 /user/info，改用 /user/{uid}
    api.userDetail(getAppInstance().globalData.userInfo.userId).then((user) => {
      getAppInstance().globalData.userInfo = user;
      wx.setStorageSync('userInfo', user);
      this.setData({ userInfo: user });
    }).catch(() => {});
  },

  /** 加载排行榜 */
  loadRanking() {
    api.ranking(20).then((list) => {
      this.setData({ ranking: list || [] });
    }).catch(() => {});
  },

  /** 去登录 */
  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  /** 退出登录 */
  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      success: (res) => {
        if (!res.confirm) return;
        getAppInstance().clearLogin();
        wx.showToast({ title: '已退出', icon: 'success' });
        this.setData({ userInfo: null, isLogin: false });
      }
    });
  }
});
