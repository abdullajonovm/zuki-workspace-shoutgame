import {Injectable} from '@angular/core';
import {HttpService, ShoutErrorResponse, ShoutResponse, ShoutSuccessResponse} from 'app/shared/services/http.service';

@Injectable()
export class GameplayService {
    constructor(private httpService: HttpService) {
    }

    async joinGame(gameId: string): Promise<JoinGameResponse> {
        return await this.httpService.sendCollectorRequest<JoinGameResponse>('/snowl/game/join', {gameId}).toPromise();
    }

    async beginPoolPlay(gameId: string): Promise<BeginPoolPlayResponse> {
        return await this.httpService.sendCollectorRequest<BeginPoolPlayResponse>('/snowl/game/beginPoolPlay', {gameId}).toPromise();
    }

    async beginBracketPlay(gameId: string): Promise<BeginBracketPlayResponse> {
        return await this.httpService.sendCollectorRequest<BeginBracketPlayResponse>('/snowl/game/beginBracketPlay', {gameId}).toPromise();
    }

    async getDecryptKey(subscriberQuestionAnswerId: string): Promise<DecryptKeyResponse> {
        return await this.httpService.sendSynchronousCollectorRequest<DecryptKeyResponse>('/snowl/question/getDecryptKey', {
            subscriberQuestionAnswerId
        }).toPromise();
    }

    async submitAnswer(subscriberQuestionAnswerId: string, selectedAnswerId: string): Promise<SubmitAnswerResponse> {
        return await this.httpService.sendCollectorRequest<SubmitAnswerResponse>('/snowl/question/submitAnswer', {
            subscriberQuestionAnswerId, selectedAnswerId
        }).toPromise()
    }

    async getBracketCountdownTime(gameId: string): Promise<number> {
        try {
            const response = await this.httpService.getWdsDoc<BracketCountdownTimer>(`/${gameId}/bracketplay_countdown.json`).toPromise();
            return response.beginsInMs;
        } catch (e) {
            return -1;
        }
    }
}

interface JoinGameResponse extends ShoutErrorResponse {
    gameNotOpen?: boolean;
    accessDenied?: boolean;
    alreadyJoined?: boolean;
    noOpenRounds?: boolean;
}

interface BeginPoolPlayResponse extends ShoutErrorResponse {
    notInGame?: boolean,
    roundNotFound?: boolean, //  (this means a config error with the game itself)
    waitForBracketPlayToBegin?: boolean, // - the user has already played all pool rounds and must wait for BRACKET play
    roundLocked?: boolean, // - a server contention issue. Very unlikely to happen. Client should wait a small amount of time (100ms) and then retry
    alreadyQueued?: boolean, // - the player has already asked to begin POOL play and is in the queue to get paired
    roundNotOpen?: boolean,
    insufficientFunds?: boolean
}

interface BeginBracketPlayResponse extends ShoutErrorResponse {
    notInGame?: boolean;
    gameNotInBracketPlay?: boolean;
}

type DecryptKeyResponse = DecryptKeySuccess | DecryptKeyError;

interface DecryptKeyError extends ShoutErrorResponse {
    illegalAnswerState?: boolean;
}

interface DecryptKeySuccess extends ShoutSuccessResponse {
    decryptKey: string;
    questionPresentedTimestamp: string;
}

interface SubmitAnswerResponse extends ShoutResponse {
    illegalAnswerState?: boolean, // - user is trying to answer someone else's question
    duplicateAnswer?: boolean, // - user has already answered
    answerTooLate?: boolean,
    questionAnsweredButNotShown?: boolean // - the decrypt key was never requested
}

interface BracketCountdownTimer {
    beginsInMs: number;
}
