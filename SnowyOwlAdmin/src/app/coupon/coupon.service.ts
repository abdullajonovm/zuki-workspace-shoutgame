import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from '../shared/services/http.service';
import {CouponBatch} from './model/coupon-batch';
import {CouponCode} from './model/coupon-code';
import {Util} from '../util';

@Injectable()
export class CouponService {
    batches: CouponBatch[] = [];
    constructor(private http: HttpService) {

    }

    createBatch(batch: CouponBatch): Promise<ShoutResponse> {
        return this.http.sendRequest('/snowladmin/coupon/createBatch', batch.toJsonObject()).toPromise();
    }

    cancelBatch(batchId: number): Promise<ShoutResponse> {
        return this.http.sendRequest('/snowladmin/coupon/cancelBatch', {batchId}).toPromise();
    }

    cancelCoupon(couponCode: string): Promise<ShoutResponse> {
        return this.http.sendRequest('/snowladmin/coupon/cancelCoupon', {couponCode}).toPromise();
    }

    assignCoupons(subscriberIds: string, batchId: number): Promise<ShoutResponse> {
        return this.http.sendRequest('/snowladmin/coupon/assign', {subscriberIds, batchId}).toPromise();
    }

    async loadBatches(): Promise<void> {
        const response = await this.http.sendRequest<GetBatchesResponse>('/snowladmin/coupon/retrieveBatches').toPromise();
        this.batches = response.couponBatches.map(b => new CouponBatch(b));
    }

    async retrieveCoupons(options: RetrieveOptions = {}): Promise<CouponCode[]> {
        const response = await this.http.sendRequest<GetCouponsResponse>('/snowladmin/coupon/retrieve', options).toPromise();
        return response.coupons.map(c => new CouponCode(c));
    }

    async getBatch(batchId: number): Promise<CouponBatch | null> {
        if (!this.batches.length) {
            await this.loadBatches();
        }
        return Util.find(this.batches, 'batchId', batchId);
    }
}

interface RetrieveOptions {
    batchId?: number;
    outstanding?: boolean;
    couponCode?: string;
    redeemed_since?: Date;
}

interface GetCouponsResponse extends ShoutResponse {
    coupons: CouponCode[];
}

interface GetBatchesResponse extends ShoutResponse {
    couponBatches: CouponBatch[];
}
