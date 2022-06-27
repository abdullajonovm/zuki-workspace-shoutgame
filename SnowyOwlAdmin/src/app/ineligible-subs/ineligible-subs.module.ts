import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {IneligibleSubsComponent} from './ineligible-subs.component';
import {RouterModule, Routes} from '@angular/router';
import { AddIneligibleSubComponent } from './add-ineligible-sub/add-ineligible-sub.component';
import {SharedModule} from '../shared/shared.module';

const routes: Routes = [
    {path: '', component: IneligibleSubsComponent}
];

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        RouterModule.forChild(routes)
    ],
    declarations: [IneligibleSubsComponent, AddIneligibleSubComponent],
    entryComponents: [AddIneligibleSubComponent]
})
export class IneligibleSubsModule {
}
