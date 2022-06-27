import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';
import {Answer} from 'app/model/answer';
import {SubscriberQuestionAnswer} from 'app/model/subscriber-question-answer';

export class Question extends BaseModel<Question> {
    @autoserialize id: string;
    @autoserialize difficulty: number; // 0 is easy 10 is hard
    @autoserialize source?: string; // where the question came from
    @autoserializeAs(String) languageCodes: string[];
    @autoserializeAs(String) forbiddenCountryCodes: string[];
    @autoserializeAs(Object) questionText: any;
    @autoserialize mediaUrl: string;
    @autoserialize mediaType: string;
    @autoserialize usageCount: number;
    @autoserializeAs(String) questionCategoryUuids: string[];
    @autoserializeAs(Date) createDate: Date;
    @autoserializeAs(Date) expirationDate: Date;
    @autoserializeAs(Answer) answers: Answer[];
    answer: SubscriberQuestionAnswer;
    opponentAnswer: SubscriberQuestionAnswer;

    answered = false;
    correctAnswerId: string;

    protected initialize(): void {
        this.answers = [];
        this.questionCategoryUuids = [];
        this.forbiddenCountryCodes = [];
        this.languageCodes = [];
        this.questionText = {};
    }
}
