import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class GamePlayer extends BaseModel<GamePlayer> {
    @autoserialize id: string;
    @autoserialize gameId: string;
    @autoserialize subscriberId: number;
    @autoserialize determination: Determination;


    @autoserialize payoutCompleted: boolean;
    @autoserialize payoutPaymentId?: string; // receipt id
    @autoserialize payoutVenue?: string;
    @autoserialize rank?: number;
    @autoserialize countdownToElimination?: number | null; // how many "lives" a user has in the game. null=no limit,
    @autoserialize nextRoundId?: string; // the uuid of the next round in a game (null=at the last round or eliminated)
    @autoserialize lastRoundId?: string; //  the uuid of the previous round in a game (null=at the first round)
    @autoserializeAs(Date) createDate: Date;

    private _payoutAwardedAmount?: number;

    @autoserialize
    get payoutAwardedAmount(): number | undefined {
        return this._payoutAwardedAmount;
    }

    set payoutAwardedAmount(value: number | undefined) {
        if (value && value > 1) {
            value = Math.floor(value);
        }
        this._payoutAwardedAmount = value;
    }
}

/**
 * INPLAY - actively playing the game now
 * SIDELINES - in the game, but not currently playing
 * ELIMINATED - user was eliminated during bracket play
 * AWARDED - game is over and user has been awarded payout
 * REMOVED - user unjoined the game
 * CANCELLED
 */
type Determination = 'INPLAY' | 'SIDELINES' | 'ELIMINATED' | 'AWARDED' | 'REMOVED' | 'CANCELLED';
