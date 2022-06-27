import {BaseModel} from './base-model';
import {autoserialize} from 'cerialize';

export class SubscriberAddress extends BaseModel<SubscriberAddress> {

    @autoserialize subscriberId: number;
    @autoserialize addressId: number;
    @autoserialize type: string;
    @autoserialize addr1: string;
    @autoserialize addr2: string;
    @autoserialize city: string;
    @autoserialize state: string;
    @autoserialize zip: string;
    @autoserialize countryCode: string;

    initialize(): void {
        this.type = 'BILLING';
        this.state = '';
        this.countryCode = '';
    }
}
