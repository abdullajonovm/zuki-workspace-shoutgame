import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {PaymentService} from '../../services/payment.service';
import {states} from 'app/constants';
import {NgForm} from '@angular/forms';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import {Subject} from 'rxjs/Subject';
import {Echeck} from '../../../model/echeck';
import {BankAccount} from '../../../model/bank-account';
import {SubscriberAddress} from '../../../model/subscriber-address';
import {SubscriberService} from '../../services/subscriber.service';
import {Country} from '../../../model/country';

@Component({
    selector: 'sh-echeck',
    templateUrl: './echeck.component.html',
    styleUrls: ['./echeck.component.scss']
})
export class EcheckComponent implements OnInit {
    states = states;
    phoneMask = [/[1-9]/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];

    @Input() withdrawing: boolean;

    @Input() echeck: Echeck;
    @Input() bank: BankAccount;
    @Input() address: SubscriberAddress;

    routingNumberChange = new Subject();
    countries: Country[] = [];

    @ViewChild('form') form: NgForm;

    constructor(public paymentService: PaymentService, private subscriberService: SubscriberService) {
        this.routingNumberChange.debounceTime(300).subscribe(() => {
            this.paymentService.queryRoutingNumber(this.bank.routingNumber).subscribe(q => {
                if (q.customer_name)
                    this.bank.bankName = q.customer_name;
            });
        });
        this.subscriberService.getCountries().then(c => this.countries = c);
    }

    ngOnInit(): void {
    }
    goBack(): void {

    }

}
