import { Injectable } from '@angular/core';
import { HttpService, ShoutResponse } from 'app/shared/services/http.service';
import { Observable } from 'rxjs';
import { Question } from 'app/model/question';
import {map} from 'rxjs/operators';

@Injectable()
export class QuestionService {
    private questions: Question[] = [];

    constructor(private httpService: HttpService) {

    }

    findQuestions(state: string): Observable<Question[]> {
        return this.httpService.sendRequest<FindQuestionsResponse>('/snowladmin/question/getByState', {state}).pipe(
            map((response: FindQuestionsResponse) => {
                if (response.questions) {
                    this.questions = response.questions.map(q => new Question(q));
                    return this.questions;
                } else {
                    return [];
                }
            })
        );
    }

    createQuestion(question: Question): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/question/create', {question});
    }

    updateQuestion(question: Question): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/question/update', {question});
    }

    changeQuestionState(question: Question, state: string): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/question/changeState', {questionId: question.id, state})
    }

    deleteQuestion(question: Question):  Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/question/delete', {questionId: question.id})
    }

    getQuestionById(questionId: string): Question | undefined {
        for (const question of this.questions) {
            if (question.id === questionId)
                return question;
        }
        return undefined;
    }
}

interface FindQuestionsResponse extends ShoutResponse {
    questions: Question[];
}
