// pages/hall/hall.js
// 对战大厅：房间列表、创建房间、输入房间号加入

const api = require('../../utils/api');

// 惰性获取 App 实例，避免模块加载时 getApp() 尚未就绪
const getAppInstance = () => getApp();

Page({
  data: {
    rooms: [],
    roomNo: '',
    password: '',
    showCreate: false,
    createForm: {
      name: '',
      mode: 'casual',
      maxPlayer: 2
    },
    modeOptions: [
      { value: 'casual', label: '休闲模式' },
      { value: 'rank', label: '排位模式' }
    ],
    modeIndex: 0
  },

  onShow() {
    this.loadRooms();
  },

  /** 拉取大厅房间列表 */
  loadRooms() {
    api.roomList().then((rooms) => {
      this.setData({ rooms: rooms || [] });
    }).catch(() => {});
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  /** 创建房间表单绑定 */
  onCreateInput(e) {
    this.setData({ ['createForm.' + e.currentTarget.dataset.field]: e.detail.value });
  },

  /** 选择模式 */
  onModeChange(e) {
    const index = e.detail.value;
    this.setData({
      modeIndex: index,
      'createForm.mode': this.data.modeOptions[index].value
    });
  },

  /** 弹出创建房间面板 */
  openCreate() {
    if (!getAppInstance().globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    this.setData({ showCreate: true });
  },

  closeCreate() {
    this.setData({ showCreate: false });
  },

  /** 提交创建房间 */
  async submitCreate() {
    const form = this.data.createForm;
    const room = await api.roomCreate({
      name: form.name,
      mode: form.mode,
      maxPlayer: Number(form.maxPlayer) || 2
    });
    wx.showToast({ title: '房间已创建', icon: 'success' });
    this.setData({ showCreate: false, 'createForm.name': '' });
    setTimeout(() => {
      wx.navigateTo({ url: '/pages/room/room?id=' + room.id });
    }, 800);
  },

  /** 加入房间 */
  async joinRoom() {
    const { roomNo, password } = this.data;
    if (!roomNo) {
      wx.showToast({ title: '请输入房间号', icon: 'none' });
      return;
    }
    const room = await api.roomJoin(roomNo, password);
    wx.showToast({ title: '加入成功', icon: 'success' });
    this.setData({ roomNo: '', password: '' });
    setTimeout(() => {
      wx.navigateTo({ url: '/pages/room/room?id=' + room.id });
    }, 800);
  },

  /** 点击列表里的房间直接加入 */
  async quickJoin(e) {
    const roomNo = e.currentTarget.dataset.roomno;
    const room = await api.roomJoin(roomNo);
    wx.navigateTo({ url: '/pages/room/room?id=' + room.id });
  },

  /** 阻止弹层内部点击冒泡到遮罩 */
  noop() {}
});
