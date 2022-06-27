import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs, deserialize } from 'cerialize';
import { Answer } from 'app/model/answer';

export class Question extends BaseModel<Question> {
    @autoserialize id!: string;
    @autoserialize difficulty!: number;
    @autoserialize source?: string;
    @autoserializeAs(String) languageCodes!: string[];
    @autoserializeAs(String) forbiddenCountryCodes!: string[];
    @autoserializeAs(Object) questionText!: any;
    @autoserialize mediaUrl!: string;
    @autoserialize mediaType!: string;
    @autoserializeAs(String) questionCategoryUuids!: string[];
    @autoserializeAs(Date) createDate!: Date;
    @autoserializeAs(Date) expirationDate!: Date;
    @deserialize usageCount!: string;
    @autoserializeAs(Answer) answers!: Answer[];
    @autoserialize status!: string;

    protected initialize(): void {
        this.answers = [];
        this.questionCategoryUuids = [];
        this.forbiddenCountryCodes = [];
        this.languageCodes = [];
        this.questionText = {};
    }

    get allCategories(): boolean {
        return this.questionCategoryUuids.length === 1 && this.questionCategoryUuids[0] === '*';
    }
}
