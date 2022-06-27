import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs, inheritSerialization} from 'cerialize';
import {RoundPlayer} from 'app/model/round-player';
import {MatchPlayer} from 'app/model/match-player';
import {MatchQuestion} from 'app/model/match-question';
import {SubscriberQuestionAnswer} from 'app/model/subscriber-question-answer';
import {GamePlayer} from 'app/model/game-player';

export class SyncMessage extends BaseModel<SyncMessage> {
    @autoserialize id: string;
    @autoserialize subscriberId: number;
    @autoserialize messageType: MessageType;
    @autoserializeAs(Date) createDate: Date;
    @autoserialize contextualId: string;

    payload?: any;

    @autoserializeAs('payload')
    private set _payloadParser(payload: string) {
        this.payload = JSON.parse(payload);
    };

    static createSyncMessage(json: { messageType: MessageType }): SyncMessage {
        switch (json.messageType) {
            case 'joined_round':
                return new JoinedRoundSyncMessage(json);
            case 'abandoned_round':
                return new AbandonedRoundSyncMessage(json);
            case 'user_matched':
                return new UserMatchedSyncMessage(json);
            case 'question':
                return new QuestionSyncMessage(json);
            case 'question_result':
                return new QuestionResultSyncMessage(json);
            case 'match_result':
                return new MatchResultSyncMessage(json);
            case 'game_result':
                return new GameResultSyncMessage(json);
            case 'joined_game':
                return new JoinedGameSyncMessage(json);
            case 'abandoned_game':
                return new AbandonedGameSyncMessage(json);
            case 'eliminated':
                return new EliminatedSyncMessage(json);
            default:
                throw new Error('Unexpected Sync Message Type');
        }
    }

}

@inheritSerialization(SyncMessage)
export class JoinedGameSyncMessage extends SyncMessage {
}

@inheritSerialization(SyncMessage)
export class AbandonedGameSyncMessage extends SyncMessage {
}

@inheritSerialization(SyncMessage)
export class EliminatedSyncMessage extends SyncMessage {
}

@inheritSerialization(SyncMessage)
export class JoinedRoundSyncMessage extends SyncMessage {
    messageType: 'joined_round';
    roundPlayer: RoundPlayer;

    set payload(payload: { roundPlayer: RoundPlayer }) {
        this.roundPlayer = new RoundPlayer(payload.roundPlayer);
    };
}

@inheritSerialization(JoinedRoundSyncMessage)
export class AbandonedRoundSyncMessage extends SyncMessage {
    messageType: 'abandoned_round';
    roundPlayer: RoundPlayer;

    set payload(payload: { roundPlayer: RoundPlayer }) {
        this.roundPlayer = new RoundPlayer(payload.roundPlayer);
    };
}

@inheritSerialization(SyncMessage)
export class UserMatchedSyncMessage extends SyncMessage {
    messageType: 'user_matched';
    players: MatchPlayer[];

    set payload(payload: { players: MatchPlayer[] }) {
        this.players = payload.players.map(player => new MatchPlayer(player));
    }
}

@inheritSerialization(SyncMessage)
export class QuestionSyncMessage extends SyncMessage {
    messageType: 'question';
    encryptedQuestion: string;
    subscriberQuestionAnswerId: string;

    set payload(payload: { question: string, subscriberQuestionAnswerId: string }) {
        this.encryptedQuestion = payload.question;
        this.subscriberQuestionAnswerId = payload.subscriberQuestionAnswerId;
    }
}

@inheritSerialization(SyncMessage)
export class QuestionResultSyncMessage extends SyncMessage {
    messageType: 'question_result';
    matchQuestion: MatchQuestion;
    correctAnswerId: string;
    subscriberQuestionAnswers: SubscriberQuestionAnswer[];

    set payload(payload: { matchQuestion: MatchQuestion, correctAnswerId: string, subscriberQuestionAnswers: SubscriberQuestionAnswer[] }) {
        this.matchQuestion = new MatchQuestion(payload.matchQuestion);
        this.correctAnswerId = payload.correctAnswerId;
        this.subscriberQuestionAnswers = payload.subscriberQuestionAnswers.map(ans => new SubscriberQuestionAnswer((ans)))
    }
}

@inheritSerialization(SyncMessage)
export class MatchResultSyncMessage extends SyncMessage {
    messageType: 'match_result';
    gamePlayer: GamePlayer;
    roundPlayer: RoundPlayer;

    set payload(payload: { gamePlayer: GamePlayer, roundPlayer: RoundPlayer }) {
        this.gamePlayer = new GamePlayer(payload.gamePlayer);
        this.roundPlayer = new RoundPlayer(payload.roundPlayer);
    }
}

@inheritSerialization(SyncMessage)
export class GameResultSyncMessage extends SyncMessage {
    messageType: 'game_result';
    gamePlayer: GamePlayer;
    roundPlayers: RoundPlayer[];

    set payload(payload: { gamePlayer: GamePlayer, roundPlayers: RoundPlayer[] }) {
        this.gamePlayer = new GamePlayer(payload.gamePlayer);
        this.roundPlayers = payload.roundPlayers.map(r => new RoundPlayer(r));
    }
}


type MessageType = 'joined_game' | 'abandoned_game' | 'joined_round' | 'abandoned_round' |
    'user_matched' | 'question' | 'question_result' | 'match_result' | 'eliminated' | 'game_result';
