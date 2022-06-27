import {Component, OnInit} from '@angular/core';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {environment} from 'environments/environment';
import {APP_VERSION} from 'environments/version';
import {GameService} from 'app/shared/services/game.service';
import {Router} from '@angular/router';
import {Game} from 'app/model/game';
import {Subject} from 'rxjs/Subject';
import {ChangelogComponent} from '../changelog/changelog.component';
import {lastSeenVersionLS} from '../../constants';

const STRINGS = environment.strings.user;

@Component({
    selector: 'sh-me',
    templateUrl: './me.component.html',
    styleUrls: ['./me.component.scss']
})
export class MeComponent implements OnInit {

    subscriber = this.subscriberService.subscriber;
    version = APP_VERSION;
    versionClicks = 0;
    loadingSubscriberGames = false;
    hasNewChangelog = false;
    loggedIn = true;

    constructor(public subscriberService: SubscriberService, private dialogService: DialogService,
                private gameService: GameService, private router: Router) {
    }

    get numWins(): number {
        let count = 0;
        this.subscriber.gamePlayers.forEach(player => {
            if (player.payoutAwardedAmount && player.payoutAwardedAmount > 0)
                count++;
        });
        return count;
    }

    ngOnInit(): void {
        const lastSeenVersion = +(localStorage.getItem(lastSeenVersionLS) || 0);
        this.hasNewChangelog = APP_VERSION > lastSeenVersion;
        this.loggedIn = this.subscriberService.isLoggedIn();

        this.load(this.subscriber.games.length === 0);
    }

    gotoGame(game: Game): void {
        this.router.navigate(['/game/', game.id]);
    }

    versionClick(): void {
        this.versionClicks++;
        setTimeout(() => {
            this.versionClicks--;
        }, 1350);
        if (this.versionClicks === 5) {
            this.dialogService.open(ChangelogComponent);
            this.hasNewChangelog = false;
        }
    }

    async handleRefresh(event: Subject<void>): Promise<void> {
        if (this.subscriberService.isLoggedIn()) {
            await this.subscriberService.loadPublicProfile(this.subscriber);
        }
        event.next();

        this.load(true);
    }

    async load(reload = false): Promise<void> {
        if (this.subscriberService.isLoggedIn()) {
            this.loadingSubscriberGames = reload;
            await this.gameService.loadGamesForSubscriber();
            this.loadingSubscriberGames = false;
        }
    }
}
