import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';
import {Round} from 'app/model/round';
import {GamePlayer} from 'app/model/game-player';
import {GameService} from 'app/shared/services/game.service';
import {
    AbandonedRoundSyncMessage,
    EliminatedSyncMessage,
    GameResultSyncMessage,
    JoinedGameSyncMessage,
    JoinedRoundSyncMessage,
    MatchResultSyncMessage,
    QuestionResultSyncMessage,
    QuestionSyncMessage,
    SyncMessage,
    UserMatchedSyncMessage
} from 'app/model/sync-message';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {Subject} from 'rxjs/Subject';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Util} from '../util';
import {LogService} from 'app/shared/services/log.service';

const SYNC_QUERY_TIME = 150;

export class Game extends BaseModel<Game> {
    @autoserialize id: string;
    @autoserialize gameStatus: GameStatus;
    @autoserializeAs(Date) closedDate: Date;
    @autoserializeAs(Object) gameNames: any;
    @autoserializeAs(Object) fetchingActivityTitles: any; // "Retrieving Question" for example. clint should display this value in the UI when retrieving a question
    @autoserializeAs(Object) submittingActivityTitles: any; // "Submitting Answer" for example. client should display this value in the UI when submitting an answer
    @autoserializeAs(String) allowableLanguageCodes: string[];
    @autoserializeAs(String) forbiddenCountryCodes: string[];
    @autoserialize allowableAppIds: number[];
    @autoserialize gamePhotoUrl: string;
    @autoserializeAs(Object) gameDescriptions: any;
    @autoserialize guideUrl?: string;
    @autoserializeAs(Object) guideHtmls: any;
    categoryIds: string[] = [];
    syncMessages = new Subject<SyncMessage>();
    state: BehaviorSubject<GameClientStatus>;
    bracketRound?: Round;
    costToJoin = 0;
    payout = 0;
    gamePlayer?: GamePlayer;
    payoutLevels: PayoutLevel[] = [];
    userJoinedGame = false;
    expectedStartDateForPoolPlay?: Date;
    expectedStartDateForBracketPlay?: Date;
    gameService: GameService;
    subscriberService: SubscriberService;
    logService: LogService;
    lastSyncMessageDate?: Date;
    stateData: any;
    userEliminated = false;
    private parsingMessages = false;
    private waitForQuestionTime = 10;
    private currentRound?: Round;

    get hasPoolPlayStarted(): boolean {
        if (this.gameStatus !== 'OPEN')
            return false;
        return !!this.rounds.length && this.rounds[0].roundStatus !== 'VISIBLE';
    }

    get hasNoPoolRounds(): boolean {
        return this.rounds.length === 1;
    }

    private _rounds: Round[];

    @autoserializeAs(Round)
    get rounds(): Round[] {
        return this._rounds;
    }

    set rounds(value: Round[]) {
        this._rounds = value;
        const categoryIds = [];
        for (const round of value) {
            categoryIds.push(...round.categoryIds);
            if (round.isBracketRound) {
                this.bracketRound = round;
                this.expectedStartDateForBracketPlay = round.expectedOpenDate;
            } else {
                this.expectedStartDateForPoolPlay = round.expectedOpenDate;
            }
            if (round.costPerPlayer)
                this.costToJoin = round.costPerPlayer;
            if (round.roundPurse)
                this.payout = round.roundPurse;
        }
        this.categoryIds = Array.from(new Set(categoryIds));
    }

    parseSyncMessages(syncMessages: SyncMessage[]): void {
        if (this.parsingMessages || !syncMessages.length)
            return;

        if (this.logService)
            this.logService.log('Parsing Sync Messages:', syncMessages);

        this.parsingMessages = true;
        const rounds = Util.keyBy(this._rounds, 'id');

        let newClientStatus: GameClientStatus = this.state.value;
        let currentRound = this.currentRound;
        let stateData: any = null;
        for (const message of syncMessages) {
            newClientStatus = 'DEFAULT';
            if (message instanceof JoinedGameSyncMessage) {
                this.userJoinedGame = true;
            } else if (message instanceof JoinedRoundSyncMessage) {
                currentRound = rounds[message.roundPlayer.roundId];
                if (!currentRound) {
                    currentRound = this.createNewRoundWithId(message.roundPlayer.roundId);
                    rounds[message.roundPlayer.roundId] = currentRound;
                }
                currentRound.userJoinedRound = true;
                newClientStatus = 'WAITING_FOR_MATCH';
                currentRound.roundStatus = currentRound.roundType === 'BRACKET' ? 'INPLAY' : 'OPEN';
            } else if (message instanceof UserMatchedSyncMessage && currentRound) {
                this.parseMatchedUsers(message, rounds);
                newClientStatus = 'WAITING_FOR_QUESTION';
                this.waitForQuestionTime = message.createDate.getTime() + currentRound.durationBetweenActivitiesSeconds - Date.now()
            } else if (message instanceof QuestionSyncMessage && currentRound) {
                currentRound.encryptedQuestions.push(message);
                newClientStatus = 'HAS_QUESTION';
                stateData = message;
            } else if (message instanceof QuestionResultSyncMessage && currentRound) {
                currentRound.setQuestionResult(message);
                newClientStatus = currentRound.isFinished() ? 'DEFAULT' : 'WAITING_FOR_QUESTION';
            } else if (message instanceof MatchResultSyncMessage) {
                const round = rounds[message.roundPlayer.roundId];
                round.result = message.roundPlayer;
                round.roundStatus = 'CLOSED';
            } else if (message instanceof AbandonedRoundSyncMessage) {
                const round = rounds[message.roundPlayer.roundId];
                round.userJoinedRound = false;
            } else if (message instanceof GameResultSyncMessage) {
                this.gamePlayer = message.gamePlayer;
                this.gameStatus = 'CLOSED';
            } else if (message instanceof EliminatedSyncMessage) {
                this.userEliminated = true;
                // TODO: handle this
            }
            this.lastSyncMessageDate = message.createDate;
            this.syncMessages.next(message);
        }
        this.stateData = stateData;
        this.currentRound = currentRound;

        if (this.lastSyncMessageDate)
            this.lastSyncMessageDate.setTime(this.lastSyncMessageDate.getTime() + 1);

        if (this.gameStatus === 'CANCELLED' || this.gameStatus === 'CLOSED')
            newClientStatus = 'DEFAULT';

        if (this.state.value !== newClientStatus) {
            this.state.next(newClientStatus);
        }

        this.parsingMessages = false;


    }

    protected initialize(): void {
        this.allowableLanguageCodes = [];
        this.forbiddenCountryCodes = [];
        this.allowableAppIds = [];
        this.gameNames = {};
        this.fetchingActivityTitles = {};
        this.submittingActivityTitles = {};
        this._rounds = [];
        this.guideHtmls = {};
        this.state = new BehaviorSubject<GameClientStatus>('DEFAULT');
        this.state.subscribe((state) => {
            if (this.logService)
                this.logService.log('Game State Change:', state);
        })
    }

    private parseMatchedUsers(match: UserMatchedSyncMessage, rounds: { [roundId: string]: Round }): void {
        for (const player of match.players) {
            const round = rounds[player.roundId];
            if (player.subscriberId !== this.subscriberService.subscriber.subscriberId) {
                round.matchedPlayer = player;
                this.subscriberService.loadPublicProfile(player);
            } else {
                round.subscriberMatchPlayer = player;
            }
        }
    }

    private createNewRoundWithId(id: string): Round {
        const roundToClone = this.rounds[this.rounds.length - 1];
        const result = new Round(roundToClone.toJSON());
        result.id = id;

        this.rounds.push(result);

        return result;
    }
}

export type GameStatus = 'PENDING' | 'OPEN' | 'INPLAY' | 'CLOSED' | 'CANCELLED';

type GameClientStatus = 'DEFAULT' | 'WAITING_FOR_MATCH' | 'WAITING_FOR_QUESTION' | 'HAS_QUESTION';

export interface PayoutLevel {
    key: number;
    val: number;
}
