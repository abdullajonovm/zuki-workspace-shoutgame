import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';
import {MatchPlayer} from 'app/model/match-player';
import {QuestionResultSyncMessage, QuestionSyncMessage} from 'app/model/sync-message';
import {RoundPlayer} from 'app/model/round-player';
import {Question} from 'app/model/question';
import {Util} from 'app/util';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

export class Round extends BaseModel<Round> {
    @autoserialize id: string;
    @autoserialize gameId: string;
    @autoserializeAs(Object) roundNames: any;
    @autoserialize roundSequence: number;
    @autoserialize roundType: 'POOL' | 'BRACKET';
    @autoserialize roundStatus: RoundStatus;
    @autoserialize finalRound: boolean;
    @autoserialize costPerPlayer: number;
    @autoserialize matchGlobal: boolean;
    @autoserializeAs(Date) createDate: Date;
    @autoserialize durationBetweenActivitiesSeconds: number;
    @autoserialize matchPlayerCount: number;
    @autoserialize maximumPlayerCount: number;
    @autoserialize minimumMatchCount: number;
    @autoserialize roundPurse: number;
    @autoserialize minimumActivityToWinCount: number;
    @autoserialize maximumActivityCount: number;
    @autoserialize activityMaximumDurationSeconds: number;
    @autoserialize playerMaximumDurationSeconds: number;
    @autoserialize currentPlayerCount: number;
    @autoserialize roundActivityType: string;
    @autoserialize roundActivityValue: string;
    @autoserializeAs(String, 'categories') categoryIds: string[];
    @autoserializeAs(Date) expectedOpenDate: Date;
    userJoinedRound: boolean;
    matchedPlayer?: MatchPlayer;
    subscriberMatchPlayer?: MatchPlayer;
    encryptedQuestions: QuestionSyncMessage[];
    questions: Question[];
    result?: RoundPlayer;
    finished = new BehaviorSubject(false);

    get isBracketRound(): boolean {
        return this.roundType === 'BRACKET';
    }

    setQuestionResult(result: QuestionResultSyncMessage): void {
        const questionId = result.matchQuestion.questionId;
        let decryptedQuestion = Util.find(this.questions, 'id', questionId);
        if (!decryptedQuestion) {
            decryptedQuestion = this.getDecryptedQuestion(result);
        }
        if (!decryptedQuestion)
            return;

        decryptedQuestion.correctAnswerId = result.correctAnswerId;
        result.subscriberQuestionAnswers.forEach(answer => {
            if (answer.subscriberId === result.subscriberId) {
                decryptedQuestion!.answer = answer;
            } else {
                decryptedQuestion!.opponentAnswer = answer;
            }
        })
    }

    isFinished(): boolean {
        if (this.finished.getValue())
            return true;

        const maxQuestionCountBeforeGivingUp = this.maximumActivityCount;
        const numQuestions = this.questions.length;
        const numQuestionsToWin = this.minimumActivityToWinCount;

        if (numQuestions >= maxQuestionCountBeforeGivingUp) {
            this.finished.next(true);
            return true;
        } else {
            let playerCorrectCount = 0;
            let opponentCorrectCount = 0;
            for (const question of this.questions) {
                if (question.answer && question.answer.didWin) {
                    playerCorrectCount++;
                } else if (question.opponentAnswer && question.opponentAnswer.didWin) {
                    opponentCorrectCount++;
                }
            }

            const result = playerCorrectCount > numQuestionsToWin || opponentCorrectCount > numQuestionsToWin;
            this.finished.next(result);
            return result;
        }
    }

    protected initialize(): void {
        this.roundNames = {};
        this.categoryIds = [];
        this.encryptedQuestions = [];
        this.questions = [];
    }

    private getDecryptedQuestion(result: QuestionResultSyncMessage): Question | null {
        const subscriberQuestionAnswers = Util.keyBy(result.subscriberQuestionAnswers, 'id');
        for (const encrypted of this.encryptedQuestions) {
            const match = subscriberQuestionAnswers[encrypted.subscriberQuestionAnswerId];
            if (match) {
                const questionStr = Util.decryptAES(match.questionDecryptKey, encrypted.encryptedQuestion);
                const question = new Question(JSON.parse(questionStr));
                this.questions.push(question);
                return question;
            }
        }
        return null;
    }

}

/*
 PENDING - round has been created in the admin API but isn't visible to users yet
 CANCELLED
 VISIBLE - round is visible to users (i.e. will be published in the WDS docs), but can't yet be joined
 OPEN - round is ready to join (or leave)
 FULL - round is open, but nobody else can join
 INPLAY - the round is currently running (only applies to BRACKET rounds) and can't be joined or left
 CLOSED
 */
type RoundStatus = 'PENDING' | 'CANCELLED' | 'VISIBLE' | 'OPEN' | 'FULL' | 'INPLAY' | 'CLOSED';

//
// "roundPurse": optional double - for ShoutMillionaire and TippingPoint, the final round in a game contains the actual payout amount. Ignore the purse on any other round,
//     "costPerPlayer": optional double - for ShoutMillionaire and TippingPoint, the first round in a game contains the cost to join the game. Ignore this value for other rounds
// "minimumActivityToWinCount": int - how many questions a player must answer correctly to win a match
// "maximumActivityCount": optional int - how many questions to ask before giving up if nobody in a match has won yet (i.e. if
//                                        nobody gets enough questions right after 5 questions give up and both players lose). null=no limit
// "playerMaximumDurationSeconds": int - how many seconds a player has to answer a question before they time out
// "durationBetweenActivitiesSeconds": int - how many seconds to wait before sending the next question,?
