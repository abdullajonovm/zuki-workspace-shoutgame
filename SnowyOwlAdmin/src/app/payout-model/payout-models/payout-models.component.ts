import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {PayoutModelService} from '../../shared/services/payout-model.service';
import {PayoutModel} from '../../model/payout-model';
import {Router} from '@angular/router';
import {TitleService} from '../../shared/services/title.service';
import {VxDialog} from 'vx-components';
import {GeneratePayoutsDialogComponent} from '../../shared/generate-payouts-dialog/generate-payouts-dialog.component';

@Component({
    selector: 'app-payout-models',
    templateUrl: './payout-models.component.html',
    styleUrls: ['./payout-models.component.scss']
    // changeDetection: ChangeDetectionStrategy.OnPush
})
export class PayoutModelsComponent implements OnInit {
    payoutModels: PayoutModel[] = [];
    selectedPayoutModel?: PayoutModel;
    loading = false;

    constructor(private payoutModelService: PayoutModelService, private cdr: ChangeDetectorRef,
                private router: Router, private titleService: TitleService, private dialog: VxDialog) {
    }

    ngOnInit(): void {
        this.titleService.title = 'Payout Models';
        this.loadPayoutModels();
    }

    viewModel(): void {
        this.router.navigate(['/payout-model/edit/', this.selectedPayoutModel!.payoutModelId]);
    }

    generatePayoutTable(): void {
        this.dialog.open(GeneratePayoutsDialogComponent, {
            payoutModelId: this.selectedPayoutModel!.payoutModelId
        });
    }

    clonePayoutModel(): void {
        this.router.navigate(['/payout-model/clone/', this.selectedPayoutModel!.payoutModelId]);
    }

    private loadPayoutModels(): void {
        this.loading = true;
        this.cdr.markForCheck();

        this.payoutModelService.getPayoutModels().subscribe(models => {
            this.payoutModels = models;
            this.loading = false;
            this.cdr.markForCheck();
        })
    }

}
