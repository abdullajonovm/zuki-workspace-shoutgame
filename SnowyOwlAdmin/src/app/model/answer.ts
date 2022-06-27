import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs } from 'cerialize';

export class Answer extends BaseModel<Answer> {
    @autoserialize id!: string;
    @autoserialize questionId!: string;
    @autoserializeAs(Object) answerText!: any;
    @autoserialize mediaUrl!: string;
    @autoserialize mediaType!: string;
    @autoserialize correct!: boolean;
    @autoserialize surveyPercent!: number;
    @autoserializeAs(Date) createDate!: Date;

    protected initialize(): void {
        this.answerText = {};
    }
}
