import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {TitleService} from '../../shared/services/title.service';
import {AffiliateService, ReferralResponse} from '../../shared/services/affiliate.service';
import {VxDialog} from 'vx-components';

@Component({
    selector: 'app-affiliate-tree',
    templateUrl: './affiliate-tree.component.html',
    styleUrls: ['./affiliate-tree.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AffiliateTreeComponent implements OnInit {
    loading = false;

    affiliates?: ParsedReferredPerson[];
    referredPeople: {[name: string]: ParsedReferredPerson} = {};

    people: ParsedReferredPerson[] = [];

    get currentPerson(): ParsedReferredPerson | undefined {
        return this.people.length ? this.people[this.people.length - 1] : undefined;
    }

    me?: ParsedReferredPerson;

    constructor(private titleService: TitleService, private affiliateService: AffiliateService, private dialog: VxDialog,
                private cdr: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.titleService.title = 'Affiliate Tree';
        this.loadInfo();
    }

    loadInfo(): void {
        this.loading = true;
        this.cdr.markForCheck();

        this.affiliateService.getReferralInfo().subscribe(resp => {
            this.loading = false;
            this.cdr.markForCheck();

            if (!resp.success) {
                this.dialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: `An unexpected error occurred: ${JSON.stringify(resp)}`,
                    buttons: ['Dismiss']
                })
            } else {
                this.handleResponse(resp);
            }
        })
    }

    private handleResponse(resp: ReferralResponse): void {
        for (const sub of resp.referredSubscribers) {
            const referrer = this.ensureReferredPerson(sub.referrerNickname);
            const referred = this.ensureReferredPerson(sub.nickname);
            if (!referrer.children.includes(referred))
                referrer.children.push(referred);

            referred.date = sub.date;
            referred.parent = referrer;
        }

        for (const transaction of resp.referralTransactions) {
            const person = this.ensureReferredPerson(transaction.nickname);
            person.events.push(transaction);

            person.amount = +(person.amount + transaction.amount).toFixed(2);
        }

        this.affiliates = [];

        for (const affiliate of resp.affiliateNicknames) {
            const person = this.referredPeople[affiliate];
            if (person) {
                this.affiliates.push(person);
                person.isAffiliate = true;
            }
        }

        this.me = new ParsedReferredPerson('All Affiliates');
        this.me.children = this.affiliates;

        this.adjustPerson(this.me);
        this.me.numChildren -= this.me.children.length;

        this.people = [this.me];

        this.cdr.markForCheck();
    }

    /**
     * Calculates the numChildren and the amount for a referred person
     */
    private adjustPerson(person: ParsedReferredPerson, affiliateName = ''): void {
        if (person.adjusted) {
            return;
        }

        person.adjusted = true;
        person.affiliateName = affiliateName;

        if (person.isAffiliate) {
            affiliateName = person.nickname;
        }

        person.numChildren = person.children.length;
        for (const child of person.children) {
            this.adjustPerson(child, affiliateName);

            if (!child.isAffiliate || person === this.me) {
                person.numChildren += child.numChildren;
                person.amount += child.amount;
            }
        }
    }

    private ensureReferredPerson(nickname: string): ParsedReferredPerson {
        return this.referredPeople[nickname] = this.referredPeople[nickname] || new ParsedReferredPerson(nickname);
    }
}

export class ParsedReferredPerson {
    parent?: ParsedReferredPerson;
    children: ParsedReferredPerson[] = [];
    date?: Date;
    isAffiliate?: boolean;
    affiliateName = '';

    amount = 0;
    numChildren = 0;


    adjusted = false;
    events: ReferralEvent[] = [];

    constructor(public readonly nickname: string) {}
}
export interface ReferralEvent {
    date: Date;
    nickname: string;
    amount: number;
}
