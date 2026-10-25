// pages/room/room.js
// 房间页：等待其他玩家、房主开局

const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    roomId: null,
    room: null,
    isOwner: false,
    canStart: false
  },

  onLoad(options) {
    this.setData({ roomId: options.id });
  },

  onShow() {
    this.loadRoom();
  },

  /** 拉取房间信息 */
  loadRoom() {
    const roomId = this.data.roomId;
    api.roomDetail(roomId).then((room) => {
      const myId = app.globalData.userInfo ? app.globalData.userInfo.userId : null;
      const isOwner = myId !== null && room.ownerId === myId;
      this.setData({
        room: room,
        isOwner: isOwner,
        canStart: isOwner && room.currentPlayer >= 2
      });
      wx.setNavigationBarTitle({ title: room.name || '房间' });
    }).catch(() => {});
  },

  /** 房主开始对局 */
  async onStart() {
    const battleId = await api.roomStart(this.data.roomId);
    wx.redirectTo({ url: '/pages/battle/battle?id=' + battleId });
  },

  /** 离开房间 */
  async onLeave() {
    await api.roomLeave(this.data.roomId);
    wx.navigateBack();
  },

  /** 复制房间号 */
  onCopy() {
    wx.setClipboardData({
      data: this.data.room.roomNo,
      success: () => wx.showToast({ title: '房间号已复制', icon: 'success' })
    });
  }
});
