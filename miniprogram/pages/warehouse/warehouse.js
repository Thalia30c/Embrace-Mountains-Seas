// pages/warehouse/warehouse.js
// 仓库页：上半区为卡牌库（分类查找），下半区为展示区
// 选中卡牌后播放同编号视频（miniprogram/video/{id}.mp4），播完放大卡牌居中，两侧展示属性与能力；
// 若该卡牌无对应视频，则直接展示卡牌。

const api = require('../../utils/api');
const { BASE_URL } = require('../../utils/config');

const getAppInstance = () => getApp();

// 类型中文名，供 wxml 映射
const KIND_NAME = { attack: '攻击', defense: '防御', magic: '法术' };

// 本地已提供动画的视频编号（与后端 backend/src/main/resources/static/video 目录保持一致；新增动画视频时在此补充）
const VIDEO_IDS = new Set([1, 2, 3, 4, 5, 7, 10]);

Page({
  data: {
    cards: [],            // 该用户拥有的全部卡牌（去重后）
    filteredCards: [],    // 经过种类/等级筛选后用于展示的卡牌
    typeFilter: 'all',    // all / attack / defense / magic
    levelFilter: 'all',   // all / 黄 / 玄
    kindName: KIND_NAME,  // 供 wxml 通过 kindName[kind] 取中文
    loading: true,
    // 下方展示区
    selectedCard: null,   // 当前选中的卡牌
    videoSrc: '',         // 视频路径
    hasVideo: false,      // 该卡牌是否有对应视频
    videoEnded: false,    // 动画是否已播完（播完切到放大卡牌展示）
    videoError: false     // 视频是否加载失败（无对应视频）
  },

  onShow() {
    this.loadCards();
  },

  /** 进场加载：读取该用户拥有的全部卡牌详情 */
  loadCards() {
    const userInfo = getAppInstance().globalData.userInfo;
    if (!userInfo || !userInfo.userId) {
      this.setData({ loading: false });
      return;
    }
    api.ownedCards(userInfo.userId)
      .then((data) => {
        // 等级类名需为 ASCII（WXSS 不支持中文类名），这里提前映射好
        const cards = (data || []).map((c) =>
          Object.assign({}, c, {
            lvClass: c.cLevel === '黄' ? 'huang' : c.cLevel === '玄' ? 'xuan' : 'other'
          })
        );
        this.setData({ cards, loading: false });
        this.applyFilter();
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  /** 按种类筛选 */
  onTypeFilter(e) {
    this.setData({ typeFilter: e.currentTarget.dataset.type });
    this.applyFilter();
  },

  /** 按等级筛选 */
  onLevelFilter(e) {
    this.setData({ levelFilter: e.currentTarget.dataset.level });
    this.applyFilter();
  },

  /** 根据两个筛选条件计算展示列表 */
  applyFilter() {
    const { cards, typeFilter, levelFilter } = this.data;
    const filtered = cards.filter((c) => {
      const okType = typeFilter === 'all' || c.kind === typeFilter;
      const okLevel = levelFilter === 'all' || c.cLevel === levelFilter;
      return okType && okLevel;
    });
    this.setData({ filteredCards: filtered });
  },

  /** 选中上方卡牌 -> 下方展示区播放动画 */
  onSelectCard(e) {
    const index = e.currentTarget.dataset.index;
    const card = this.data.filteredCards[index];
    if (!card) return;
    const id = Number(card.card);
    const hasVideo = VIDEO_IDS.has(id);
    this.setData({
      selectedCard: card,
      videoSrc: BASE_URL + '/video/' + id + '.mp4',
      hasVideo,
      videoEnded: false,
      videoError: false
    });
    // 有视频时确保从头播放
    if (hasVideo) {
      const that = this;
      setTimeout(() => {
        const vc = wx.createVideoContext('whVideo', that);
        if (vc) vc.play();
      }, 300);
    }
  },

  /** 视频元数据就绪后强制播放（兜底自动播放被拦截的情况） */
  onVideoMeta() {
    const vc = wx.createVideoContext('whVideo', this);
    if (vc) {
      vc.play();
      // 尝试进入原生全屏（盖住顶部导航栏）；若系统因手势限制拒绝，CSS 仍会铺满页面区域
      try { vc.requestFullScreen({ direction: 0 }); } catch (e) {}
    }
  },

  /** 动画播放结束 -> 放大卡牌并展示属性/能力 */
  onVideoEnded() {
    // 视频元素卸载后系统会自动退出原生全屏，无需手动 exitFullScreen
    this.setData({ videoEnded: true });
  },

  /** 视频加载失败（无对应视频）-> 直接展示卡牌 */
  onVideoError() {
    this.setData({ hasVideo: false, videoEnded: true, videoError: true });
  },

  /** 重播动画（视频播完后再次播放） */
  onReplay() {
    this.setData({ videoEnded: false });
    const that = this;
    setTimeout(() => {
      const vc = wx.createVideoContext('whVideo', that);
      if (vc) {
        vc.seek(0);
        vc.play();
        try { vc.requestFullScreen({ direction: 0 }); } catch (e) {}
      }
    }, 100);
  }
});
