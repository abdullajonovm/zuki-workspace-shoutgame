import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from './http.service';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {PayoutModel} from '../../model/payout-model';
import {map, tap} from 'rxjs/operators';
import {Util} from '../../util';
import {Game} from '../../model/game';
import {Payout} from '../../model/payout';
import {PayoutModelRound} from 'app/model/payout-model-round';

const payoutModelCacheTime = 30 * 1000;

@Injectable()
export class PayoutModelService {

    private payoutModels = new BehaviorSubject<PayoutModel[]>([]);
    private useCachedModels = false;
    constructor(private http: HttpService) {

    }

    getPayoutModels(costToJoin?: number): Observable<PayoutModel[]> {
        if (!costToJoin && this.useCachedModels) {
            return this.payoutModels.asObservable();
        }

        return this.http.sendRequest<PayoutModelsResponse>('/snowladmin/payoutModel/get', {costToJoin}).pipe(
            map((resp) => resp.payoutModels.map(model => new PayoutModel(model))),
            tap(models => {
                if (!costToJoin) {
                    this.payoutModels.next(models);
                    this.useCachedModels = true;
                    setTimeout(() => {
                        this.useCachedModels = false;
                    }, payoutModelCacheTime)
                }
            })
        );
    }

    // TODO: Have Scott give me an API to get individual payout models
    getPayoutModel(payoutModelId: number): Observable<PayoutModel | null> {
        const found = Util.find(this.payoutModels.value, 'payoutModelId', payoutModelId);
        if (found) {
            return of(found);
        }

        this.useCachedModels = false;
        return this.getPayoutModels().pipe(
            map((payoutModels) => Util.find(payoutModels, 'payoutModelId', payoutModelId))
        );
    }

    createPayoutModel(payoutModel: PayoutModel): Observable<ShoutResponse> {
        this.useCachedModels = false;
        return this.http.sendRequest('/snowladmin/payoutModel/create', {payoutModel: payoutModel.toJson()});
    }

    generatePayoutTable(payoutModelId: number, minimumPayoutAmount: number, expectedNumPlayers: number): Observable<PayoutModelRound[]> {
        return this.http.sendSynchronousCollectorRequest<PayoutsResponse>('/snowl/game/previewPayoutTable', {
            expectedNumPlayers,
            payoutModelId,
            minimumPayoutAmount
        }).pipe(
            map(resp => resp.payouts.sort((a, b) => a.startingPlayerCount - b.startingPlayerCount))
        );
    }
}


interface PayoutsResponse extends ShoutResponse {
    payouts: PayoutModelRound[]
}

interface PayoutModelsResponse extends ShoutResponse {
    payoutModels: PayoutModel[];
}
