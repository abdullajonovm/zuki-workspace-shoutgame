import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from './http.service';
import {VenueItem} from 'app/model/venue-item';
import {CreditCard, CustomerProfile} from '../../model/customer-profile';
import {DialogService} from '../dialog/dialog.service';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {BankAccount} from '../../model/bank-account';
import {Echeck} from '../../model/echeck';
import {SubscriberService} from './subscriber.service';

const CLIENT_KEY = '222MG873znw2dXMW29f3TGNa97BTzM2fL3jYycd5e8C7TAWx3f4x9g3bSgWUKhee';
const API_LOGIN_ID = '2bX5V6pXaV';

@Injectable()
export class PaymentService {
    customerProfile?: CustomerProfile;

    constructor(private httpService: HttpService, private dialogService: DialogService, private http: HttpClient, private subscriberService: SubscriberService) {
        if (this.subscriberService.isLoggedIn())
            this.loadCustomerProfile();
    }

    sendPaymentToAuthorize(cardData: CardData): Promise<DispatchResponse> {
        return new Promise<DispatchResponse>((resolve) => {
            const secureData: SecureData = {
                cardData,
                authData: {
                    apiLoginID: API_LOGIN_ID,
                    clientKey: CLIENT_KEY
                }
            };

            Accept.dispatchData(secureData, response => {
                resolve(response);
            });
        })
    }

    async getItemsForVenue(): Promise<VenueItem[]> {
        const resp = await this.httpService.sendCollectorRequest<VenueItemResponse>('/store/getItemsForVenue', {
            demo: !environment.production,
            venue: 'tv.shout.snowyowl'
        }).toPromise();

        return resp.items.sort((a, b) => +a.price > +b.price ? 1 : -1) || [];
    }

    async loadCustomerProfile(): Promise<void> {
        const resp = await this.httpService.sendCollectorRequest<CustomerProfileResponse>('/store/getCustomerProfile').toPromise();
        this.customerProfile = new CustomerProfile(resp.customerProfile);
        this.customerProfile.creditCardsOnFile.forEach(card => {
            card.number = card.number.replace(/X/g, '*');
        })
    }

    async saveCard(card: CardData): Promise<void> {
        const response = await this.sendPaymentToAuthorize(card);
        await this.httpService.sendCollectorRequest('/store/addPaymentMethod', {
            nonce: response.opaqueData.dataValue
        }).toPromise();
    }

    async buy(item: VenueItem, using: PurchaseUsing): Promise<void> {
        if (using instanceof CreditCard) {
            await this.httpService.sendCollectorRequest('/store/purchaseItem', {
                demo: !environment.production,
                customerProfileCreditCardInfoExternalRefId: using.externalRefId,
                itemUuid: item.uuid
            }).toPromise();

        } else {
            const response = await this.sendPaymentToAuthorize(using);
            if (response.messages.resultCode === 'Error') {
                throw new Error('Got failed messages from authorize: ' + JSON.stringify(response.messages));
            } else {
                await this.httpService.sendCollectorRequest('/store/purchaseItem', {
                    demo: !environment.production,
                    nonce: response.opaqueData.dataValue,
                    itemUuid: item.uuid
                }).toPromise();
            }
        }

    }

    async createBankAccount(account: BankAccount): Promise<AddAccountResponse> {
        return this.httpService.sendCollectorRequest<AddAccountResponse>('/store/bankAccount/create', {account: account.toJSONString()}).toPromise();
    }

    async echeck(echeck: Echeck): Promise<void> {
        const phoneNumber = echeck.phone.replace(/[^\d]/g, '');
        await this.httpService.sendCollectorRequest('/store/oneTimeDraftRTV', {itemUuid: echeck.itemUuid, accountId: echeck.accountId,
            addressId: echeck.addressId, phone: phoneNumber}).toPromise();
    }

    async withdraw(amount: number, accountId: string, addressId: number): Promise<void> {
        await this.httpService.sendCollectorRequest('/store/billPayCheck', {amount, accountId, addressId}).toPromise();
    }

    redeemCoupon(couponCode: string): Promise<RedeemCouponResponse> {
        return this.httpService.sendCollectorRequest<RedeemCouponResponse>('/store/coupon/redeem', {couponCode}).toPromise();
    }

    queryRoutingNumber(number: string): Observable<{ customer_name: string }> {
        return this.http.jsonp<{ customer_name: string }>('https://www.routingnumbers.info/api/data.json?rn=' + encodeURIComponent(number), 'callback');
    }
}

export type PaymentMethod = CardData | CreditCard | Echeck;
export type PurchaseUsing = CardData | CreditCard;

interface ClientTokenResponse extends ShoutResponse {
    token: string;
}

interface VenueItemResponse extends ShoutResponse {
    items: VenueItem[];
}

interface CustomerProfileResponse extends ShoutResponse {
    customerProfile: Partial<CustomerProfile>;
}

export class CardData {
    cardNumber = '';
    month: string;
    year: string;
    cardCode?: string;
    zip?: string;
    fullName?: string;
}

interface AddAccountResponse extends ShoutResponse {
    account: BankAccount;
}
interface RedeemCouponResponse extends ShoutResponse {
    couponAlreadyRedeemed: boolean;
}
