import {Component, Input} from '@angular/core';
import {GameService} from 'app/shared/services/game.service';
import {Subject} from 'rxjs/Subject';
import {Util} from '../../util';
import {Router} from '@angular/router';
import {Game} from '../../model/game';
import {Category} from '../../model/category';
import {CategoryFilter} from '../filter/filter.component';

@Component({
    selector: 'sh-all-games',
    templateUrl: './all-games.component.html',
    styleUrls: ['./all-games.component.scss']
})
export class AllGamesComponent {
    trackByGame = Util.trackByGame;
    canPlayNow = Util.canPlayNow;
    @Input() categoryFilter: CategoryFilter;

    constructor(public gameService: GameService, private router: Router) {
    }

    get games(): Game[] {
        return this.gameService.games.filter((game) => {
            return Util.shouldShowGame(game) && !game.userJoinedGame && Util.hasCategory(game, this.categoryFilter)
        }).sort((a, b) => {
            const canA = Util.canPlayNow(a);
            const canB = Util.canPlayNow(b);
            return canA === canB ? 0 : (canA && !canB ? -1 : 1);
        });
    }

    async handleRefresh(event: Subject<void>): Promise<void> {
        await this.gameService.getGames();
        event.next();
    }

    goToGame(game: Game, playNow = false): void {
        this.router.navigate(['/game', game.id], {
            queryParams: {
                playNow: playNow || undefined
            }
        })
    }

}
