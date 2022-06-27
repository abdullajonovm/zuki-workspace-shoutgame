import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {DialogComponent, OnDialogOpen} from 'app/shared/dialog/dialog.component';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {environment} from 'environments/environment';
import {PaymentService, PaymentMethod, CardData} from '../services/payment.service';
import {VenueItem} from '../../model/venue-item';
import {DialogService} from '../dialog/dialog.service';
import {NgModel} from '@angular/forms';
import * as validCard from 'card-validator';
import {LogService} from '../services/log.service';
import {CreditCard} from '../../model/customer-profile';
import {states} from '../../constants';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/debounceTime';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import {SubscriberAddress} from '../../model/subscriber-address';
import {Echeck} from '../../model/echeck';
import {BankAccount} from '../../model/bank-account';

const STRINGS = environment.strings.payment;

@Component({
    selector: 'sh-payment',
    templateUrl: './payment.component.html',
    styleUrls: ['./payment.component.scss']
})
export class PaymentComponent implements OnDialogOpen<PaymentType>, OnInit {
    moneyMask = createNumberMask({allowDecimal: true});
    state: State = 'Default';

    history: State[] = [];

    saveCard = false;
    card = new CardData();
    selectedItem?: VenueItem;

    dialog?: DialogComponent;

    subscriber = this.subscriberService.subscriber;
    venueItems: VenueItem[] = [];

    selectedPaymentMethod?: PaymentMethod;

    bank = new BankAccount();
    address = new SubscriberAddress();
    echeck = new Echeck();

    withdrawAmount: number;

    couponCode = '';

    @Output() onClose = new EventEmitter();

    constructor(private subscriberService: SubscriberService, public paymentService: PaymentService,
                private dialogService: DialogService, private logService: LogService) {
        this.paymentService.getItemsForVenue().then(items => this.venueItems = items);
        this.subscriberService.getCountries();
        // this.subscriberService.getCashPoolTransactions().then((a) => {
        //     debugger;
        // })
    }

    maskCard(card: CardData | CreditCard): string {
        if (!card) {
            return '';
        }

        if (card instanceof CardData) {
            return card.cardNumber.replace(/[\d\s]*(?=\d{4})/g, '*****')
        } else {
            return card.number.replace(/X/g, '****');
        }
    }

    ngOnInit(): void {
        if (this.subscriberService.isLoggedIn())
            this.paymentService.loadCustomerProfile();
    }

    onDialogOpen(dialog: DialogComponent, type: PaymentType): void {
        this.dialog = dialog;
        dialog.dialogOptions.disableClose = true;

        this.state = type;
    }

    goToState(state: State): void {
        this.history.push(this.state);
        this.state = state;
    }

    goBack(): void {
        this.state = this.history.pop()!;
    }

    checkWithdraw(withdraw: HTMLInputElement, model: NgModel): void {
        const value = +withdraw.value.replace(/[^0-9.]/g, '');
        let invalid = false;
        if (value <= 0 || value > this.subscriber.availableWallet) {
            invalid = true;
        } else {
            this.withdrawAmount = value;
        }
        setTimeout(() => model.control.setErrors(invalid ? {invalid: true} : null));
    }


    async purchase(): Promise<void> {
        const item = this.selectedItem;
        if (!item) {
            this.state = 'Default';
            this.logService.error('Tried to purchase but no item was selected');
            return;
        }
        this.dialogService.showLoadingIndicator('Buying...');

        if (this.selectedPaymentMethod === this.echeck) {
            await this.purchaseWithCheck(item);
        } else {
            await this.purchaseWithCard(item);
        }

        await this.subscriberService.loadPlayerDetails();
        this.dialogService.closeLoadingIndicator();

        if (this.dialog)
            this.dialog.close();
        this.onClose.emit();
    }

    explainWithdraw(): void {
        this.dialogService.open({
            body: 'You are only able to withdraw money that you have added to your account or won in a game. Coupon Codes and other money that is given to you for free cannot be withdrawn.',
            buttons: [{text: 'Close'}]
        })
    }

    async withdrawMoney(): Promise<void> {
        try {
            const addressId = (await this.subscriberService.addSubscriberAddress(this.address)).addressId;
            const accountId = (await this.paymentService.createBankAccount(this.bank)).account.id;
            await this.paymentService.withdraw(this.withdrawAmount, accountId, addressId);
        } catch (e) {
            this.dialogService.closeLoadingIndicator();
            this.dialogService.open({
                body: 'Withdrawing failed, please try again later.',
                buttons: [{text: 'Ok'}]
            });
            this.logService.error('Failed to withdraw', e);
            return;
        }
        this.dialogService.open({
            title: 'Success!',
            body: `Successfully sent $${this.withdrawAmount} to your bank account!  It will arrive in the next 3-5 business days.`,
            buttons: [{text: 'Ok'}]
        });
        await this.subscriberService.loadPlayerDetails();
        if (this.dialog)
            this.dialog.close();
        this.onClose.emit();
    }

    async redeemCouponCode(): Promise<void> {
        this.dialogService.showLoadingIndicator('Redeeming...');
        const response = await this.paymentService.redeemCoupon(this.couponCode);
        if (response.success) {
            await this.subscriberService.loadPlayerDetails();
            this.dialogService.closeLoadingIndicator();
            this.dialogService.open({
                title: 'Success!',
                body: 'Successfully redeemed coupon code!',
                buttons: [{text: 'Ok'}]
            });
            this.goBack();
        } else {
            this.dialogService.closeLoadingIndicator();
            this.dialogService.open({
                body: response.couponAlreadyRedeemed ? 'Coupon already redeemed!' : 'Invalid Coupon Code!',
                buttons: [{text: 'Ok'}]
            });
        }
    }

    private async purchaseWithCard(item: VenueItem): Promise<void> {

        try {
            await this.paymentService.buy(item, this.selectedPaymentMethod as any);
        } catch (e) {
            this.dialogService.closeLoadingIndicator();
            this.dialogService.open({
                body: 'Purchase failed, please try again later',
                buttons: [{text: 'Ok'}]
            });
            this.logService.error(e);
            return;
        }

        if (this.saveCard && this.selectedPaymentMethod === this.card)
            await this.paymentService.saveCard(this.card);
    }

    private async purchaseWithCheck(item: VenueItem): Promise<void> {
        try {
            this.echeck.itemUuid = item.uuid;
            this.echeck.addressId = (await this.subscriberService.addSubscriberAddress(this.address)).addressId;
            const bankAccount = await this.paymentService.createBankAccount(this.bank);
            this.echeck.accountId = bankAccount.account.id;
            await this.paymentService.echeck(this.echeck);
        } catch (e) {
            this.dialogService.closeLoadingIndicator();
            this.dialogService.open({
                body: 'Purchase failed, please try again later',
                buttons: [{text: 'Ok'}]
            });
            this.logService.error(e);
            return;
        }

    }
}

type State = 'Default' | 'NotEnoughMoney' | 'Add' | 'Withdraw' | 'Coupon';
type PaymentType = 'Default' | 'NotEnoughMoney';
