import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {Subject} from 'rxjs/Subject';
import {NavigationEnd, NavigationStart, Router, RouterLinkActive} from '@angular/router';
import {AllGamesComponent} from './all-games/all-games.component';
import {MyGamesComponent} from './my-games/my-games.component';
import {MeComponent} from './me/me.component';
import {GameService} from 'app/shared/services/game.service';
import {Game} from 'app/model/game';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {Navigation} from 'selenium-webdriver';
import {Category} from '../model/category';
import {CategoryFilter} from './filter/filter.component';

@Component({
    selector: 'sh-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {

    @ViewChild('allGamesLink')
    allGamesLink: RouterLinkActive;

    @ViewChild('myGamesLink')
    myGamesLink: RouterLinkActive;

    @ViewChild('meLink')
    meLink: RouterLinkActive;

    @ViewChild(AllGamesComponent)
    allGamesComponent: AllGamesComponent;

    @ViewChild(MyGamesComponent)
    myGamesComponent: MyGamesComponent;

    @ViewChild(MeComponent)
    meComponent: MeComponent;

    categoryFilter: CategoryFilter = 'all';

    private navigations: { [key: string]: number } = {};
    private currentNav: string;
    private ngUnsubscribe = new Subject();

    constructor(public subscriberService: SubscriberService, private gameService: GameService,
                private router: Router, private dialog: DialogService) {
        this.trackNavigationScrolling();
    }

    async ngOnInit(): Promise<void> {
        const games = await this.gameService.getGames();
        this.parseGames(games);
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    handleRefresh(event: Subject<void>): void {
        let component: AllGamesComponent | MyGamesComponent | MeComponent | null = null;
        if (this.allGamesLink.isActive)
            component = this.allGamesComponent;
        else if (this.myGamesLink.isActive)
            component = this.myGamesComponent;
        else if (this.meLink.isActive)
            component = this.meComponent;

        if (component) {
            component.handleRefresh(event);
        } else {
            event.next();
        }

    }

    private parseGames(games: Game[]): void {
        for (const game of games) {
            if (game.costToJoin === 0 && !game.userJoinedGame && game.hasPoolPlayStarted && game.gameStatus === 'OPEN') {
                const dialog = this.dialog.open({
                    body: 'The free game "' + game.gameNames.en + '" is ready to be played!',
                    buttons: [{
                        text: 'Play Now!'
                    }],
                    disableClose: true
                });

                dialog.onClose.subscribe(() => {
                    this.router.navigate(['/game/', game.id], {
                        queryParams: {
                            playNow: true
                        }
                    })
                });
            } else if (game.gameStatus === 'INPLAY' && game.userJoinedGame && !game.userEliminated) {
                const dialog = this.dialog.open({
                    body: 'Your game "' + game.gameNames.en + '" is in bracket play!',
                    buttons: [{
                        text: 'Play Now!'
                    }],
                    disableClose: true
                });

                dialog.onClose.subscribe(() => {
                    this.router.navigate(['/game/', game.id]);
                });
            }
        }
    }

    trackNavigationScrolling(): void {
        this.router.events.takeUntil(this.ngUnsubscribe).subscribe((event) => {
            if (event instanceof NavigationStart && this.currentNav) {
                this.navigations[this.currentNav] = document.scrollingElement!.scrollTop;
            } else if (event instanceof NavigationEnd) {
                this.currentNav = event.url;
                setTimeout(() => {
                    document.scrollingElement!.scrollTop = this.navigations[event.url] ? this.navigations[event.url] : 0;
                })
            }
        });
    }
}
