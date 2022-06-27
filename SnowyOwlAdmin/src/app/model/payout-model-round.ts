import {BaseModel} from './base-model';
import {autoserialize} from 'cerialize';

export class PayoutModelRound extends BaseModel<PayoutModelRound> {
    @autoserialize sortOrder!: number;
    @autoserialize payoutModelId!: number;
    @autoserialize description!: string;
    @autoserialize startingPlayerCount!: number;
    @autoserialize eliminatedPlayerCount!: number;
    @autoserialize eliminatedPayoutAmount!: number;
    @autoserialize type!: 'CASH';
    @autoserialize category!: 'PHYSICAL' | 'VIRTUAL';

    protected initialize(): void {
        this.type = 'CASH';
        this.category = 'PHYSICAL';
    }
}
