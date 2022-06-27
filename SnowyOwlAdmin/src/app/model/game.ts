import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs } from 'cerialize';
import { Round } from 'app/model/round';
import {PayoutModel} from './payout-model';

export class Game extends BaseModel<Game> {
    @autoserialize id!: string;
    @autoserialize gameStatus!: GameStatus;

    @autoserializeAs(Object) gameNames!: any;
    @autoserializeAs(Object) fetchingActivityTitles!: any;
    @autoserializeAs(Object) submittingActivityTitles!: any;
    @autoserializeAs(Object) gameDescriptions!: any;
    @autoserializeAs(String) allowableLanguageCodes!: string[];
    @autoserializeAs(String) forbiddenCountryCodes!: string[];
    @autoserialize bracketEliminationCount!: number;
    @autoserialize gameEngine!: string;
    @autoserialize allowBots!: boolean;
    @autoserialize allowableAppIds!: number[];
    @autoserialize includeActivityAnswersBeforeScoring!: boolean;
    @autoserialize engineType!: 'MULTI_LIFE' | 'SINGLE_LIFE';
    @autoserialize gamePhotoUrl?: string;
    @autoserialize guideUrl?: string;
    @autoserializeAs(Object) guideHtmls!: any;
    @autoserializeAs(Date) pendingDate!: Date;
    @autoserializeAs(Date) openDate!: Date;
    @autoserializeAs(Date) inplayDate!: Date;
    @autoserialize gameType!: 'DEFAULT' | 'TESTER';
    @autoserializeAs('useDoctoredTimeForBots') speedUpBots!: boolean;
    @autoserialize fillWithBots!: boolean;
    @autoserialize pairImmediately!: boolean;
    @autoserialize productionGame!: boolean;
    @autoserialize privateGame!: boolean;
    @autoserialize inviteCode!: string;
    @autoserialize autoStartPoolPlay!: boolean;
    @autoserialize autoStartBracketPlay!: boolean;
    @autoserialize autoBracketPlayPreStartNotificationTimeMs!: number;

    @autoserialize startingLivesCount!: number;
    @autoserialize additionalLivesCost!: number;
    @autoserialize maxLivesCount!: number;

    @autoserializeAs(Round) rounds!: Round[];

    get gameStatus2(): string {
        if (this.gameStatus === 'OPEN') {
            let startedStatus = ' - Pool started';
            if (!this.hasPoolPlayStarted && !this.hasNoPoolRounds) {
                startedStatus = '- Pool not started';
            } else if (!this.hasPoolPlayStarted && this.hasNoPoolRounds) {
                startedStatus = '- No Pool rounds';
            }
            return `OPEN${startedStatus}`;
        }
        return this.gameStatus;
    }

    get expectedStartDate(): Date | null {
        return this.rounds && this.rounds.length ? this.rounds[this.rounds.length - 1].expectedOpenDate : null;
    }

    get hasPoolPlayStarted(): boolean {
        if (this.gameStatus !== 'OPEN')
            return false;

        return !!this.rounds.length && this.rounds[0].roundStatus !== 'VISIBLE';
    }

    get hasNoPoolRounds(): boolean {
        return this.rounds.length === 1;
    }

    protected initialize(): void {
        this.allowableLanguageCodes = ['en'];
        this.forbiddenCountryCodes = [];
        this.allowableAppIds = [];
        this.gameNames = {};
        this.gameDescriptions = {};
        this.fetchingActivityTitles = {};
        this.submittingActivityTitles = {};
        this.rounds = [];
        this.guideHtmls = {};
    }
}

type GameStatus = 'PENDING' | 'OPEN' | 'INPLAY' | 'CLOSED' | 'CANCELLED';
