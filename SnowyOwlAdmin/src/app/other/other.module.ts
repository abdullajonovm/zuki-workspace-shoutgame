import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OtherComponent} from './other.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../shared/shared.module';
import {OtherService} from './other.service';

const routes: Routes = [
    {path: '', component: OtherComponent}
];

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        SharedModule
    ],
    declarations: [OtherComponent],
    providers: [
        OtherService
    ]
})
export class OtherModule {
}
