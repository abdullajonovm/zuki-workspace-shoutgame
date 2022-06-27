import {Component, DoCheck, ElementRef, EventEmitter, Input, NgZone, Output, ViewChild} from '@angular/core';
import {Round} from 'app/model/round';
import {Question} from 'app/model/question';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {Util} from 'app/util';

@Component({
    selector: 'sh-round-item',
    templateUrl: './round-item.component.html',
    styleUrls: ['./round-item.component.scss']
})
export class RoundItemComponent implements DoCheck {
    @Input() round: Round;
    @Input() nextRound: boolean;

    @Output()
    onPlay = new EventEmitter();

    expanded = false;
    expandedQuestions: Question[] = [];
    @ViewChild('body') body: ElementRef;

    constructor(public subscriberService: SubscriberService, private zone: NgZone) {
    }
    ngDoCheck(): void {
        this.zone.runOutsideAngular(() => {
            if (this.body) {
                const el: HTMLDivElement = this.body.nativeElement;
                const childHeight = (el.children[0] as HTMLDivElement).offsetHeight;
                el.style.maxHeight = `${childHeight}px`;
            }
        })
    }

    toggleQuestion(question: Question): void {
        if (Util.includes(this.expandedQuestions, question)) {
            Util.remove(this.expandedQuestions, question);
        } else {
            this.expandedQuestions.push(question);
        }
    }

    isExpanded(question: Question): boolean {
        return Util.includes(this.expandedQuestions, question)
    }

    didWinRound(): boolean {
        return !!this.round.result && this.round.result.determination === 'WON';
    }

    didOpponentWin(): boolean {
        return !!this.round.result && this.round.result.determination === 'LOST';
    }

    headerClick(): void {
        if (!this.nextRound)
            this.expanded = !this.expanded;
    }

}
