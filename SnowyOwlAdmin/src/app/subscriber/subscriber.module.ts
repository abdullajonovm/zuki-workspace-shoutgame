import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { Route, RouterModule } from '@angular/router';
import {SubscriberComponent} from './subscriber.component';
import {BonusCashComponent} from './bonus-cash/bonus-cash.component';
import {SubscriberRolesComponent} from 'app/subscriber/subscriber-roles/subscriber-roles.component';

const routes: Route[] = [
    { path: '', pathMatch: 'full', component: SubscriberComponent}
];

@NgModule({
    declarations: [SubscriberComponent, BonusCashComponent, SubscriberRolesComponent],
    imports: [SharedModule, RouterModule.forChild(routes)],
    entryComponents: [BonusCashComponent, SubscriberRolesComponent],
    providers: []
})
export class SubscriberModule {

}
