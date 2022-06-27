import { Component, OnInit } from '@angular/core';
import {CouponService} from '../coupon.service';
import {CouponBatch} from '../model/coupon-batch';
import {VxDialog} from 'vx-components';
import {CreateBatchComponent} from '../create-batch/create-batch.component';
import {Router} from '@angular/router';
import {TitleService} from '../../shared/services/title.service';

@Component({
  selector: 'app-batches',
  templateUrl: './batches.component.html',
  styleUrls: ['./batches.component.scss']
})
export class BatchesComponent implements OnInit {
    loading = true;

    constructor(public couponService: CouponService, public dialog: VxDialog, private router: Router,
                private titleService: TitleService) {
    }

    ngOnInit(): void {
        this.titleService.title = 'Coupon Batches';
        this.loadBatches();
    }

    createBatch(): void {
        this.dialog.open(CreateBatchComponent).onClose.subscribe((refresh: boolean) => {
            if (refresh)
                this.loadBatches();
        });
    }

    goToBatch(batch: CouponBatch): void {
        this.router.navigate(['coupon', 'batch', batch.batchId]);
    }

    loadBatches(): void {
        this.loading = true;
        this.couponService.loadBatches().then(() => this.loading = false);
    }
}
