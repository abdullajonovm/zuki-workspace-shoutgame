import { Mock } from 'app/mock/mock';
import { Subscriber } from 'app/model/subscriber';
import { LoginJson } from 'app/shared/services/subscriber.service';

//noinspection TsLint
const mockSubscriber = new Subscriber({
    nickname: 'bxgrant',
    photoUrl: 'https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/1924972_10152606029408154_2137986613_n.jpg?oh=7e23e10573900fe419e1fe967f8a12de&oe=59CED36C',
    encryptKey: 'mockEncryptKey',
    sha256Hash: 'mockSha256Hash'
});

// const loginData: LoginJson = {
//     success: true,
//     sessionKey: 'mockSessionKey',
//     subscriber: mockSubscriber.toString(),
// };
export const SUBSCRIBER_SERVICE_MOCK = new Mock([
    // {match: /loginAndGetCommonSubscriber/, data: loginData}
]);
