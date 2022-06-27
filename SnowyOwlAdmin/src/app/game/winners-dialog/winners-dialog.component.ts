import {Component} from '@angular/core';
import {Game} from 'app/model/game';
import {GameService} from 'app/game/game.service';
import {Winner} from 'app/model/winner';
import { VxDialogDef, VxDialogRef } from 'vx-components';

@Component({
    selector: 'app-winners-dialog',
    templateUrl: './winners-dialog.component.html',
    styleUrls: ['./winners-dialog.component.scss']
})
export class WinnersDialogComponent extends VxDialogDef<Game> {

    loading = true;
    winners: Winner[] = [];

    private game: Game;

    constructor(private gameService: GameService, public dialog: VxDialogRef<WinnersDialogComponent>) {
        super();
        this.game = dialog.data;
        this.loadWinners();
    }

    async loadWinners(): Promise<void> {
        this.loading = true;
        this.winners = await this.gameService.getWinners(this.game);
        this.loading = false;
    }

    download(): void {
        let csvContent = 'data:text/csv;charset=utf-8,';
        const fields: (keyof Winner)[] = ['subscriberId', 'email', 'nickname', 'amount', 'gameId', 'rank'];
        csvContent += fields.join() + '\n';

        this.winners.forEach((winner, index) => {
            const dataStr = fields.map(field => winner[field]).join();
            csvContent += index < this.winners.length ? dataStr + '\n' : dataStr;
        });

        const encodedUri = encodeURI(csvContent);
        const link = document.createElement('a');
        link.setAttribute('href', encodedUri);
        link.setAttribute('download', 'winners.csv');
        document.body.appendChild(link); // Required for FF

        link.click();
    }
}
