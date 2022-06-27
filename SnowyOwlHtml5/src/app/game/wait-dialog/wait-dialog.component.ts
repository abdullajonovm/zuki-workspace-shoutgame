import {Component, OnInit} from '@angular/core';
import {DialogComponent, OnDialogOpen} from 'app/shared/dialog/dialog.component';
import {GameplayService} from 'app/shared/services/gameplay.service';
import {Game} from 'app/model/game';
import {Round} from 'app/model/round';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {PaymentComponent} from 'app/shared/payment/payment.component';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {NumAnim, Util} from 'app/util';

@Component({
    selector: 'sh-wait-dialog',
    templateUrl: './wait-dialog.component.html',
    styleUrls: ['./wait-dialog.component.scss']
})
export class WaitDialogComponent implements OnInit, OnDialogOpen<OpenParams> {

    params: OpenParams;
    joinedGame = false;
    joinedRound = false;
    countdown?: NumAnim;

    constructor(private gameplayService: GameplayService, private subscriberService: SubscriberService,
                private dialogService: DialogService) {
    }

    ngOnInit(): void {
    }

    async onDialogOpen(dialog: DialogComponent, data: OpenParams): Promise<void> {
        dialog.dialogOptions.width = '300px';
        dialog.dialogOptions.disableClose = true;
        this.params = data;

        const waitTime = await this.gameplayService.getBracketCountdownTime(data.game.id);
        if (waitTime > 0) {
            this.countdown = Util.animateNumber(waitTime / 1000, 0, waitTime);
        }

        if (data.joinGame && !data.game.userJoinedGame) {
            if (this.subscriberService.subscriber.wallet < data.game.rounds[0].costPerPlayer) {
                // TODO: Streamline this
                this.dialogService.open(PaymentComponent, {disableClose: true}, 'NotEnoughMoney');
                dialog.close();
            }

            await this.gameplayService.joinGame(data.game.id); // TODO: How to handle errors?
            this.joinedGame = true;
        } else {
            this.joinedGame = true;
        }

        if (data.joinRound) {
            await this.gameplayService.beginPoolPlay(data.game.id);
            this.joinedRound = true;
        } else {
            this.joinedRound = true;
        }

        data.game.state.subscribe(state => {
            switch (state) {
                case 'WAITING_FOR_MATCH':
                    break;
                default:
                    dialog.close();
                    break;
            }
        })
    }
}

interface OpenParams {
    game: Game;
    joinGame?: boolean;
    joinRound?: Round;
}
