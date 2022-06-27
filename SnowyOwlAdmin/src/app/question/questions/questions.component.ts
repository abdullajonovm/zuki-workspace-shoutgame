import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { QuestionService } from 'app/question/question.service';
import { Question } from 'app/model/question';
import { Router } from '@angular/router';
import { TitleService } from 'app/shared/services/title.service';
import { VxDialog } from 'vx-components';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {CategoryService} from '../../shared/services/category.service';

@Component({
    selector: 'app-find-question',
    templateUrl: './questions.component.html',
    styleUrls: ['./questions.component.scss']
})
export class FindQuestionComponent implements OnInit {
    get state(): string {
        return this._state;
    }

    set state(value: string) {
        this._state = value;
        this.search();
    }

    private _state = 'UNPUBLISHED';
    questions: Question[] = [];

    selectedQuestion!: Question;

    loading = false;

    boundCategoriesToString: Function;

    constructor(private questionService: QuestionService, private router: Router, private titleService: TitleService,
                private vxDialog: VxDialog, public categoryService: CategoryService) {
        this.boundCategoriesToString = this.categoriesToString.bind(this);
    }

    ngOnInit(): void {
        this.titleService.title = 'Questions';
        this._state = 'UNPUBLISHED';
        setTimeout(() => this.search(), 100);
    }

    search(): void {
        this.loading = true;
        this.questionService.findQuestions(this._state).subscribe(arr => {
            this.questions = arr;
            this.loading = false;
        });
    }

    publishQuestion(): void {
        this.questionService.changeQuestionState(this.selectedQuestion, 'PUBLISHED').subscribe(result => {
            if (result.success)
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully published question!',
                    buttons: ['Ok']
                });
            else
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error publishing: ',
                    body: JSON.stringify(result),
                    buttons: ['Ok']
                });
            this.search();
        });
    }

    retireQuestion(): void {
        this.questionService.changeQuestionState(this.selectedQuestion, 'RETIRED').subscribe(result => {
            if (result.success)
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully retired question!',
                    buttons: ['Ok']
                });
            else
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error retiring question: ',
                    body: JSON.stringify(result),
                    buttons: ['Ok']
                });
            this.search();
        });
    }

    republishQuestion(): void {
        this.questionService.changeQuestionState(this.selectedQuestion, 'PUBLISHED').subscribe(result => {
            if (result.success)
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully republished question!',
                    buttons: ['Ok']
                });
            else
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error republishing question: ',
                    body: JSON.stringify(result),
                    buttons: ['Ok']
                });
            this.search();
        });
    }

    deleteQuestion(): void {
        this.questionService.deleteQuestion(this.selectedQuestion).subscribe(result => {
            if (result.success)
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Success!',
                    body: 'Successfully deleted question!',
                    buttons: ['Ok']
                });
            else
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error deleting question: ',
                    body: JSON.stringify(result),
                    buttons: ['Ok']
                });

            this.search();
        });
    }

    editQuestion(): void {
        this.router.navigate(['/question/edit', this.selectedQuestion.id]);
    }

    categoriesToString(categoryIds: string[]): string {
        return categoryIds.map(id => {
            const category = this.categoryService.keyedCategories[id];

            return category ? category.categoryName.en : 'UNKNOWN';
        }).join(', ');
    }

    cloneQuestion(): void {
        this.router.navigate(['/question/clone', this.selectedQuestion.id]);
    }
}
