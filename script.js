import http from 'k6/http';
import {check} from 'k6';

export let options = {
    vus: 700,  // 가상 유저 수
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    let url = 'http://3.36.124.139:8080/api/transfers/transfer';

    let payload = JSON.stringify({
        senderId: 1,
        recipientId: 2,
        amount: 1000,
    });

    let params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let res = http.post(url, payload, params);  // POST 요청 실행

    check(res, {
        'status is 200': (r) => r.status === 200,  // 상태 코드 200 확인
    });

}
