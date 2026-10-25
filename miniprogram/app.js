// app.js
// 小程序入口，全局状态放这里

App({
  globalData: {
    // 后端服务地址。真机预览时改成局域网 IP，不能用 localhost
    baseUrl: 'http://192.168.43.242:8080/api',
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
