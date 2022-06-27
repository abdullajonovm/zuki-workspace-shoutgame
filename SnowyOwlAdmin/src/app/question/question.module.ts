import { NgModule } from '@angular/core';
import { QuestionComponent } from 'app/question/question.component';
import { Route, RouterModule } from '@angular/router';
import { FindQuestionComponent } from 'app/question/questions/questions.component';
import { PendingChangesGuard } from 'app/PendingChangesGuard';
import { QuestionService } from './question.service';
import { SharedModule } from 'app/shared/shared.module';

const routes: Route[] = [
    {path: '', pathMatch: 'full', component: FindQuestionComponent},
    {path: 'edit/:id', component: QuestionComponent, canDeactivate: [PendingChangesGuard]},
    {path: 'create', component: QuestionComponent, canDeactivate: [PendingChangesGuard]},
    {path: 'clone/:cloneId', component: QuestionComponent, canDeactivate: [PendingChangesGuard]}
];

@NgModule({
    declarations: [QuestionComponent, FindQuestionComponent],
    imports: [SharedModule, RouterModule.forChild(routes)],
    providers: [QuestionService]
})
export class QuestionModule {
}
