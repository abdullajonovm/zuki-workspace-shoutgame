import { HttpService, ShoutResponse } from 'app/shared/services/http.service';
import { Injectable } from '@angular/core';
import { Category } from 'app/model/category';

import { VxDialog } from 'vx-components';
import {Util} from '../../util';
import {AffiliatePlan} from '../../model/affiliate-plan';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable()
export class AffiliateService {


    constructor(private httpService: HttpService, private dialog: VxDialog) {
    }

    setAffiliatePlan(plan: AffiliatePlan): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/affiliateplan/add', plan.toJsonObject());
    }

    getCurrentAffiliatePlan(): Observable<AffiliatePlan> {
        return this.httpService.getWdsDoc<AffiliatePlan>('/affiliatePlan.json').pipe(
            map(plan => new AffiliatePlan(plan))
        );
    }

    getReferralInfo(): Observable<ReferralResponse> {
        return this.httpService.sendRequest<ReferralResponse>('/snowladmin/referralInfo').pipe(
            map(resp => {
                if (resp.success) {
                    resp.referralTransactions = resp.referralTransactions.sort((a, b) => a.date > b.date ? -1 : 1);
                    resp.referredSubscribers = resp.referredSubscribers.sort((a, b) => a.date > b.date ? -1 : 1);
                }
                return resp;
            })
        );
    }
}

export interface ReferralResponse extends ShoutResponse {
    referredSubscribers: {
        date: Date,
        nickname: string,
        referrerNickname: string
    }[],
    referralTransactions: {
        date: Date,
        nickname: string,
        amount: number
    }[],
    affiliateNicknames: string[]
}
