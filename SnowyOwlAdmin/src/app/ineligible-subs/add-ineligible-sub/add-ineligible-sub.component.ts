import {Component} from '@angular/core';
import { VxDialogComponent, VxDialogDef, VxDialogRef, VxToast } from 'vx-components';
import {SubscriberService} from '../../shared/services/subscriber.service';
import {Subscriber} from '../../model/subscriber';

@Component({
    selector: 'app-add-ineligible-sub',
    templateUrl: './add-ineligible-sub.component.html',
    styleUrls: ['./add-ineligible-sub.component.scss']
})
export class AddIneligibleSubComponent extends VxDialogDef {
    selectedSub = '';
    linkedSub = '';
    reason = '';

    constructor(private subscriberService: SubscriberService,
                public dialog: VxDialogRef<AddIneligibleSubComponent>,
                public toast: VxToast) {
        super();
        this.subscriberService.loadAllSubscribers();
    }

    get subs(): Subscriber[] {
        return this.subscriberService.allSubscribers;
    }

    async submit(): Promise<void> {
        const result = await this.subscriberService.addIneligibleSubscriber(this.selectedSub, this.reason, this.reason === 'EMPLOYEE' ? undefined : this.linkedSub);

        if (result.success) {
            this.toast.open({
                title: 'Success',
                text: 'Successfully Added Ineligible Subscriber'
            });
            this.dialog.close(undefined)
        } else {
            this.toast.open({
                title: 'Error',
                text: 'Unexpected Error Adding Ineligible Subscriber: ' + JSON.stringify(result, null, 2),
                type: 'error',
                duration: 10000
            })
        }
    }
}
