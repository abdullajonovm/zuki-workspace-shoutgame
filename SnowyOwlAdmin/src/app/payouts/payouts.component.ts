import {Component, OnInit} from '@angular/core';
import { AlertDialogComponent } from '../shared/alert-dialog/alert-dialog.component';
import {TitleService} from '../shared/services/title.service';
import {PayoutsService} from './payouts.service';
import {AwaitingPayout} from '../model/awaiting-payout';
import {Util} from '../util';
import { VxDialog, VxToast } from 'vx-components';
import {LoadingDialogComponent} from '../shared/loading-dialog.component';

@Component({
    templateUrl: './payouts.component.html',
    styleUrls: ['./payouts.component.scss']
})
export class PayoutsComponent implements OnInit {
    loading = false;
    payouts: AwaitingPayout[] = [];
    selectedPayout?: AwaitingPayout;

    constructor(private titleService: TitleService, private payoutsService: PayoutsService,
                private dialog: VxDialog, private toast: VxToast) {

    }

    ngOnInit(): void {
        this.titleService.title = 'Payouts';
        this.loadPayouts();
    }

    async markAsPaid(): Promise<void> {
        const payout = this.selectedPayout!;

        const confirmed = await this.dialog.open(AlertDialogComponent, {
            title: 'Confirm',
            body: `Are you sure you want to mark ${payout.nickname} as having been paid $${payout.amount}?`,
            buttons: ['Yes', 'No']
        }).onClose.toPromise();

        if (confirmed === 'No')
            return;

        const dialog = this.dialog.open(LoadingDialogComponent, 'Marking as Paid...');
        const result = await this.payoutsService.markAwaitingPayoutAsPaid(payout!.manualRedeemRequestId);
        dialog.close();

        if (!result.success) {
            this.dialog.open(AlertDialogComponent, {
                title: 'Error!',
                body: 'Unexpected error trying to mark as paid: ' + JSON.stringify(result),
                buttons: ['Dismiss']
            });
        } else {
            this.toast.open({
                text: 'Successfully Marked as Paid!',
                duration: 4000
            })
        }

        this.loadPayouts();
    }

    copyEmailAddress(): void {
        const result = Util.copyTextToClipboard(this.selectedPayout!.email);
        if (result) {
            this.toast.open({
                text: 'Successfully copied to clipboard!'
            })
        } else {
            this.toast.open({
                text: 'An error occurred trying to copy to clipboard.',
                duration: 4000,
                type: 'error'
            })
        }
    }

    private async loadPayouts(): Promise<void> {
        this.loading = true;
        this.payouts = await this.payoutsService.getAwaitingPayouts();
        this.loading = false;
    }

}
