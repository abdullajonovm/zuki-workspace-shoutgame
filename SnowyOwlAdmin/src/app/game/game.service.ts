import {Injectable} from '@angular/core';
import { PlayerInfo } from '../model/player-info';
import {HttpService, ShoutResponse} from '../shared/services/http.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Game} from 'app/model/game';
import {Round} from 'app/model/round';

import {Winner} from 'app/model/winner';
import {Payout} from '../model/payout';

@Injectable()
export class GameService {
    private games?: Game[];

    constructor(private httpService: HttpService) {
    }

    async findGames(statuses: string): Promise<Game[]> {
        const response = await this.httpService.sendRequest<FindGamesResponse>('/snowladmin/games/list', {statuses}).toPromise();
        if (response.games) {
            response.games = response.games.map(game => new Game(game));
        } else {
            response.games = [];
        }

        this.games = response.games;
        await Promise.all(this.games.map(game => this.loadGameRounds(game)));
        return response.games;
    }

    async getGameById(id: string): Promise<Game> {
        let game: Game = new Game();
        if (this.games) {
            for (const gm of this.games) {
                if (gm.id === id)
                    game = gm;
            }
        }
        const updatedGame = await this.httpService.sendRequest<GetGameResponse>('/snowladmin/game/get', {gameId: id}).toPromise();
        game.fillFromJson(updatedGame.game);
        await this.loadGameRounds(game);
        return game;
    }

    async notifyFreeplayers(gameId: string): Promise<ShoutResponse> {
        return await this.httpService.sendRequest('/snowladmin/game/notifyFreeplayers', {gameId}).toPromise();
    }


    async loadGameRounds(game: Game): Promise<void> {
        const response = await this.httpService.sendRequest<GetGameRoundsResponse>('/snowladmin/game/getrounds',
            {gameId: game.id}).toPromise();

        game.rounds = response.rounds ? response.rounds.map(round => new Round(round)) : [];
    }

    openGame(game: Game): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/game/open', {gameId: game.id}).pipe(
            map((response) => {
                if (response.success) {
                    game.gameStatus = 'OPEN';
                }

                return response;
            })
        )
    }

    startPoolPlay(game: Game): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/game/beginPoolPlay', {gameId: game.id});
    }

    startBracketPlay(game: Game, beginsInMilliseconds: number): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/game/beginBracketPlay', {
            gameId: game.id,
            beginsInMs: beginsInMilliseconds
        }).pipe(
            map(response => {
                if (response.success) {
                    game.gameStatus = 'INPLAY';
                }

                return response;
            })
        );
    }

    cancelGame(game: Game): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/game/cancel', {gameId: game.id}).pipe(
            map(response => {
                if (response.success) {
                    game.gameStatus = 'CANCELLED';
                }
                return response;
            })
        );
    }

    createGame(options: CreateGameOptions): Observable<ShoutResponse> {
        const toSend: any = options;
        toSend.game = options.game.toJson();
        toSend.expectedStartDateForBracketPlay = options.expectedStartDateForBracketPlay.toISOString();
        toSend.expectedStartDateForPoolPlay = options.expectedStartDateForPoolPlay.toISOString();

        return this.httpService.sendRequest('/snowladmin/game/create', toSend);
    }

    cloneGame(game: Game, expectedStartDate: Date, expectedStartDateForPoolPlay: Date,
              expectedStartDateForBracketPlay: Date, gameNames: any): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/game/clone', {
            gameId: game.id,
            expectedStartDateForPoolPlay: expectedStartDateForPoolPlay.toISOString(),
            expectedStartDateForBracketPlay: expectedStartDateForBracketPlay.toISOString(),
            gameNames: JSON.stringify(gameNames)
        });
    }

    async getWinners(game: Game): Promise<Winner[]> {
        const result = await this.httpService.sendRequest<GetWinnersResponse>('/snowladmin/game/getWinners', {gameId: game.id}).toPromise();
        return result.winners.map(winner => {
            if (winner.amount && winner.amount > 1) {
                winner.amount = Math.floor(winner.amount);
            }
            return new Winner(winner)
        }).sort((a, b) => a.amount > b.amount ? -1 : 1);
    }

    getPlayerInfo(gameId: string): Observable<PlayerInfo[]> {
        return this.httpService.sendRequest<PlayerInfoResponse>('/snowladmin/game/viewPlayerInfo', {gameId}).pipe(
            map(resp => resp.playerInfo)
        );
    }
}

interface FindGamesResponse extends ShoutResponse {
    games: Game[];
}

interface GetGameRoundsResponse extends ShoutResponse {
    rounds: Round[];
}

interface GetGameResponse extends ShoutResponse {
    game: Game;
}

interface GetWinnersResponse extends ShoutResponse {
    winners: Winner[];
}

interface PlayerInfoResponse extends ShoutResponse {
    playerInfo: PlayerInfo[];
}


export interface CreateGameOptions {
    game: Game;
    expectedStartDateForPoolPlay: Date;
    expectedStartDateForBracketPlay: Date;
    photoUrl?: string;
    imageExternallyManaged?: boolean;
    minimumPayoutAmount: number;
    payoutModelId: number;
    giveSponsorPlayerWinningsBackToSponsor: boolean;
}
