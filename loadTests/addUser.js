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
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<4000'],
  },
};

const baseUrl = 'http://localhost:8080/backend/auth/register';

export default function (data) {
  const uniqueEmail = `loadtest_user_${Math.random().toString(36).substring(7)}@example.com`;

  const userPayload = JSON.stringify({
    firstName: 'Load',
    lastName: 'Tester',
    email: uniqueEmail,
    password: 'Passw0rd!',
    isWorker: true
  });

  const userParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(baseUrl, userPayload, userParams);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has id': (r) => JSON.parse(r.body).id !== undefined,
  });

  sleep(0.2); // simulate a pause between requests
}
