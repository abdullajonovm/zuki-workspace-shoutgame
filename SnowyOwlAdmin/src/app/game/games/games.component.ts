import {Component, OnInit} from '@angular/core';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import { AddSponsorPlayersDialogComponent } from '../add-sponsor-players-dialog/add-sponsor-players-dialog.component';
import {GameService} from '../game.service';
import {Game} from 'app/model/game';
import {Router} from '@angular/router';
import {TitleService} from 'app/shared/services/title.service';
import {CloneGameDialogComponent} from 'app/game/clone-game-dialog/clone-game-dialog.component';
import {DeviceService} from 'app/shared/services/device.service';
import {Util} from 'app/util';
import { VxDialog, VxToast } from 'vx-components';
import {StartBracketPlayDialogComponent} from 'app/game/start-bracket-play-dialog.component';
import {WinnersDialogComponent} from 'app/game/winners-dialog/winners-dialog.component';
import {CopyToClipboardComponent} from '../copy-to-clipboard/copy-to-clipboard.component';
import { PlayerInfoDialogComponent } from '../player-info-dialog/player-info-dialog.component';


@Component({
    selector: 'app-find-game',
    templateUrl: './games.component.html',
    styleUrls: ['./games.component.scss']
})
export class FindGameComponent implements OnInit {
    gameTypes = ['Pending', 'Open', 'In Play', 'Closed', 'Cancelled'];
    selectedTypes: string[] = [];

    games: Game[] = [];
    selectedGame!: Game;

    loading = false;

    constructor(private gameService: GameService, private router: Router,
                private titleService: TitleService, public deviceService: DeviceService,
                public vxDialog: VxDialog, private toast: VxToast) {
}

    showDialog(): void {
        this.router.navigate(['/game/clone/', this.selectedGame.id]);
        // const dialog = this.vxDialog.open(CloneGameDialogComponent, {}, this.selectedGame);
        // dialog.onClose.subscribe(x => {
        //     if (x) {
        //         this.findGames();
        //     }
        // })
    }

    ngOnInit(): void {
        setTimeout(() => this.findGames(), 100);
        this.selectedTypes = ['Pending', 'In Play', 'Open'];
        this.titleService.title = 'Games';
    }

    toggleType(type: string, shouldInclude: boolean): void {
        const idx = this.selectedTypes.indexOf(type);
        if (!shouldInclude && idx !== -1) {
                this.selectedTypes.splice(idx, 1);
        } else if (shouldInclude && idx === -1) {
            this.selectedTypes.push(type);
        }
    }

    findGames(): void {
        this.loading = true;
        const gameTypes = this.selectedTypes.map(gt => gt.replace(' ', '').toUpperCase()).join(',');
        this.gameService.findGames(gameTypes).then((games) => {
            this.games = games;
            this.loading = false;
        })
    }

    viewGame(): void {
        this.router.navigate(['/game/view', this.selectedGame.id])
    }

    async notifyFreeplayers(): Promise<void> {
        const response = await this.gameService.notifyFreeplayers(this.selectedGame!.id);
        if (response.success) {
            this.toast.open({
                title: 'Success!',
                text: 'Successfully Notified Freeplayers'
            })
        } else {
            this.toast.open({
                title: 'Error!',
                text: 'Unexpected Error Notifying Freeplayers',
                type: 'error'
            })
        }

    }

    openGame(): void {
        this.gameService.openGame(this.selectedGame).subscribe(response => {
            if (response.success) {
                this.findGames();
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully opened game!',
                    buttons: ['Ok']
                });
            } else {
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error opening game',
                    body: JSON.stringify(response),
                    buttons: ['Ok']
                });
            }
        });
    }

    startPoolPlay(): void {
        this.gameService.startPoolPlay(this.selectedGame).subscribe(response => {
            if (response.success) {
                this.findGames();

                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully started pool play!',
                    buttons: ['Ok']
                });

            } else {
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error starting pool play',
                    body: JSON.stringify(response),
                    buttons: ['Ok']
                });
            }
        });
    }

    cancelGame(): void {
        const dialog = this.vxDialog.open(AlertDialogComponent, {
            title: 'Confirm',
            body: 'Are you sure you want to cancel this game?',
            buttons: ['Yes', 'No']
        });

        dialog.onClose.subscribe(val => {
            if (val) {
                this.gameService.cancelGame(this.selectedGame).subscribe(response => {
                    if (response.success) {
                        this.findGames();
                        this.vxDialog.open(AlertDialogComponent, {
                            title: 'Success!',
                            body: 'Successfully cancelled game!',
                            buttons: ['Ok']
                        });
                    } else {
                        this.vxDialog.open(AlertDialogComponent, {
                            title: 'Error cancelling game',
                            body: JSON.stringify(response),
                            buttons: ['Ok']
                        });
                    }
                });
            }
        })

    }

    startTournament(): void {
        const dialog = this.vxDialog.open(StartBracketPlayDialogComponent);
        dialog.onClose.subscribe(time => {
            if (time == null)
                return;

            this.gameService.startBracketPlay(this.selectedGame, time * 60000).subscribe(response => {
                if (response.success) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Success!',
                        body: `Tournament will begin in ${time} minute${time === 1 ? '' : 's'}.`,
                        buttons: ['Ok']
                    });
                } else {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error starting bracket play',
                        body: JSON.stringify(response),
                        buttons: ['Ok']
                    });
                }
            });
        });

    }

    viewWinners(): void {
        this.vxDialog.open(WinnersDialogComponent, this.selectedGame);
    }

    formatDate(date: Date): string {
        if (date instanceof Date)
            return Util.formatDate(date);

        return '';
    }

    showGameUrl(): void {
        this.vxDialog.open(CopyToClipboardComponent, this.selectedGame.id);
    }

    getPlayerInfo(): void {
        this.vxDialog.open(PlayerInfoDialogComponent, this.selectedGame.id);
    }

    addSponsorPlayers(): void {
        this.vxDialog.open(AddSponsorPlayersDialogComponent, this.selectedGame.id);
    }
}
