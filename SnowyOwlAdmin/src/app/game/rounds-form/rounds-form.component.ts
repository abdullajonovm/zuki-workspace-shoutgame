import {
    AfterViewChecked,
    AfterViewInit, ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnChanges,
    SimpleChange,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import {ControlContainer, NgForm, NgModelGroup} from '@angular/forms';
import {Round} from '../../model/round';
import * as uuid from 'uuid/v4';
import {Category} from '../../model/category';

@Component({
    selector: 'app-rounds-form',
    templateUrl: './rounds-form.component.html',
    styleUrls: ['./rounds-form.component.scss'],
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoundsFormComponent {

    @Input() editable = true;
    @Input() categories: Category[] = [];

    selectedCategories: string[] = [];
    selectedCategoriesPOOL: string[] = [];
    // minimumActivity: number;
    // maximumActivity: number;
    maximumDuration = 10;
    playerMaxDuration!: number;
    numPoolRounds = 2;
    secondsBetween = 5;

    get allCategories(): boolean {
        return this.selectedCategories.length === 1 && this.selectedCategories[0] === '*';
    }

    get allCategoriesPool(): boolean {
        return this.selectedCategoriesPOOL.length === 1 && this.selectedCategoriesPOOL[0] === '*';
    }

    constructor(private cdr: ChangeDetectorRef) {
    }

    setAllCategories(type: 'pool' | 'bracket'): void {
        const arr = type === 'pool' ? this.selectedCategoriesPOOL : this.selectedCategories;

        if (arr.length !== 1 || arr[0] !== '*') {
            setTimeout(() => {
                if (type === 'pool') {
                    this.selectedCategoriesPOOL = ['*'];
                } else {
                    this.selectedCategories = ['*'];
                }
                this.cdr.markForCheck();
            });
        }
    }

    initFromExisting(rounds: Round[]): void {
        if (!rounds.length)
            return;

        this.numPoolRounds = rounds.length - 1;
        // this.minimumActivity = rounds[0].minimumActivityToWinCount;
        // this.maximumActivity = rounds[0].maximumActivityCount;
        this.maximumDuration = rounds[0].activityMaximumDurationSeconds;
        this.playerMaxDuration = rounds[0].playerMaximumDurationSeconds;
        this.secondsBetween = rounds[0].durationBetweenActivitiesSeconds;
        for (const round of rounds) {
            if (round.isBracketRound) {
                this.selectedCategories = round.categories;
            } else {
                this.selectedCategoriesPOOL = round.categories;
            }
        }
        this.cdr.markForCheck();
    }

    getRounds(maxPlayerCount: number, languageCodes: string[], gameId: string): Round[] {
        const result: Round[] = [];
        for (let i = 0; i <= this.numPoolRounds; i++) {
            const isBracket = i === this.numPoolRounds;
            const round = new Round();
            round.id = uuid();
            round.roundStatus = 'PENDING';
            round.finalRound = isBracket;
            round.roundType = isBracket ? 'BRACKET' : 'POOL';
            round.roundSequence = i + 1;
            round.gameId = gameId;
            round.matchGlobal = false;
            // round.minimumActivityToWinCount = this.minimumActivity;
            // round.maximumActivityCount = this.maximumActivity;
            round.activityMaximumDurationSeconds = this.maximumDuration;
            round.playerMaximumDurationSeconds = this.playerMaxDuration;
            round.durationBetweenActivitiesSeconds = this.secondsBetween;
            round.maximumPlayerCount = maxPlayerCount;
            round.categories = isBracket ? this.selectedCategories : this.selectedCategoriesPOOL;
            round.matchPlayerCount = 2;
            round.roundActivityType = 'Trivia';
            round.roundActivityValue = 'n/a';
            round.minimumMatchCount = 1;
            // TODO: get rid of round names?
            languageCodes.forEach(code => {
                round.roundNames[code] = 'Round ' + round.roundSequence;
            });
            result.push(round);
        }
        return result;
    }

}
