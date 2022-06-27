import {Injectable} from '@angular/core';
import {Subscriber} from 'app/model/subscriber';
import {environment} from 'environments/environment';

@Injectable()
export class LogService {
    subscriber: Subscriber;

    log(message: any, ...toLog: any[]): void {
        console.log(message, ...toLog);
        this.sendMessage('log', [message, ...toLog]);
    }

    error(message: any, ...toLog: any[]): void {
        console.error(message, ...toLog);
        this.sendMessage('error', [message, ...toLog]);
    }

    info(message: any, ...toLog: any[]): void {
        console.info(message, ...toLog);
        this.sendMessage('info', [message, ...toLog]);
    }

    private sendMessage(logType: string, toLog: any[]): void {
        if (!environment.production)
            return;

        const result: any = {};
        let outStr = '';
        let objNumber = 1;
        for (const item of toLog) {
            const type = typeof item;
            if (type !== 'function' && type !== 'object') {
                outStr += ` ${item}`;
            } else {
                outStr += ' ${_l' + objNumber + '}';
                result['_l' + objNumber] = item;
                objNumber++;
            }
        }

        result.message = outStr.trim().replace(/\s\s/g, ' ');
        if (this.subscriber) {
            const subscriber = this.subscriber;
            result.sub = `${subscriber.nickname} - ${subscriber.email} - id: ${subscriber.subscriberId}`;
        }
        result.type = logType;
        _LTracker.push(result);
    }
}
