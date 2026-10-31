// utils/config.js
// 后端地址配置
//
// 背景：真机预览时小程序跑在手机上，localhost 指向手机自己，必须换成电脑的局域网 IP。
//      团队多人开发时 IP 各不相同，手工改来改去很容易出错，所以这里改成「自动探测」：
//      启动时按顺序探测候选主机，第一个能通的就用它，并记住下次直接用（存 Storage）。

const PORT = 8080;
const API_PREFIX = '/api';

/* 候选主机：模拟器用 127.0.0.1；其余为局域网 IP（换成你自己的可继续往后加） */
const HOSTS = [
  '127.0.0.1',            // 开发者工具模拟器
  '192.168.110.245',      // 本机 WLAN 局域网 IP（真机预览时手机连同一 WiFi）
  '192.168.56.1',         // 本机以太网/VirtualBox 网段
  '192.168.110.176'       // 同伴电脑（历史注释里留下的）
];

function buildUrl(host) { return 'http://' + host + ':' + PORT + API_PREFIX; }

/* 当前生效地址：优先用上次探测成功的，其次默认本机 */
let baseUrl = buildUrl(HOSTS[0]);
try {
  const saved = wx.getStorageSync('apiHost');
  if (saved) baseUrl = buildUrl(saved);
} catch (e) { /* 忽略 */ }

function getBaseUrl() { return baseUrl; }

/** 逐个探测候选地址：找到可用的就切换过去并记住；全失败则保持原地址 */
function detectBackend() {
  return new Promise((resolve) => {
    const order = [];
    try {
      const saved = wx.getStorageSync('apiHost');
      if (saved) order.push(saved);   // 上次成功的先试，最快
    } catch (e) { /* 忽略 */ }
    HOSTS.forEach(h => { if (order.indexOf(h) < 0) order.push(h); });

    let i = 0;
    const tryNext = () => {
      if (i >= order.length) { resolve(false); return; }
      const host = order[i++];
      wx.request({
        url: buildUrl(host) + '/user/ranking?limit=1',
        method: 'GET',
        timeout: 4000,
        success: (res) => {
          if (res && res.data && res.data.code === 0) {
            baseUrl = buildUrl(host);
            try { wx.setStorageSync('apiHost', host); } catch (e) { /* 忽略 */ }
            console.log('[后端已连接]', baseUrl);
            resolve(true);
          } else {
            tryNext();
          }
        },
        fail: () => tryNext()
      });
    };
    tryNext();
  });
}

module.exports = {
  getBaseUrl: getBaseUrl,
  setBaseUrl: function (host) { baseUrl = buildUrl(host); },
  detectBackend: detectBackend,
  PORT: PORT,
  HOSTS: HOSTS,
  BASE_URL: baseUrl
};
