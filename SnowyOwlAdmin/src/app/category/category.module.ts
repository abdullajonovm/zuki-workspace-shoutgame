import { NgModule } from '@angular/core';
import { CategoryComponent } from 'app/category/category.component';
import { SharedModule } from 'app/shared/shared.module';
import { Route, RouterModule } from '@angular/router';
import { FindCategoryComponent } from './find-category/categories.component';

const routes: Route[] = [
    { path: '**', component: FindCategoryComponent}
];

@NgModule({
    declarations: [CategoryComponent, FindCategoryComponent],
    entryComponents: [CategoryComponent],
    imports: [SharedModule, RouterModule.forChild(routes)]
})
export class CategoryModule {

}
