// utils/request.js
// 统一请求封装：所有页面调接口都走这里，不要在页面里直接调 wx.request
//
// 统一处理三件事：
// 1. 自动带上登录令牌
// 2. 后端统一返回 { code, msg, data }，这里判断 code 并解析
// 3. 令牌失效（40002）时自动跳登录页

const { BASE_URL } = require('./config');

/**
 * 发起请求
 * @param {string} url    接口路径，以 / 开头，例如 '/user/login'
 * @param {string} method 请求方法，默认 GET
 * @param {object} data   请求参数
 * @param {boolean} silent 是否静默失败（不弹 toast），默认 false
 * @returns {Promise} 成功时 resolve 后端的 data 字段
 */
function request(url, method = 'GET', data = {}, silent = false) {
  return new Promise((resolve, reject) => {
    const app = getApp();
    wx.request({
      url: BASE_URL + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': app.globalData.token ? 'Bearer ' + app.globalData.token : ''
      },
      success: (res) => {
        const body = res.data;
        // 网络层异常
        if (!body || typeof body.code === 'undefined') {
          const msg = '服务器返回异常';
          if (!silent) wx.showToast({ title: msg, icon: 'none' });
          reject(new Error(msg));
          return;
        }
        // 业务成功
        if (body.code === 0) {
          resolve(body.data);
          return;
        }
        // 令牌失效，跳登录
        if (body.code === 40002) {
          app.clearLogin();
          wx.showToast({ title: '请重新登录', icon: 'none' });
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/login/login' });
          }, 800);
          reject(new Error(body.msg));
          return;
        }
        // 其他业务错误
        if (!silent) {
          wx.showToast({ title: body.msg || '操作失败', icon: 'none' });
        }
        reject(new Error(body.msg || '操作失败'));
      },
      fail: (err) => {
        if (!silent) {
          wx.showToast({ title: '网络连接失败', icon: 'none' });
        }
        reject(err);
      }
    });
  });
}

/** GET 请求 */
function get(url, params = {}, silent = false) {
  return request(url, 'GET', params, silent);
}

/** POST 请求 */
function post(url, data = {}, silent = false) {
  return request(url, 'POST', data, silent);
}

/** DELETE 请求 */
function del(url, data = {}, silent = false) {
  return request(url, 'DELETE', data, silent);
}

module.exports = { request, get, post, del, BASE_URL };
