import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {GameService} from 'app/shared/services/game.service';
import {Game} from 'app/model/game';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {RoundComponent} from 'app/game/round/round.component';
import {Round} from 'app/model/round';
import {GameplayService} from 'app/shared/services/gameplay.service';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/takeUntil';
import {DialogComponent} from 'app/shared/dialog/dialog.component';
import {environment} from 'environments/environment';
import {NotificationComponent} from 'app/shared/notification/notification.component';
import {DeviceService} from '../shared/services/device.service';
import 'rxjs/add/operator/filter';
import {Util} from '../util';
import {LogService} from 'app/shared/services/log.service';
import {WaitDialogComponent} from 'app/game/wait-dialog/wait-dialog.component';
import {GameGuideDialogComponent} from './game-guide-dialog.component';
import {NavigationService} from '../shared/services/navigation.service';
import {PaymentComponent} from '../shared/payment/payment.component';
import {GameResultSyncMessage} from '../model/sync-message';
import {GameResultsComponent} from './game-results/game-results.component';

const STRINGS = environment.strings.game;

@Component({
    selector: 'sh-game',
    templateUrl: './game.component.html',
    styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit, OnDestroy {
    game: Game;

    nextRound?: Round;
    lastRound?: Round;
    loading = true;
    subscriber = this.subscriberService.subscriber;
    private ngUnsubscribe: Subject<void> = new Subject<void>();
    private roundDialog?: DialogComponent;
    private playOnLoad = false;
    private roundComponentOpen: boolean;
    private waiting: boolean;

    constructor(private route: ActivatedRoute, private gameService: GameService, private dialogService: DialogService,
                private subscriberService: SubscriberService, private gameplayService: GameplayService, private router: Router,
                public deviceService: DeviceService, private logService: LogService, public navigationService: NavigationService,
                private changeDetector: ChangeDetectorRef) {
    }

    get showGameGuide(): boolean {
        const hasGameGuide = (!!this.game.guideUrl && !!this.game.guideUrl.length) || !Util.isEmpty(this.game.guideHtmls);
        if (this.game.hasNoPoolRounds) {
            return this.game.userJoinedGame && hasGameGuide;
        } else {
            return hasGameGuide;
        }
    }

    ngOnInit(): void {
        this.route.params.subscribe((params) => {
            this.loadGame(params.id);
        });

        this.route.queryParams.subscribe(params => {
            if (params.playNow) {
                this.playOnLoad = true;
                this.router.navigate([], {replaceUrl: true});
            }
        });
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    notificationPrefs(): void {
        this.dialogService.open(NotificationComponent);
    }

    viewGameGuide(): void {
        if (this.game.guideUrl && this.game.guideUrl.length) {
            const win = window.open(this.game.guideUrl, '_blank');
            win.focus();
        } else {
            this.dialogService.open(GameGuideDialogComponent, null, this.game);
        }
    }

    poolHelp(): void {
        this.dialogService.open({
            body: STRINGS.poolPlayDescription,
            buttons: [{text: STRINGS.core.ok}]
        });
    }

    async beginPoolPlay(): Promise<void> {
        if (!this.nextRound)
            return;
        if (!this.subscriberService.isLoggedIn()) {
            this.forceLogin('You need to be logged in to play a game!');
            return;
        }

        if (!this.waiting) {
            this.waiting = true;
            const dialog = this.dialogService.open(WaitDialogComponent, {
                disableClose: true
            }, {
                joinGame: !this.game.userJoinedGame,
                joinRound: this.nextRound,
                game: this.game
            });
            dialog.onClose.subscribe(() => this.waiting = false);
        }
    }

    async joinGame(): Promise<void> {
        if (this.game.userJoinedGame)
            return;

        if (!this.subscriberService.isLoggedIn()) {
            this.forceLogin('You need to be logged in to join a game!');
            return;
        }

        if (this.subscriberService.subscriber.wallet < this.game.rounds[0].costPerPlayer) {
            // TODO: Streamline this
            this.dialogService.open(PaymentComponent, {disableClose: true}, 'NotEnoughMoney');
            return;
        }

        this.dialogService.showLoadingIndicator('Joining Game...');
        const resp = await this.gameplayService.joinGame(this.game.id);
        this.dialogService.closeLoadingIndicator();

        if (!resp.success) {
            // TODO: handle errors
            this.dialogService.open({
                body: STRINGS.errorJoiningGame
            });
        }
    }

    async beginBracketPlay(): Promise<void> {
        this.dialogService.showLoadingIndicator('Joining Bracket Play...');
        await this.gameplayService.beginBracketPlay(this.game.id);
        this.dialogService.closeLoadingIndicator();
    }

    goToHome(): void {
        let previousUrl = '/home/all';
        if (this.navigationService.lastNavigation) {
            previousUrl = this.navigationService.lastNavigation[0].url;
        }
        this.router.navigate([previousUrl])
    }

    async onRefresh(event: Subject<void>): Promise<void> {
        this.game.lastSyncMessageDate = undefined;
        this.loading = true;
        await this.loadGame(this.game.id);
        event.next();
        this.loading = false;
    }

    viewResults(): void {
        this.dialogService.open(GameResultsComponent, null, this.game);
    }

    private async loadGame(id: string): Promise<void> {
        let game: Game;
        try {
            game = await this.gameService.getGameById(id, {forceRefresh: true, skipSyncMessages: !this.subscriberService.isLoggedIn()});
        } catch (e) { // The game was not found
            this.router.navigate(['/home']);
            return;
        }
        this.game = game;

        game.state.takeUntil(this.ngUnsubscribe).subscribe(status => {
            this.nextRound = Util.getNextRound(this.game);
            this.lastRound = Util.getLastRound(this.game);

            switch (status) {
                case 'WAITING_FOR_MATCH':
                    if (!this.waiting) {
                        this.waiting = true;
                        const dialog = this.dialogService.open(WaitDialogComponent, null, {game: this.game});
                        dialog.onClose.subscribe(() => this.waiting = false);
                    }
                    break;
                case 'WAITING_FOR_QUESTION':
                case 'HAS_QUESTION':
                    this.dialogService.closeLoadingIndicator();
                    this.showRoundComponent(this.nextRound);
                    break;
            }
        });

        // this.showRoundComponent(new Round());

        this.checkForGameResults();

        this.loading = false;
        if (this.playOnLoad) {
            this.playNow();
            this.playOnLoad = false;
        }
    }

    private showRoundComponent(round?: Round): void {
        if (!round || this.roundDialog || this.roundComponentOpen)
            return;
        this.roundComponentOpen = true;
        this.logService.info('Showing round component with round:', round, 'game:', this.game);

        this.roundDialog = this.dialogService.open(RoundComponent, null, {
            game: this.game,
            round
        });
        this.roundDialog.onClose.subscribe(() => {
            this.roundDialog = undefined;
            this.roundComponentOpen = false;
            this.nextRound = Util.getNextRound(this.game);
            this.changeDetector.detectChanges();
        });
    }

    private async playNow(): Promise<void> {
        if (!Util.canPlayNow(this.game))
            return;

        if (this.game.gameStatus === 'OPEN')
            this.beginPoolPlay();
        else if (this.game.gameStatus === 'INPLAY')
            this.beginBracketPlay();
    }

    private checkForGameResults(): void {
        this.game.syncMessages.takeUntil(this.ngUnsubscribe).subscribe((message) => {
            if (message instanceof GameResultSyncMessage) {
                this.viewResults();
            }
        })
    }

    private forceLogin(message: string): void {
        this.dialogService.open({
            body: message,
            buttons: [{text: 'Login', value: 'login'}, {text: 'Signup', value: 'signup'}, {text: 'Cancel'}]
        }).onClose.subscribe(val => {
            if (val) {
                this.subscriberService.redirectUrl = this.router.url;
                this.router.navigate(['/login'], {fragment: val})
            }
        })
    }
}

interface Level {
    min: number;
    max: number;
}
