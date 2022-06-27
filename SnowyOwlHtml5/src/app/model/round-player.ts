import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class RoundPlayer extends BaseModel<RoundPlayer> {
    @autoserialize id: string;
    @autoserialize gameId: string;
    @autoserialize roundId: string;
    @autoserialize subscriberId: number;
    @autoserialize determination: Determination;

    @autoserialize rank?: number;

    @autoserializeAs(Date) createDate: Date;
}

type Determination = 'WON' | 'LOST' | 'TIMEDOUT' | 'ABANDONED' | 'UNKNKOWN' | 'CANCELLED';
