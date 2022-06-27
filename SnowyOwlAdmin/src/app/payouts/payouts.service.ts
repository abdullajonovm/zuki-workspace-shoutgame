import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from '../shared/services/http.service';
import {AwaitingPayout} from '../model/awaiting-payout';

@Injectable()
export class PayoutsService {
    constructor(private http: HttpService) {

    }

    async getAwaitingPayouts(): Promise<AwaitingPayout[]> {

        const result = await this.http.sendRequest<AwaitingPayoutResponse>('/snowladmin/payouts/getOustandingManualRequests').toPromise();
        return result.awaitingPayout.map(payout => new AwaitingPayout(payout));
    }

    async markAwaitingPayoutAsPaid(manualRedeemRequestId: number): Promise<ShoutResponse> {
        return await this.http.sendRequest('/snowladmin/payout/markAsPaid', {manualRedeemRequestId}).toPromise();
    }
}

interface AwaitingPayoutResponse extends ShoutResponse {
    awaitingPayout: AwaitingPayout[];
}
