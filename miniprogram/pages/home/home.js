// pages/home/home.js
// 功能展示页：登录后主菜单，各功能入口的中转界面

const getAppInstance = () => getApp();

Page({
  data: {
    nickname: '玩家',
    rank: '',
    score: 0,
    items: [
      { key: 'battle', label: '战 斗', desc: '单机三局 · 选牌换牌 · 擂台对决', main: true, url: '/pages/index/index' },
      { key: 'hall', label: '对战大厅', desc: '创建/加入房间', url: '/pages/hall/hall' },
      { key: 'warehouse', label: '卡牌仓库', desc: '查看已有卡牌', url: '/pages/warehouse/warehouse' },
      { key: 'deck', label: '卡组配置', desc: '编辑我的卡组', url: '/pages/deck/deck' },
      { key: 'profile', label: '个人战绩', desc: '段位与积分', url: '/pages/profile/profile' }
    ]
  },

  onShow() {
    const app = getAppInstance();
    const u = (app && app.globalData && app.globalData.userInfo) || wx.getStorageSync('userInfo') || {};
    this.setData({
      nickname: u.nickname || u.userAccount || '玩家',
      rank: u.rank || '青铜',
      score: u.score || 0
    });
  },

  /** 进入某个功能页面 */
  go(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) return;
    wx.navigateTo({ url });
  },

  /** 退出登录，回到登录页 */
  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      success: (res) => {
        if (!res.confirm) return;
        const app = getAppInstance();
        if (app && app.clearLogin) app.clearLogin();
        wx.reLaunch({ url: '/pages/login/login' });
      }
    });
  }
});
