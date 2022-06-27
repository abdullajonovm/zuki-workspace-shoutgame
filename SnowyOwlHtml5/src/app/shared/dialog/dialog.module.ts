import {NgModule} from '@angular/core';
import {DialogComponent} from 'app/shared/dialog/dialog.component';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {CommonModule} from '@angular/common';

@NgModule({
    declarations: [DialogComponent],
    imports: [CommonModule],
    entryComponents: [DialogComponent],
    providers: [DialogService]
})
export class DialogModule {

}
