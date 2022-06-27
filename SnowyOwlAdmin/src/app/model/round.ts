import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs } from 'cerialize';

export class Round extends BaseModel<Round> {
    @autoserialize id!: string;
    @autoserialize gameId!: string;
    @autoserializeAs(Object) roundNames!: any;
    @autoserialize roundSequence!: number;
    @autoserialize roundType!: 'POOL' | 'BRACKET';
    @autoserialize roundStatus!: string;
    @autoserialize finalRound!: boolean;
    @autoserialize matchGlobal!: boolean;
    @autoserialize durationBetweenActivitiesSeconds!: number;
    @autoserialize matchPlayerCount!: number;
    @autoserialize maximumPlayerCount!: number;
    @autoserialize minimumMatchCount!: number;
    @autoserialize roundPurse!: number;
    // @autoserialize minimumActivityToWinCount!: number;
    // @autoserialize maximumActivityCount!: number;
    @autoserialize activityMaximumDurationSeconds!: number;
    @autoserialize playerMaximumDurationSeconds!: number;
    @autoserialize roundActivityType!: string;
    @autoserialize roundActivityValue!: string;
    @autoserializeAs(String) categories!: string[];
    @autoserializeAs(Date) expectedOpenDate!: Date;

    set allCategories(allCategories: boolean) {
        this.categories = allCategories ? ['*'] : [];
    }

    get allCategories(): boolean {
        return this.categories.length === 1 && this.categories[0] === '*';
    }

    get displayName(): string {
        return this.isBracketRound ? 'Tournament Round' : `Round ${this.roundSequence}`;
    }

    get isBracketRound(): boolean {
        return this.roundType === 'BRACKET';
    }

    toggleCategory(toggle: boolean, category: string): void {
        const idx = this.categories.indexOf(category);
        if (toggle) {
            this.categories.push(category);
        } else if (idx) {
            this.categories.splice(idx, 1);
        }
    }

    protected initialize(): void {
        this.roundNames = {};
        this.categories = [];
    }
}
