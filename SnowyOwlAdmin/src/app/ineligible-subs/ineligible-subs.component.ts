import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {SubscriberService} from '../shared/services/subscriber.service';
import {IneligibleSub} from '../model/ineligible-sub';
import {Subscriber} from '../model/subscriber';
import { VxDialog, VxToast } from 'vx-components';
import {AddIneligibleSubComponent} from './add-ineligible-sub/add-ineligible-sub.component';
import {TitleService} from '../shared/services/title.service';
import {LoadingDialogComponent} from '../shared/loading-dialog.component';

@Component({
    selector: 'app-ineligible-subs',
    templateUrl: './ineligible-subs.component.html',
    styleUrls: ['./ineligible-subs.component.scss']
    // changeDetection: ChangeDetectionStrategy.OnPush
})
export class IneligibleSubsComponent implements OnInit {

    loading = false;
    selectedSub?: IneligibleSub;

    constructor(private subscriberService: SubscriberService, private cdr: ChangeDetectorRef, private dialog: VxDialog,
                private titleService: TitleService, private toast: VxToast) {
        this.titleService.title = 'Ineligible Subscribers'
    }

    get inSubs(): IneligibleSub[] {
        return this.subscriberService.ineligibleSubs;
    }

    ngOnInit(): void {
        this.loadIsubs();
    }

    async loadIsubs(): Promise<void> {
        this.loading = true;
        this.cdr.markForCheck();
        await this.subscriberService.loadIneligibleSubscribers();
        this.loading = false;
        this.cdr.markForCheck();
    }

    async addISub(): Promise<void> {
        await this.dialog.open(AddIneligibleSubComponent).onClose.toPromise();
        this.loadIsubs();
    }

    async remove(): Promise<void> {
        const dialog = this.dialog.open(LoadingDialogComponent,  'Removing Ineligible Subscriber...');
        const result = await this.subscriberService.removeIneligibleSubscriber(this.selectedSub!.isId);
        dialog.close();

        if (result.success) {
            this.toast.open({
                title: 'Success!',
                text: 'Successfully removed ineligible subscriber.'
            })
        } else {
            this.toast.open({
                title: 'Error!',
                text: 'Unexpected Error: ' + JSON.stringify(result, null, 2),
                type: 'error'
            });
        }
        await this.loadIsubs();
    }

}

interface ParsedIsub extends IneligibleSub {
    sub?: Subscriber;
    linkedSub?: Subscriber;
}
