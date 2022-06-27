import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class MatchQuestion extends BaseModel<MatchQuestion> {
    @autoserialize id: string;
    @autoserialize gameId: string;
    @autoserialize roundId: string;
    @autoserialize matchId: string;
    @autoserialize questionId: string;
    @autoserialize matchQuestionStatus: MatchQuestionStatus;
    @autoserialize wonSubscriberId: number | null; // Will be null until there is a winner
    @autoserialize determination: Determination;

    @autoserializeAs(Date) createDate: Date;
    @autoserializeAs(Date) completedDate: Date;

}

type MatchQuestionStatus = 'NEW' | 'OPEN' | 'WAITING_FOR_NEXT_QUESTION' | 'PROCESSING' | 'CLOSED' | 'CANCELLED';
type Determination = 'WINNER' | 'NO_WINNER' | 'TIE' | 'UNKNOWN'; // TODO: what does unknown mean?
