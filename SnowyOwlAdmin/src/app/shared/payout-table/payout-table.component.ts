import {Component} from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import {Payout} from '../../model/payout';
import {PayoutModelRound} from 'app/model/payout-model-round';

@Component({
    selector: 'app-payout-table',
    templateUrl: './payout-table.component.html',
    styleUrls: ['./payout-table.component.scss']
})
export class PayoutTableComponent extends VxDialogDef<PayoutModelRound[]> {
    payouts: PayoutModelRound[];

    constructor(private dialog: VxDialogRef<PayoutTableComponent>) {
        super();
        this.payouts = dialog.data;
    }

    close(): void {
        this.dialog.close();
    }
}
