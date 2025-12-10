import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

export let options = {
    scenarios: {
        high_load: {
            executor: 'constant-arrival-rate',
            rate: 300,
            timeUnit: '1s',
            duration: '100s',
            preAllocatedVUs: 300,
            maxVUs: 2000,
        },
    },
};

// Counters
const status200 = new Counter('status_200');
const status201 = new Counter('status_201');

// Trends for response time
const rt200 = new Trend('rt_200');
const rt201 = new Trend('rt_201');

const firstNames = ["Arjun", "Rohan", "Neha", "Asha", "Vivek", "Sita", "Kiran", "Rita"];
const cities = ["mumbai", "delhi", "noida", "bengaluru", "pune", "kolkata", "guwahati"];

function randomValue(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
    const firstname = randomValue(firstNames);
    const location = randomValue(cities);
    const url = `http://localhost:8086/api/v1/getby/firstname/location?firstname=${firstname}&location=${location}`;

    const res = http.get(url);

    // count and track response time
    if (res.status === 200) {
        status200.add(1);
        rt200.add(res.timings.duration);
    }

    if (res.status === 201) {
        status201.add(1);
        rt201.add(res.timings.duration);
    }

    check(res, {
        "status is 200/201": (r) => r.status === 200 || r.status === 201,
    });
}

// ------- Summary -------
export function handleSummary(data) {
    // extract metric values safely
    const t200 = data.metrics['status_200']?.values?.count || 0;
    const t201 = data.metrics['status_201']?.values?.count || 0;
    const total = t200 + t201;

    const pct200 = total ? ((t200 / total) * 100).toFixed(2) : 0;
    const pct201 = total ? ((t201 / total) * 100).toFixed(2) : 0;

    const p95_200 = data.metrics['rt_200']?.values['p(95)']?.toFixed(2) || "N/A";
    const p95_201 = data.metrics['rt_201']?.values['p(95)']?.toFixed(2) || "N/A";

    console.log("\n========== 📊 Response Summary ==========");
    console.log(`Total Requests: ${total}`);
    console.log(`200 OK: ${t200} (${pct200}%)`);
    console.log(`201 Created: ${t201} (${pct201}%)`);
    console.log(`p(95) latency 200: ${p95_200} ms`);
    console.log(`p(95) latency 201: ${p95_201} ms`);
    console.log("=========================================\n");

    return {
        "summary.json": JSON.stringify(data, null, 2),
    };
}
