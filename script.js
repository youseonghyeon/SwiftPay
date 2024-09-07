import http from 'k6/http';
import {check} from 'k6';

export let options = {
    vus: 120,  // 가상 유저 수
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    let awsSendUrl = 'http://3.36.124.139:8080/api/transfers/transfer';
    let localSendUrl = 'http://localhost:10000/api/transfers/transfer';
    let userCreateUrl = 'http://3.36.124.139:8080/api/accounts/create';

    var index = Math.floor(Math.random() * 1000);  // 고유한 인덱스 값 생성

    let sendPayload = JSON.stringify({
        senderId: 1,
        recipientId: 2,
        amount: 100,
    });

    let userCreatePayload = JSON.stringify({
        "username": "bot" + index,
        "name": "로봇" + index,
        "balance": 1000000000
    })

    let params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let res = http.post(awsSendUrl, sendPayload, params);  // POST 요청 실행

    check(res, {
        'status is 200': (r) => r.status === 200,  // 상태 코드 200 확인
    });

}
