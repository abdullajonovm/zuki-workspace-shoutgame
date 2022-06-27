import {autoserialize} from 'cerialize';
import {BaseModel} from './base-model';

export class Echeck extends BaseModel<Echeck> {
    @autoserialize itemUuid: string;
    @autoserialize accountId: string;
    @autoserialize addressId: number;
    @autoserialize phone: string;
}
