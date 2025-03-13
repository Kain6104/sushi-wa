import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  vus: 1000, // 1000 người dùng đồng thời
  duration: '30s', // Thời gian kiểm tra
};

export default function () {
  http.get('http://192.168.170.247:8080'); // URL ứng dụng
  sleep(1);
}
