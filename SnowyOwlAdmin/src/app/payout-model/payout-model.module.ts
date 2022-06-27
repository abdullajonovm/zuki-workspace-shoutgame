import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayoutModelComponent } from './payout-model/payout-model.component';
import { PayoutModelsComponent } from './payout-models/payout-models.component';
import {SharedModule} from '../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
const routes: Routes = [
    {path: '', component: PayoutModelsComponent},
    {path: 'edit/:id', component: PayoutModelComponent},
    {path: 'clone/:cloneId', component: PayoutModelComponent},
    {path: 'create', component: PayoutModelComponent}
];
@NgModule({
  imports: [
    CommonModule,
      SharedModule,
      RouterModule.forChild(routes)
  ],
  declarations: [PayoutModelComponent, PayoutModelsComponent]
})
export class PayoutModelModule { }
