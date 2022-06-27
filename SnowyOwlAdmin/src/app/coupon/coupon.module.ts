import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { Route, RouterModule } from '@angular/router';
import { BatchComponent } from './batch.component';
import {CouponService} from './coupon.service';
import {BatchesComponent} from './batches/batches.component';
import { CreateBatchComponent } from './create-batch/create-batch.component';
import { AssignCodesComponent } from './assign-codes/assign-codes.component';

const routes: Route[] = [
    { path: '', pathMatch: 'full', component: BatchesComponent},
    { path: 'batch/:id', component: BatchComponent}
];

@NgModule({
    declarations: [BatchComponent, BatchesComponent, CreateBatchComponent, AssignCodesComponent],
    imports: [SharedModule, RouterModule.forChild(routes)],
    entryComponents: [CreateBatchComponent, AssignCodesComponent],
    providers: [CouponService]
})
export class CouponModule {

}
