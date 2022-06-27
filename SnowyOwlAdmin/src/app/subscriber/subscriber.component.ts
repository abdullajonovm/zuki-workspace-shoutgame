import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { SubscriberRolesComponent } from 'app/subscriber/subscriber-roles/subscriber-roles.component';
import { subMonths, subWeeks, subYears } from 'date-fns';
import { Observable } from 'rxjs';
import { VxDialog } from 'vx-components';
import { Subscriber } from '../model/subscriber';
import { SubscriberService } from '../shared/services/subscriber.service';
import { TitleService } from '../shared/services/title.service';
import { BonusCashComponent } from './bonus-cash/bonus-cash.component';

@Component({
    templateUrl: './subscriber.component.html',
    styleUrls: ['./subscriber.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SubscriberComponent implements OnInit {
    searchType: 'email' | 'date' | 'role' = 'date';

    subscribers: Subscriber[] = [];

    dateRange: DateRange = 'month';
    email = '';
    role = '';

    allRoles$ = this.subscriberService.getRoles();

    selectedSubscriber?: Subscriber;

    loading = false;
    constructor(public subscriberService: SubscriberService, private titleService: TitleService,
                private dialog: VxDialog, private cdr: ChangeDetectorRef) {
        this.titleService.title = 'Subscriber';
    }

    ngOnInit(): void {
        this.search();
    }

    sendExtraCash(): void {
        if (this.selectedSubscriber) {
            this.dialog.open(BonusCashComponent, {subscriber: this.selectedSubscriber, type: 'bonus'});
        }
    }

    addCashToSponsor(): void {
        if (this.selectedSubscriber) {
            this.dialog.open(BonusCashComponent, {subscriber: this.selectedSubscriber, type: 'sponsor'});
        }
    }

    changeRoles(): void {
        if (this.selectedSubscriber) {
            this.dialog.open(SubscriberRolesComponent, this.selectedSubscriber);
        }
    }

    search(): void {
        if (this.loading)
            return;

        let search: Observable<Subscriber[]>;
        if (this.searchType === 'date') {
            search = this.subscriberService.searchForSubscribers({fromDate: this.getFromDate(), toDate: new Date()});
        } else if (this.searchType === 'role') {
            search = this.subscriberService.searchForSubscribers({role: this.role});
        } else {
            search = this.subscriberService.searchForSubscribers({partialEmail: this.email});
        }

        this.loading = true;
        this.cdr.markForCheck();
        search.subscribe(resp => {
            this.subscribers = resp;
            this.loading = false;
            this.cdr.markForCheck();
        });
    }


    private getFromDate(): Date {
        switch (this.dateRange) {
            case 'week':
                return subWeeks(new Date(), 1);
            case 'month':
                return subMonths(new Date(), 1);
            case '6months':
                return subMonths(new Date(), 6);
            case 'year':
                return subYears(new Date(), 1);
            case 'allTime':
                return subYears(new Date(), 20);
            default:
                return new Date();
        }
    }

}

type DateRange = 'week' | 'month' | '6months' | 'year' | 'allTime';
