import {NgModule} from '@angular/core';

import {AffiliatePlanComponent} from './affiliate-plan.component';
import {SharedModule} from '../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import { AffiliateTreeComponent } from './affiliate-tree/affiliate-tree.component';

const routes: Routes = [
    {path: '', pathMatch: 'full', component: AffiliatePlanComponent},
    {path: 'tree', component: AffiliateTreeComponent}
];

@NgModule({
    imports: [SharedModule, RouterModule.forChild(routes)],
    exports: [],
    declarations: [AffiliatePlanComponent, AffiliateTreeComponent, AffiliateTreeComponent],
    providers: []
})
export class AffiliatePlanModule {
}
