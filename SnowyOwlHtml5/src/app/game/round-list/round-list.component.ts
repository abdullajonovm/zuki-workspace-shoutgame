import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Round} from 'app/model/round';

@Component({
    selector: 'sh-round-list',
    templateUrl: './round-list.component.html',
    styleUrls: ['./round-list.component.scss']
})
export class RoundListComponent {

    @Input() rounds: Round[];
    @Input() nextRound: Round;
    @Input() hideBracketPlay = false;
    @Input() hidePoolPlay = false;

    @Output() onPlay = new EventEmitter<Round>();

}
