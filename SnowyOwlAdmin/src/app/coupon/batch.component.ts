import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { AlertDialogComponent } from '../shared/alert-dialog/alert-dialog.component';
import {CouponService} from './coupon.service';
import {CouponBatch} from './model/coupon-batch';
import {CouponCode} from './model/coupon-code';
import {TitleService} from '../shared/services/title.service';
import {VxDialog} from 'vx-components';
import {AssignCodesComponent} from './assign-codes/assign-codes.component';

@Component({
    selector: 'app-coupon',
    templateUrl: './batch.component.html',
    styleUrls: ['./batch.component.scss']
})
export class BatchComponent implements OnInit {
    batch?: CouponBatch;
    codes: CouponCode[] = [];
    loading = false;
    constructor(private route: ActivatedRoute, private couponService: CouponService, private router: Router,
                private titleService: TitleService, private dialog: VxDialog) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
           this.loadBatch(+params.id);
        });
    }

    assignCodes(): void {
        if (!this.batch) return;

        const batchId = this.batch.batchId;
        this.dialog.open(AssignCodesComponent, this.batch).onClose.subscribe((reload: boolean) => {
            if (reload)
                this.loadBatch(batchId);
        });
    }

    cancelBatch(): void {
        this.dialog.open(AlertDialogComponent, {
            title: 'Confirm',
            body: 'Are you sure you want to cancel this coupon batch? (Will not affect already-redeemed coupons)',
            defaultButtonIdx: 1,
            buttons: ['Yes', 'No']
        }).onClose.subscribe((val: string) => {
            if (val === 'Yes') {
                this.cancel();
            }
        })
    }

    private async loadBatch(batchId: number): Promise<void> {
        this.loading = true;
        const batch = await this.couponService.getBatch(batchId);
        if (!batch) {
            this.router.navigate(['/coupon']);
            return;
        }
        this.batch = batch;
        this.titleService.title = batch.batchName;
        this.codes = await this.couponService.retrieveCoupons({batchId: batch.batchId});
        this.loading = false;
    }

    private async cancel(): Promise<void> {
        if (!this.batch) return;
        await this.couponService.cancelBatch(this.batch.batchId);
        this.loadBatch(this.batch.batchId);
    }
}
