// pages/deck/deck.js
// 牌组页：四组卡组（进攻/防御/法术/备选）的放置与锁卡

const api = require('../../utils/api');

// 惰性获取 App 实例
const getAppInstance = () => getApp();

// 四组卡组定义（与后端 warehouse 表的 Attack/Defense/magic/backup 一一对应）
const GROUP_DEFS = [
  { key: 'attack', label: '进攻卡组' },
  { key: 'defense', label: '防御卡组' },
  { key: 'magic', label: '法术卡组' },
  { key: 'backup', label: '备选卡组' }
];

// 每组固定 3 个卡槽
const SLOT_COUNT = 3;

// 把后端返回的编号列表补齐成长度为 SLOT_COUNT 的数组，空位用 0 表示
function padSlots(list) {
  const arr = (list || []).slice(0, SLOT_COUNT);
  while (arr.length < SLOT_COUNT) arr.push(0);
  return arr;
}

// 根据卡牌类型与卡槽分组判断是否允许放置：
// 进攻组收攻击牌、防御组收防御牌、法术组收法术牌；复合牌(compound)可额外放入法术组；备选组不受限制
function canPlace(kind, compound, groupKey) {
  if (groupKey === 'backup') return true;
  if (groupKey === 'magic') return kind === 'magic' || compound === true;
  if (groupKey === 'attack') return kind === 'attack';
  if (groupKey === 'defense') return kind === 'defense';
  return false;
}

// 放置被拒绝时，提示卡牌属性（复合牌额外标注「复合」）
function placeFailMsg(kind, compound) {
  const names = { attack: '攻击', defense: '防御', magic: '法术' };
  const typeName = names[kind] + (compound ? '（复合）' : '');
  let msg = '该卡为【' + typeName + '】牌，';
  if (compound) {
    msg += '可放入' + names[kind] + '卡组或法术卡组';
  } else {
    msg += '仅可放入' + names[kind] + '卡组';
  }
  return msg;
}

Page({
  data: {
    // 四组卡组：每组 slots 为卡槽中的卡牌编号（0 表示空），slotPool 记录该卡来自底池的哪个下标
    groups: [],
    // 底池：仓库全部卡牌，placed=true 表示已被放到上方卡槽
    pool: [],
    // 当前选中的底池卡牌下标，-1 表示未选中
    selected: -1,
    // 底池为空/全放置时的提示语
    poolHint: '',
    // 古风确认弹窗状态
    modal: { show: false, title: '', content: '', confirmText: '确定', cancelText: '取消', danger: false, action: '' }
  },

  onShow() {
    this.loadDeck();
  },

  /** 跳转仓库页 */
  goWarehouse() {
    wx.navigateTo({ url: '/pages/warehouse/warehouse' });
  },

  /** 进场加载：读取当前用户卡组编队，复原上次锁卡后的状态 */
  loadDeck() {
    const userInfo = getAppInstance().globalData.userInfo;
    if (!userInfo || !userInfo.userId) {
      return;
    }
    api.deckLoad(userInfo.userId).then((data) => {
      const cards = (data && data.cardList) || [];
      const pool = cards.map((c) => ({
        card: c.card,
        kind: c.kind,
        compound: c.compound,
        placed: false
      }));

      // 用四组已保存的编号初始化卡槽，并顺次消耗底池中对应编号的卡牌
      const saved = [
        (data && data.attack) || [],
        (data && data.defense) || [],
        (data && data.magic) || [],
        (data && data.backup) || []
      ];
      const groups = GROUP_DEFS.map((def, i) => {
        const list = padSlots(saved[i]);
        const slots = list.slice();
        const slotPool = [-1, -1, -1];
        list.forEach((card, si) => {
          if (card > 0) {
            const idx = pool.findIndex((p) => !p.placed && p.card === card);
            if (idx >= 0) {
              pool[idx].placed = true;
              slotPool[si] = idx;
            }
          }
        });
        return { key: def.key, label: def.label, slots, slotPool };
      });

      this.setData({ groups, pool, selected: -1, poolHint: this.buildHint(pool) });
    }).catch(() => {});
  },

  /** 计算底池提示语 */
  buildHint(pool) {
    if (!pool || pool.length === 0) return '暂无卡牌，快去对战收集吧';
    if (pool.every((p) => p.placed)) return '卡牌已全部放置';
    return '';
  },

  /** 点击底池卡牌：选中/取消选中 */
  onSelectPool(e) {
    const pidx = Number(e.currentTarget.dataset.pidx);
    if (this.data.pool[pidx].placed) return;
    this.setData({ selected: this.data.selected === pidx ? -1 : pidx });
  },

  /** 点击上方卡槽：空槽则放入选中卡牌，已放则取消并退回底池 */
  onTapSlot(e) {
    const group = e.currentTarget.dataset.group;
    const si = Number(e.currentTarget.dataset.slot);
    const gi = this.data.groups.findIndex((g) => g.key === group);
    if (gi < 0) return;
    const groups = this.data.groups.slice();
    const grp = Object.assign({}, groups[gi]);
    grp.slots = grp.slots.slice();
    grp.slotPool = grp.slotPool.slice();
    const pool = this.data.pool.slice();

    if (grp.slots[si] === 0) {
      // 空槽：把当前选中的底池卡牌放进来
      if (this.data.selected < 0) {
        wx.showToast({ title: '请先选择下方卡牌', icon: 'none' });
        return;
      }
      const pidx = this.data.selected;
      const sel = pool[pidx];
      if (!canPlace(sel.kind, sel.compound, group)) {
        this.showAttrModal(placeFailMsg(sel.kind, sel.compound));
        return;
      }
      if (sel.placed) return;
      grp.slots[si] = sel.card;
      grp.slotPool[si] = pidx;
      pool[pidx] = Object.assign({}, pool[pidx], { placed: true });
      groups[gi] = grp;
      this.setData({ groups, pool, selected: -1, poolHint: this.buildHint(pool) });
    } else {
      // 已放置：取消，卡牌退回底池原位置
      const pidx = grp.slotPool[si];
      if (pidx >= 0 && pool[pidx]) {
        pool[pidx] = Object.assign({}, pool[pidx], { placed: false });
      }
      grp.slots[si] = 0;
      grp.slotPool[si] = -1;
      groups[gi] = grp;
      this.setData({ groups, pool, poolHint: this.buildHint(pool) });
    }
  },

  /** 锁卡：四组卡槽全部放置才允许提交 */
  /** 弹出古风确认窗 */
  showModal(opts) {
    this.setData({
      modal: Object.assign(
        { show: true, title: '', content: '', confirmText: '确定', cancelText: '取消', danger: false, action: '' },
        opts
      )
    });
  },
  /** 弹出仅含“知道了”的信息窗（用于卡牌属性不符提示） */
  showAttrModal(content) {
    this.showModal({
      title: '卡牌属性不符',
      content: content,
      confirmText: '知道了',
      cancelText: '',
      action: 'info'
    });
  },
  hideModal() {
    this.setData({ 'modal.show': false });
  },
  onModalCancel() {
    this.hideModal();
  },
  onModalConfirm() {
    const action = this.data.modal.action;
    this.hideModal();
    if (action === 'clear') {
      this.submitClear();
    } else if (action === 'lock') {
      this.submitLock();
    }
  },
  noop() {},

  onLock() {
    const groups = this.data.groups;
    for (let i = 0; i < groups.length; i++) {
      for (let j = 0; j < groups[i].slots.length; j++) {
        if (groups[i].slots[j] === 0) {
          wx.showToast({ title: '请补上空的召唤位', icon: 'none' });
          return;
        }
      }
    }
    const userInfo = getAppInstance().globalData.userInfo;
    if (!userInfo || !userInfo.userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    this.showModal({
      title: '封印卡组',
      content: '锁卡后编队将铭刻于卷，确定提交？',
      confirmText: '锁卡',
      cancelText: '再想想',
      action: 'lock'
    });
  },

  /** 真正提交锁卡 */
  submitLock() {
    const groups = this.data.groups;
    const userInfo = getAppInstance().globalData.userInfo;
    const payload = {
      uid: userInfo.userId,
      attack: groups[0].slots,
      defense: groups[1].slots,
      magic: groups[2].slots,
      backup: groups[3].slots
    };
    wx.showLoading({ title: '锁卡中' });
    api.deckLock(payload)
      .then(() => {
        wx.hideLoading();
        wx.showToast({ title: '锁卡成功', icon: 'success' });
      })
      .catch(() => {
        wx.hideLoading();
      });
  },

  /** 点击清空：先弹二次确认 */
  onClear() {
    this.showModal({
      title: '撤去编队',
      content: '将清空进攻/防御/法术三组卡槽并写回仓库（备选组保留），确定吗？',
      confirmText: '清空',
      cancelText: '取消',
      danger: true,
      action: 'clear'
    });
  },

  /** 真正提交清空：仅清空 Attack/Defense/magic 三列，备选组原样保留写回 DB */
  submitClear() {
    const groups = this.data.groups;
    const userInfo = getAppInstance().globalData.userInfo;
    const payload = {
      uid: userInfo.userId,
      attack: [],
      defense: [],
      magic: [],
      backup: groups[3].slots
    };
    wx.showLoading({ title: '清空中' });
    api.deckLock(payload)
      .then(() => {
        wx.hideLoading();
        this.doClear();
      })
      .catch(() => {
        wx.hideLoading();
      });
  },

  /** 执行清空：复位进攻/防御/法术三组卡槽，卡牌退回底池；备选组保持不动 */
  doClear() {
    const groups = this.data.groups.slice();
    const pool = this.data.pool.slice();
    // 仅前三个分组（进攻/防御/法术）复位，备选组（下标 3）保留
    for (let gi = 0; gi < 3; gi++) {
      const grp = Object.assign({}, groups[gi]);
      grp.slots = grp.slots.slice();
      grp.slotPool = grp.slotPool.slice();
      grp.slots.forEach((card, si) => {
        const pidx = grp.slotPool[si];
        if (pidx >= 0 && pool[pidx]) {
          pool[pidx] = Object.assign({}, pool[pidx], { placed: false });
        }
        grp.slots[si] = 0;
        grp.slotPool[si] = -1;
      });
      groups[gi] = grp;
    }
    this.setData({ groups, pool, selected: -1, poolHint: this.buildHint(pool) });
    wx.showToast({ title: '已清空主卡组', icon: 'none' });
  }
});
