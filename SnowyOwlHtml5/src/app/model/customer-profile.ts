import {BaseModel} from './base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class CreditCard extends BaseModel<CreditCard> {
    @autoserialize cardType: string;
    @autoserialize number: string;
    @autoserialize expDate: string;
    @autoserialize externalRefId: string;
}


export class CustomerProfile extends BaseModel<CustomerProfile> {
    @autoserialize subscriberId: number;
    @autoserialize customerId: string;
    @autoserializeAs(CreditCard) creditCardsOnFile: CreditCard[];

    initialize(): void {
        this.creditCardsOnFile = [];
    }
}
