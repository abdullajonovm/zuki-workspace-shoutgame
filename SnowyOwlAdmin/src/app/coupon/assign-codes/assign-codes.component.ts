import {Component, OnInit} from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import {CouponBatch} from '../model/coupon-batch';
import {CouponService} from '../coupon.service';
import {SubscriberService} from '../../shared/services/subscriber.service';

@Component({
    selector: 'app-assign-codes',
    templateUrl: './assign-codes.component.html',
    styleUrls: ['./assign-codes.component.scss']
})
export class AssignCodesComponent extends VxDialogDef<CouponBatch, boolean> {
    batch: CouponBatch;
    selected: number[] = [];

    constructor(private couponService: CouponService, public subscriberService: SubscriberService,
                private dialog: VxDialogRef<AssignCodesComponent>) {
        super();
        this.batch = dialog.data;
        this.subscriberService.loadAllSubscribers();
    }


    async submit(): Promise<void> {
        await this.couponService.assignCoupons(this.selected.join(','), this.batch.batchId);
        this.dialog.close(true);
    }

}

