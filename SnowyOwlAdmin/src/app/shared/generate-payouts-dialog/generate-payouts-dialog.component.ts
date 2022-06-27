import {Component} from '@angular/core';
import { VxDialog, VxDialogDef, VxDialogRef } from 'vx-components';
import {PayoutModelService} from '../services/payout-model.service';
import {PayoutModel} from '../../model/payout-model';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import {PayoutTableComponent} from '../payout-table/payout-table.component';
import {LoadingDialogComponent} from '../loading-dialog.component';
import {Payout} from '../../model/payout';
import {PayoutModelRound} from 'app/model/payout-model-round';

@Component({
    selector: 'app-generate-payouts-dialog',
    templateUrl: './generate-payouts-dialog.component.html',
    styleUrls: ['./generate-payouts-dialog.component.scss']
})
export class GeneratePayoutsDialogComponent extends VxDialogDef<OpenData | undefined> {
    numberMask = createNumberMask({prefix: '', allowDecimal: false});
    moneyMask = createNumberMask({prefix: '$', allowDecimal: true});

    payoutModelId?: number;
    minimumPayoutAmount = 1;
    expectedNumPlayers!: number;
    payoutModels$ = this.payoutModelService.getPayoutModels();

    constructor(private payoutModelService: PayoutModelService,
                private vxDialog: VxDialog, private dialog: VxDialogRef<GeneratePayoutsDialogComponent>) {
        super();

        if (dialog.data) {
            this.minimumPayoutAmount = dialog.data.minimumPayoutAmount || 1;
            this.payoutModelId = dialog.data.payoutModelId;
        }
    }

    close(): void {
        this.dialog.close();
    }

    generate(): void {
        const loader = this.vxDialog.open(LoadingDialogComponent,  'Generating Payout Table...');
        this.payoutModelService.generatePayoutTable(this.payoutModelId!, this.minimumPayoutAmount, this.expectedNumPlayers).subscribe((models: PayoutModelRound[]) => {
            loader.close();
            this.vxDialog.open(PayoutTableComponent, models);
        })
    }

}

interface OpenData {
    payoutModelId?: number;
    minimumPayoutAmount?: number;
}
