import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PayoutsComponent} from './payouts.component';
import {PayoutsService} from './payouts.service';
import {SharedModule} from '../shared/shared.module';

const routes: Routes = [
    {path: '', pathMatch: 'full', component: PayoutsComponent}
];

@NgModule({
    imports: [
        SharedModule,
        RouterModule.forChild(routes)
    ],
    declarations: [
        PayoutsComponent
    ],
    providers: [
        PayoutsService
    ]
})
export class PayoutsModule {

}
