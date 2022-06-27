import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class MatchPlayer extends BaseModel<MatchPlayer> {
    @autoserialize id: string;
    @autoserialize gameId: string;
    @autoserialize roundId: string;
    @autoserialize matchId: string;
    @autoserialize subscriberId: number;
    @autoserialize determination: Determination;

    @autoserializeAs(Date) createDate: Date;

    publicProfile?: PublicProfile;
}

type Determination = 'UNKNOWN' | 'WON' | 'LOST' | 'CANCELLED';

export interface PublicProfile {
    subscriberId: number;
    nickname: string;
    photoUrl: string;
    primaryIdHash: string;
}
