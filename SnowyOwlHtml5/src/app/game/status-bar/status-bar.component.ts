import {Component, ElementRef, HostListener, Input, OnDestroy} from '@angular/core';
import {Game, PayoutLevel} from 'app/model/game';
import {GameService} from 'app/shared/services/game.service';

const PAYOUT_START_PERCENT = 75;

@Component({
    selector: 'sh-status-bar',
    templateUrl: './status-bar.component.html',
    styleUrls: ['./status-bar.component.scss']
})
export class StatusBarComponent implements OnDestroy {
    currentPlayers = 0;
    maxPlayers = 0;
    numPayouts = 0;
    payoutAmount = 0;
    payoutPositionPct = PAYOUT_START_PERCENT;
    payoutLevels: PayoutLevel[] = [];
    currentPositionPct = 100;
    private top = 0;
    private height = 0;
    private mouseDown = false;
    private interval: any;
    private totalPayouts: number;

    constructor(private elementRef: ElementRef, private gameService: GameService) {
    }

    private _game: Game;

    @Input()
    get game(): Game {
        return this._game;
    }

    set game(value: Game) {
        this._game = value;
        this.init();
    }

    ngOnDestroy(): void {
        clearInterval(this.interval);
    }

    @HostListener('touchstart', ['$event'])
    @HostListener('mousedown', ['$event'])
    handleTouchStart(event: MouseEvent | TouchEvent): void {
        this.calcContainerPosition();
        let y = (event instanceof MouseEvent) ? event.pageY : event.touches[0].pageY;
        y -= this.top;
        this.placeLine(y);
        this.mouseDown = true;
    }

    @HostListener('document:touchend', ['$event'])
    @HostListener('document:mouseup', ['$event'])
    handleTouchEnd(event: any): void {
        this.mouseDown = false;
        this.refresh();
    }

    @HostListener('touchmove', ['$event'])
    @HostListener('mousemove', ['$event'])
    handleTouchMove(event: MouseEvent | TouchEvent): void {
        if (this.mouseDown) {
            let y = (event instanceof MouseEvent) ? event.pageY : event.touches[0].pageY;
            y -= this.top;
            this.placeLine(y);
            event.preventDefault();
            event.stopPropagation();
        }
    }

    placeLine(y: number): void {
        if (y < 0)
            y = 0;
        if (y > this.height)
            y = this.height;
        const linePos = getPercent(y, this.height);
        if (linePos < PAYOUT_START_PERCENT) {
            const max = this.height * PAYOUT_START_PERCENT / 100;
            const level = getPercent(y, max) * this.payoutLevels.length / 100;
            const payoutLevel = this.payoutLevels[Math.floor(level)];
            if (payoutLevel) {
                this.numPayouts = payoutLevel.key;
                this.payoutAmount = payoutLevel.val;
            }
        } else {
            this.numPayouts = this.payoutLevels[this.payoutLevels.length - 1].key;
            this.payoutAmount = this.payoutLevels[this.payoutLevels.length - 1].val;
        }
        this.payoutPositionPct = linePos;
    }

    private calcContainerPosition(): void {
        let top = 0;
        let a = this.elementRef.nativeElement;
        while (a) {
            top += a.offsetTop;
            a = a.offsetParent;
        }
        this.top = top;
        this.height = this.elementRef.nativeElement.offsetHeight;
    }

    private refresh(): void {
        this.maxPlayers = this._game.rounds[0].maximumPlayerCount;

        this.payoutPositionPct = PAYOUT_START_PERCENT;
        this.numPayouts = this.payoutLevels[this.payoutLevels.length - 1].key;
        this.totalPayouts = this.payoutLevels[this.payoutLevels.length - 1].key;
        this.payoutAmount = this.payoutLevels[this.payoutLevels.length - 1].val;
    }

    private async checkForCurrentPlayers(): Promise<void> {
        try {
            const outstandingMatches = await this.gameService.getOutstandingMatches(this.game.id);
            this.currentPlayers = outstandingMatches.numOutstandingMatches * 2;

        } catch (e) {
            this.currentPlayers = this._game.rounds[this._game.rounds.length - 1].currentPlayerCount;
        }
        if (this.currentPlayers < this.totalPayouts) {
            const pct = getPercent(this.currentPlayers, this.totalPayouts);
            this.currentPositionPct = pct * PAYOUT_START_PERCENT / 100;
        } else {
            const diff = this.currentPlayers - this.totalPayouts;
            const pct = getPercent(diff, this.maxPlayers - this.totalPayouts);
            this.currentPositionPct = PAYOUT_START_PERCENT + ((100 - PAYOUT_START_PERCENT) * pct / 100);
        }

    }

    private init(): void {
        this.payoutLevels = this._game.payoutLevels;
        this.calcContainerPosition();
        this.refresh();
        this.checkForCurrentPlayers();
        if (this.game.gamePlayer) {

        } else {
            this.interval = setInterval(() => this.checkForCurrentPlayers(), 500);
        }
    }
}

function getPercent(small: number, big: number): number {
    return small / big * 100;
}
