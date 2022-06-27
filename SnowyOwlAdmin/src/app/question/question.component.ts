import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Question } from 'app/model/question';
import { CategoryService } from 'app/shared/services/category.service';
import { DeviceService } from 'app/shared/services/device.service';
import { Answer } from 'app/model/answer';
import * as uuid from 'uuid/v4';
import { NgForm } from '@angular/forms';
import { QuestionService } from 'app/question/question.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TitleService } from 'app/shared/services/title.service';
import { ComponentCanDeactivate } from 'app/PendingChangesGuard';
import { VxDialog } from 'vx-components';
import { dateMaskShort } from 'app/shared/date.directive';
import { AlertDialogComponent } from '../shared/alert-dialog/alert-dialog.component';
import {LoadingDialogComponent} from '../shared/loading-dialog.component';

const maxAnswers = 10;

@Component({
    selector: 'app-question',
    templateUrl: './question.component.html',
    styleUrls: ['./question.component.scss']
})
export class QuestionComponent implements OnInit, ComponentCanDeactivate {
    dateMask = dateMaskShort;
    get correctAnswer(): Answer {
        return this._correctAnswer;
    }

    set correctAnswer(value: Answer) {
        this.question.answers.forEach(answer => {
            answer.correct = answer === value;
        });
        this._correctAnswer = value;
    }

    get numAnswers(): number {
        return this._numAnswers;
    }

    set numAnswers(value: number) {
        if (value > maxAnswers) {
            value = maxAnswers;
        }

        const numAnswers = this.numAnswers || 0;
        const newAnswers: Answer[] = [];
        if (numAnswers > value) {
            const diff = numAnswers - value;
            this.question.answers.splice(numAnswers - diff, diff);
        } else if (numAnswers < value) {
            for (let i = 0; i < value - numAnswers; i++) {
                newAnswers.push(this.createAnswer());
            }
            // Done on timeout to keep from clearing answer text.
            setTimeout(() => this.question.answers = [...this.question.answers, ...newAnswers]);
        }

        this._numAnswers = value;
    }

    question!: Question;
    minDate!: Date;
    editingQuestion = false;
    cloningQuestion = false;
    editable = false;

    @ViewChild('form', { static: false })
    form!: NgForm;

    private _correctAnswer!: Answer;
    private _numAnswers!: number;
    private submitted = false

    constructor(public categoryService: CategoryService, public deviceService: DeviceService,
                private questionService: QuestionService, private router: Router,
                private titleService: TitleService, private route: ActivatedRoute,
                public vxDialog: VxDialog) {
    }

    ngOnInit(): void {
        this.route.params.subscribe((params) => {
            if (params.id) {
                this.editingQuestion = true;
                this.loadQuestion(params.id);
                this.titleService.title = 'Edit Question';
            } else if (params.cloneId) {
                this.editingQuestion = false;
                this.editable = true;
                this.cloningQuestion = true;
                this.loadQuestion(params.cloneId);
                this.titleService.title = 'Clone Question';
            } else {
                this.editingQuestion = false;
                this.editable = true;
                this.titleService.title = 'Create Question';
            }
        });

        if (!this.editingQuestion && !this.cloningQuestion) {
            const question = new Question();
            question.createDate = new Date();
            question.difficulty = 5;
            question.id = uuid();
            question.languageCodes = ['en'];
            this.question = question;
            this.numAnswers = 3;
        }

        this.minDate = new Date();
        this.submitted = false;
    }

    setAllCategories(selected: boolean): void {
        const categories = this.question.questionCategoryUuids;
        if (selected && (categories.length !== 1 || categories[0] !== '*')) {
            setTimeout(() => {
                this.question.questionCategoryUuids = ['*'];
            })
        }
    }

    submit(): void {
        const form = this.form;
        if (!form.valid)
            return;

        if (this.editingQuestion) {
            this.submitted = true;
            const dialog = this.vxDialog.open(LoadingDialogComponent,  'Updating Question...');
            this.questionService.updateQuestion(this.question).subscribe(resp => {
                if (resp.success) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Success!',
                        body: 'Question successfully updated!',
                        buttons: ['Ok']
                    });
                } else {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error updating question:',
                        body: JSON.stringify(resp),
                        buttons: ['Ok']
                    });
                }

                this.router.navigate(['/question']);
                dialog.close();
            });
        } else {
            this.submitted = true;
            const dialog = this.vxDialog.open(LoadingDialogComponent,  this.cloningQuestion ? 'Cloning Question...' : 'Creating Question...');
            this.questionService.createQuestion(this.question).subscribe(resp => {
                if (resp.success) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Success!',
                        body: this.cloningQuestion ? 'Question successfully cloned!' : 'Question successfully created!',
                        buttons: ['Ok']
                    });
                } else {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: this.cloningQuestion ? 'Error cloning question:' : 'Error creating question:',
                        body: JSON.stringify(resp),
                        buttons: ['Ok']
                    });
                }

                dialog.close();
                this.router.navigate(['/question']);
            });
        }
    }

    // @HostListener allows us to also guard against browser refresh, close, etc.
    @HostListener('window:beforeunload')
    canDeactivate(): boolean {
        if (!this.submitted && this.form && this.form.dirty) {
            return confirm('WARNING: You have unsaved changes. Press Cancel to go back ' +
                'and save these changes, or OK to lose these changes.');
        }
        return true;
    }

    private createAnswer(): Answer {
        const answer = new Answer();
        answer.id = uuid();
        answer.createDate = new Date();
        answer.questionId = this.question.id;
        return answer;
    }

    private loadQuestion(questionId: string): void {
        const q = this.questionService.getQuestionById(questionId);
        if (q) {
            this._numAnswers = q.answers.length;

            for (const answer of q.answers) {
                if (answer.correct) {
                    this._correctAnswer = answer;
                    break;
                }
            }

            if (this.editingQuestion) {
                this.editable = q.status === 'UNPUBLISHED';
            } else if (this.cloningQuestion) {
                q.id = uuid();
            }

            this.question = q;
        } else {
            this.router.navigate(['/question']);
        }

    }
}
