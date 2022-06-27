import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import { PlayerInfo } from '../../model/player-info';
import { GameService } from '../game.service';

@Component({
    selector: 'app-player-info-dialog',
    templateUrl: './player-info-dialog.component.html',
    styleUrls: ['./player-info-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayerInfoDialogComponent extends VxDialogDef<string> {
    info: PlayerInfo[] = [];
    loading = true;

    constructor(private cdr: ChangeDetectorRef, private gameService: GameService, public dialog: VxDialogRef<PlayerInfoDialogComponent>) {
        super();

        this.gameService.getPlayerInfo(dialog.data).subscribe(info => {
            this.loading = false;
            this.info = info;
            this.cdr.markForCheck();
        })
    }

    parsePaidPlayer(bool: boolean): string {
        return bool ? 'false' : 'true';
    }
}
