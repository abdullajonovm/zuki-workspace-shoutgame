import {Component} from '@angular/core';
import {Game} from 'app/model/game';
import { VxDialog, VxDialogDef, VxDialogRef } from 'vx-components';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {DeviceService} from '../../shared/services/device.service';
import {GameService} from '../game.service';
import {dateMask} from '../../shared/date.directive';
import {Router} from '@angular/router';

@Component({
    selector: 'app-clone-game-dialog',
    templateUrl: './clone-game-dialog.component.html',
    styleUrls: ['./clone-game-dialog.component.scss']
})
export class CloneGameDialogComponent extends VxDialogDef<Game, boolean> {
    dateMask = dateMask;
    startDate = new Date();
    poolDate = new Date();
    bracketDate = new Date();
    gameNames: any = {};
    game: Game;

    constructor(public deviceService: DeviceService, private gameService: GameService,
                private vxDialog: VxDialog, private router: Router, public dialog: VxDialogRef<CloneGameDialogComponent>) {
        super()

        const game = dialog.data;
        this.game = game;

        if (game.gameNames) {
            for (const key in game.gameNames) {
                if (game.gameNames.hasOwnProperty(key))
                    this.gameNames[key] = game.gameNames[key];
            }
        }
    }

    submit(): void {
        this.gameService.cloneGame(this.game, this.startDate, this.poolDate, this.bracketDate, this.gameNames).subscribe(resp => {
            if (resp.success) {
                this.dialog.close(true);
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully cloned game!',
                    buttons: ['Ok']
                });
            } else {
                this.dialog.close(false);
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error cloning game',
                    body: JSON.stringify(resp),
                    buttons: ['Ok']
                });
            }
        });
    }

    advanced(): void {
        this.dialog.close(false);
        this.router.navigate(['/game/clone/', this.game.id]);
    }

}
