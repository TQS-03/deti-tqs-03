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

const userUrl = 'http://localhost:8080/backend/auth/register';

export function setup() {
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

  const userRes = http.post(userUrl, userPayload, userParams);

  console.log(userRes.body)

  check(userRes, {
    'user create → status 200': (r) => r.status === 200 || r.status === 201,
    'user create → has userId': (r) => {
      try {
        const body = r.json();
        return body.id !== undefined || body.userId !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  let userId = null;
  try {
    const body = userRes.json();
    // if your backend returns { "id": "...", ... }, pick whichever key applies:
    userId = body.id || body.userId || null;
  } catch (e) {
    // leave userId as null if parsing fails
  }

  if (!userId) {
    throw new Error('setup(): user creation failed, no userId returned');
  }

  return { userId };
}

export default function (data) {
  const createdUserId = data.userId;

  const payload = JSON.stringify({
    name: `Station-${Math.random().toString(36).substring(7)}`,
    address: '123 Load Test Blvd',
    maxOccupation: 10,
    currentOccupation: 0,
    latitude: '40.7128',
    longitude: '74.0060',
    pricePerKWh: 2.00,
    personId: createdUserId,
    chargerTypes: ['CCS', 'TYPE 2']
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(baseUrl, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has id': (r) => JSON.parse(r.body).id !== undefined,
  });

  sleep(0.2); // simulate a pause between requests
}
