import {Component, Input, OnInit} from '@angular/core';
import {Util} from '../../util';
import {Subject} from 'rxjs/Subject';
import {Game} from '../../model/game';
import {Router} from '@angular/router';
import {GameService} from 'app/shared/services/game.service';
import {Category} from '../../model/category';
import {CategoryFilter} from '../filter/filter.component';

@Component({
    selector: 'sh-my-games',
    templateUrl: './my-games.component.html',
    styleUrls: ['./my-games.component.scss']
})
export class MyGamesComponent implements OnInit {

    trackByGame = Util.trackByGame;
    canPlayNow = Util.canPlayNow;
    @Input() categoryFilter: CategoryFilter;

    constructor(private router: Router, public gameService: GameService) {
    }

    get games(): Game[] {
        return this.gameService.games.filter((game) => {
            return Util.shouldShowGame(game) && game.userJoinedGame && Util.hasCategory(game, this.categoryFilter)
        }).sort((a, b) => {
            const canA = Util.canPlayNow(a);
            const canB = Util.canPlayNow(b);
            return canA === canB ? 0 : (canA && !canB ? -1 : 1);
        });
    }

    ngOnInit(): void {
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
