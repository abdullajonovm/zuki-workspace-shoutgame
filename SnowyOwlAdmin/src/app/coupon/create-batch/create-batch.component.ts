import {Component, OnInit} from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import {CouponBatch} from '../model/coupon-batch';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import {dateMask, dateMaskShort} from '../../shared/date.directive';
import {CouponService} from '../coupon.service';

@Component({
    selector: 'app-create-batch',
    templateUrl: './create-batch.component.html',
    styleUrls: ['./create-batch.component.scss']
})
export class CreateBatchComponent extends VxDialogDef<undefined, boolean> implements OnInit {
    dateMask = dateMaskShort;
    moneyMask = createNumberMask({prefix: '$', allowDecimal: true});
    batch = new CouponBatch();
    amount!: number;

    expires = false;
    constructor(private couponService: CouponService, private dialog: VxDialogRef<CreateBatchComponent>) {
        super();
    }

    ngOnInit(): void {
    }

    async submit(): Promise<void> {
        if (!this.expires) {
            this.batch.expireDate = undefined;
        }
        this.batch.createDate = new Date();
        this.batch.amount = this.amount;
        await this.couponService.createBatch(this.batch);
        this.dialog.close(true);
    }
}
