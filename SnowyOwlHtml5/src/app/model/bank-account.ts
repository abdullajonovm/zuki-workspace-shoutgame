import {BaseModel} from './base-model';
import {autoserialize} from 'cerialize';

export class BankAccount extends BaseModel<BankAccount> {
    @autoserialize id: string;
    @autoserialize bankName: string;
    @autoserialize checkingAccountName: string;
    @autoserialize routingNumber: string;
    @autoserialize accountNumber: string;
}
