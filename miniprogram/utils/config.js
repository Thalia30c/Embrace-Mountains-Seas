// utils/config.js
// 后端地址配置

/**
 * 真机预览时 localhost 指向手机自己，连不上电脑。
 * 改成电脑局域网 IP，例如：'http://192.168.1.100:8080/api'
 * 查询方式：在电脑上执行 ipconfig，看 IPv4 地址
 */
const BASE_URL = 'http://localhost:8080/api';

module.exports = {
  BASE_URL
};
