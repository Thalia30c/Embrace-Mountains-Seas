// utils/sync.js
// 断网兜底：后台不可用时的「延时提交队列」
//
// 场景：玩家一局打完，后端连不上（服务没开 / WiFi 断了 / IP 变了），
//      这时如果把这次的加减分丢了，玩家的段位分就对不上。
// 做法：先把 {uid, delta} 存进 Storage，后端一恢复就自动补传（App 启动时、上报成功时都会冲一次队列）。

const api = require('./api');

const KEY = 'pendingScores';

function list() {
  try { return wx.getStorageSync(KEY) || []; } catch (e) { return []; }
}
function save(arr) {
  try { wx.setStorageSync(KEY, arr); } catch (e) { /* 忽略 */ }
}

/** 入队：本次对局的积分变化 */
function enqueueScore(uid, delta) {
  if (!uid) return;               // 未登录不记队列（没法归属到账号）
  const arr = list();
  arr.push({ uid: uid, delta: delta, t: Date.now() });
  save(arr);
}

/** 补传：把队列里没上报成功的依次发出去；成功后清空 */
function flushPending(onDone) {
  const arr = list();
  if (!arr.length) { if (onDone) onDone(0); return; }

  const rest = arr.slice();
  const tryOne = () => {
    if (!rest.length) {
      save([]);
      if (onDone) onDone(arr.length);
      return;
    }
    const item = rest[0];
    api.scoreReport(item.uid, item.delta).then(() => {
      rest.shift();
      save(rest);
      setTimeout(tryOne, 0);
    }).catch(() => {
      save(rest);                 // 仍连不上：保留剩下的，下次再试
      if (onDone) onDone(arr.length - rest.length);
    });
  };
  tryOne();
}

/** 未上报条数（用于提示） */
function pendingCount() { return list().length; }

module.exports = { enqueueScore: enqueueScore, flushPending: flushPending, pendingCount: pendingCount };
