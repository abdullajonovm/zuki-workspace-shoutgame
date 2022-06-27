import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Game} from '../../model/game';

@Component({
    selector: 'sh-game-card',
    templateUrl: './game-card.component.html',
    styleUrls: ['./game-card.component.scss']
})
export class GameCardComponent {

    @Input() game: Game;
    @Input() playNow: string | null;
    @Input() moreInfo = true;

    @Output() onPlayNow = new EventEmitter();
    @Output() onMoreInfo = new EventEmitter();

    constructor() {
    }

}
