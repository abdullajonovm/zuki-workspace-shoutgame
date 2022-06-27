import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';
import {LocalizedText} from 'app/model/localized-text';

export class Answer extends BaseModel<Answer> {
    @autoserialize id: string;
    @autoserialize questionId: string;
    @autoserializeAs(Object) answerText: LocalizedText;
    @autoserialize mediaUrl: string;
    @autoserialize mediaType: string;
    @autoserialize correct: boolean;
    @autoserialize surveyPercent: number;
    @autoserializeAs(Date) createDate: Date;
}
