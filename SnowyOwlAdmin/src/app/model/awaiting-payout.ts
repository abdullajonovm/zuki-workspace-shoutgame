import {autoserialize} from 'cerialize';
import {BaseModel} from './base-model';

export class AwaitingPayout extends BaseModel<AwaitingPayout>{
    @autoserialize manualRedeemRequestId!: number;
    @autoserialize subscriberId!: number;
    @autoserialize amount!: number;
    @autoserialize nickname!: string;
    @autoserialize email!: string;
}
