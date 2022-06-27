import {autoserialize, autoserializeAs} from 'cerialize';
import {BaseModel} from './base-model';
import {PayoutModelRound} from './payout-model-round';

export class PayoutModel extends BaseModel<PayoutModel> {
    @autoserialize payoutModelId!: number;
    @autoserialize name!: string;
    @autoserialize basePlayerCount!: number;
    @autoserialize entranceFeeAmount!: number;
    @autoserialize active!: boolean;
    @autoserialize deactivationReason!: string;
    @autoserialize creatorId!: number;
    @autoserialize deactivatorId!: number;
    @autoserializeAs(Date) createDate!: Date;
    @autoserializeAs(Date) deactivatedDate?: Date;

    @autoserializeAs(PayoutModelRound) payoutModelRounds!: PayoutModelRound[];

    protected initialize(): void {
        this.payoutModelRounds = [];
    }
}
