import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '10s', target: 50 },
    { duration: '30s', target: 250 },
    { duration: '10s', target: 500 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],         // < 1% failures
    http_req_duration: ['p(95)<500'],       // 95% of requests < 500ms
  },
};

const baseUrl = 'http://localhost:8080/backend/station';

export default function () {
  const payload = JSON.stringify({
    name: `Station-${Math.random().toString(36).substring(7)}`,
    address: '123 Load Test Blvd',
    maxOccupation: 10,
    currentOccupation: 0,
    latitude: '40.7128',
    longitude: '74.0060',
    chargerTypes: ['CCS', 'TYPE 2']
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(baseUrl, payload, params);
  console.log(payload);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has id': (r) => JSON.parse(r.body).id !== undefined,
  });

  sleep(0.2); // simulate a pause between requests
}
