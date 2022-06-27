import { Component, OnInit, Type } from '@angular/core';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import { VxDialog, VxDialogDef, VxDialogRef, VxToast } from 'vx-components';
import {Subscriber} from '../../model/subscriber';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {LoadingDialogComponent} from '../../shared/loading-dialog.component';
import { ShoutResponse } from '../../shared/services/http.service';
import { SponsorService } from '../../shared/services/sponsor.service';
import {SubscriberService} from '../../shared/services/subscriber.service';
import { Util } from '../../util';

@Component({
    selector: 'app-bonus-cash',
    templateUrl: './bonus-cash.component.html',
    styleUrls: ['./bonus-cash.component.scss']
})
export class BonusCashComponent extends VxDialogDef<BonusCashOptions> {
    numberMask = createNumberMask({allowDecimal: true});
    bonusAmount = '';
    subscriber: Subscriber;
    type: 'bonus' | 'sponsor';

    constructor(private vxDialog: VxDialog, private subscriberService: SubscriberService, private sponsorService: SponsorService,
        public dialog: VxDialogRef<BonusCashComponent>, private toast: VxToast) {
        super();
        this.subscriber = dialog.data.subscriber;
        this.type = dialog.data.type;
    }

    async send(): Promise<void> {
        const amount = Util.parseNumber(this.bonusAmount);
        const dialog = this.vxDialog.open(LoadingDialogComponent,  {title: this.type === 'bonus' ? 'Sending Bonus Cash...' : 'Sending Sponsor Money...', message: ''});
        this.dialog.close();
        let resp: ShoutResponse;
        if (this.type === 'bonus') {
            resp = await this.subscriberService.sendBonusCash(this.subscriber.nickname, amount);
        } else if (this.type === 'sponsor') {
            const sponsorResp = await this.sponsorService.addCashToSponsor(this.subscriber.email, amount).toPromise();
            resp = sponsorResp;
            if (sponsorResp.notASponsor) {
                const result = this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: `Subscriber ${this.subscriber.nickname} is not a sponsor`,
                    buttons: ['Dismiss']
                });
                result.close('Dismiss');
                dialog.close();
                return;
            }
        } else {
            return;
        }

        dialog.close();
        this.toast.open({
            type: resp.success ? 'success' : 'error',
            title: resp.success ? 'Success' : 'Error',
            text: resp.success ? `Successfully gave ${this.type === 'bonus' ? 'bonus cash' : 'sponsor money'} to: ${this.subscriber.nickname}` :
                `Failed sending money to: ${this.subscriber.nickname}`
        })
    }

}

interface BonusCashOptions {
    subscriber: Subscriber,
    type: 'bonus' | 'sponsor'
}
