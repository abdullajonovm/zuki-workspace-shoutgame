import {Injectable} from '@angular/core';
import {Game, PayoutLevel} from 'app/model/game';
import 'rxjs/add/observable/of';
import {HttpService, ShoutResponse} from 'app/shared/services/http.service';
import {Category} from 'app/model/category';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {environment} from 'environments/environment';
import {Util} from '../../util';
import {SyncService} from './sync.service';
import {Router} from '@angular/router';
import {LogService} from 'app/shared/services/log.service';

const CATEGORY_MAP_NAME = 'categoryMap';
const CLEAR_MAP_DATE = 'clearGameIdMapDate';
const ALL_GAMES_CATEGORY = new Category({
    id: '*',
    categoryName: {
        en: environment.strings.game.allCategories,
        es: environment.strings.game.allCategories
    }
});
const REFRESH_DISTANCE = 24 * 60 * 60 * 1000; // one day
@Injectable()
export class GameService {

    games: Game[] = [];
    loadingGames = false;
    categoryMap: { [key: string]: Category };
    private called = false;

    constructor(private http: HttpService, private syncService: SyncService,
                private router: Router, private logService: LogService, private subscriberService: SubscriberService) {
        // TODO: check for last updated timestamp
        const clearDate = localStorage.getItem(CLEAR_MAP_DATE);
        if (clearDate && new Date() > new Date(clearDate)) {
            this.logService.log('Clearing the category id map');
            localStorage.removeItem(CATEGORY_MAP_NAME);
            localStorage.removeItem(CLEAR_MAP_DATE);
        }

        const storageMap = localStorage.getItem(CATEGORY_MAP_NAME);
        this.categoryMap = storageMap ? JSON.parse(storageMap) : {'*': ALL_GAMES_CATEGORY};
        for (const key in this.categoryMap) {
            if (this.categoryMap.hasOwnProperty(key)) {
                this.categoryMap[key] = new Category(this.categoryMap[key]);
            }
        }

        this.syncService.syncMessages.subscribe((syncMessage) => {
            for (const game of this.games) {
                if (game.id === syncMessage.contextualId) {
                    if (!this.router.isActive(`/game/${game.id}`, false)) {
                        this.router.navigate(['/game', game.id]);
                    }
                    game.parseSyncMessages([syncMessage]);

                    return;
                }
            }
        })
    }

    async getGames(): Promise<Game[]> {
        this.loadingGames = true;
        const allGames = await this.http.getWdsDoc<Game[]>('/snowl_games.json').toPromise();
        let games = [];
        const subscriber = this.subscriberService.subscriber;
        for (const game of allGames) {
            if (this.subscriberService.isLoggedIn()) {
                if (Util.includes(game.forbiddenCountryCodes, subscriber.countryCode)) {
                    continue;
                } else if (!Util.includes(game.allowableLanguageCodes, subscriber.languageCode)) {
                    continue;
                }
            }

            games.push(game);
        }

        games = await Promise.all(games.map(game => this.getGameById(game.id, {
            forceRefresh: false,
            skipAdding: true,
            skipSyncMessages: !this.subscriberService.isLoggedIn()
        })));
        this.games = games;

        this.loadingGames = false;
        return this.games;
    }

    async getGameById(id: string, options: GetGameOptions = {}): Promise<Game> {
        let result: Game | null = null;
        let existed = false;
        for (const game of this.games) {
            if (game.id === id) {
                result = game;
                existed = true;
                break;
            }
        }
        result = result || new Game();
        if (!result.gameService)
            result.gameService = this;
        if (!result.subscriberService)
            result.subscriberService = this.subscriberService;
        if (!result.logService)
            result.logService = this.logService;

        if (!result.rounds.length || options.forceRefresh) {
            result.fillFromJson(await this.http.getWdsDoc<Game>(`/${id}/game.json`).toPromise());
        }
        if (!result.payoutLevels.length || options.forceRefresh) {
            const gamePayout = await this.http.getWdsDoc<GamePayout>(`/${id}/payout.json`).toPromise();
            result.payoutLevels = gamePayout.payouts.map(payout => {
                if (payout.val > 1) {
                    payout.val = Math.floor(payout.val);
                }
                return payout;
            });

            result.payout = gamePayout.playerPot;
        }
        if (!existed || options.forceRefresh) {
            await this.loadCategoriesForGame(result);
        }
        if (!existed && !options.skipAdding) {
            this.games.push(result);
        }
        if (options.forceRefresh) {
            result.lastSyncMessageDate = undefined;
        }
        if (!options.skipSyncMessages) {
            const syncMessages = await this.syncService.getSyncMessagesForGame(result.id, result.lastSyncMessageDate);
            result.parseSyncMessages(syncMessages);
        }

        return result;
    }

    async loadCategory(categoryId: string): Promise<Category> {
        if (this.categoryMap[categoryId])
            return this.categoryMap[categoryId];

        const resp = await this.http.sendCollectorRequest<CategoryResponse>(
            '/snowl/getQuestionCategoriesFromCategoryIds', {categories: [categoryId]}, false).toPromise();
        this.categoryMap[categoryId] = resp.questionCategories[0];

        return resp.questionCategories[0];
    }

    async loadGamesForSubscriber(): Promise<void> {
        await this.subscriberService.loadPlayerDetails();
        const subscriber = this.subscriberService.subscriber;
        const gamePlayerMap = Util.keyBy(subscriber.gamePlayers, 'gameId');

        const games = await Promise.all(subscriber.gamePlayers.map(pg => this.getGameById(pg.gameId, {
            forceRefresh: false,
            skipAdding: true,
            skipSyncMessages: true
        })));
        subscriber.games = games.sort((a, b) => a.closedDate > b.closedDate ? -1 : 1);
        subscriber.games.forEach(game => {
            game.gamePlayer = gamePlayerMap[game.id];
        });

    }

    async getOutstandingMatches(gameId: string): Promise<OutstandingMatches> {
        return await this.http.getWdsDoc<OutstandingMatches>(`/${gameId}/bracketplay_outstanding_matches.json`).toPromise();
    }

    private async loadCategoriesForGame(game: Game): Promise<void> {
        const newCategoryIds: string[] = [];
        game.categoryIds.forEach(category => {
            if (!this.categoryMap[category])
                newCategoryIds.push(category);
        });

        if (newCategoryIds.length) {
            // loads any new categories we need
            const categoriesResponse = await this.http.sendCollectorRequest<CategoryResponse>(
                '/snowl/getQuestionCategoriesFromCategoryIds', {categories: newCategoryIds.join()}, false).toPromise();

            categoriesResponse.questionCategories.forEach(category => {
                this.categoryMap[category.id] = new Category(category);
            });
        }


        localStorage.setItem(CATEGORY_MAP_NAME, JSON.stringify(this.categoryMap));

        if (!localStorage.getItem(CLEAR_MAP_DATE)) {
            const date = new Date(Date.now() + REFRESH_DISTANCE);
            localStorage.setItem(CLEAR_MAP_DATE, date.toISOString());
        }
    }
}

interface CategoryResponse extends ShoutResponse {
    questionCategories: Category[];
}

interface GamePayout {
    playerPot: number;
    payouts: PayoutLevel[]
}

interface GetGameOptions {
    forceRefresh?: boolean;
    skipAdding?: boolean;
    skipSyncMessages?: boolean;
}

export interface OutstandingMatches {
    totalMatchesForRound: number;
    roundSequence: number;
    numOutstandingMatches: number;
}
