import {ModuleWithProviders, NgModule} from '@angular/core';
import {DataTableModule} from 'app/data-table/data-table.module';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import { VX_BUTTON_DEFAULT_COLOR, VX_BUTTON_DEFAULT_VARIATION, VxComponentsModule } from 'vx-components';
import {DateDirective} from './date.directive';
import {TextMaskModule} from 'angular2-text-mask';
import { ExpansionPanelComponent } from './expansion-panel/expansion-panel.component';
import {LoadingDialogComponent} from './loading-dialog.component';
import {PendingChangesGuard} from '../PendingChangesGuard';
import { MaskDirective } from './mask.directive';
import {CategoryService} from './services/category.service';
import { SponsorService } from './services/sponsor.service';
import {TitleService} from './services/title.service';
import {DeviceService} from './services/device.service';
import {HttpService} from './services/http.service';
import {SubscriberService} from './services/subscriber.service';
import {LoggedInGuard} from '../logged-in.guard';
import {ImageUploadComponent} from './image-upload/image-upload.component';
import {MediaService} from './services/media.service';
import {EventManager} from '@angular/platform-browser';
import {CustomEventManager} from './custom-event-manager';
import {NumberDirective} from './number.directive';
import {LanguageNamePipe} from './language-name.pipe';
import {PayoutModelService} from './services/payout-model.service';
import { TooltipDirective } from './tooltip.directive';
import {validators} from './validators';
import { GeneratePayoutsDialogComponent } from './generate-payouts-dialog/generate-payouts-dialog.component';
import {PayoutTableComponent} from './payout-table/payout-table.component';
import {AffiliateService} from './services/affiliate.service';
import { AlertDialogComponent } from './alert-dialog/alert-dialog.component';

const mdModules = [
    VxComponentsModule
];

const components = [LoadingDialogComponent, ImageUploadComponent,
    GeneratePayoutsDialogComponent, PayoutTableComponent, ExpansionPanelComponent];
const directives = [DateDirective, NumberDirective, MaskDirective, TooltipDirective];
const pipes = [LanguageNamePipe];
const modules = [DataTableModule, CommonModule, FormsModule, ...mdModules];

@NgModule({
    declarations: [...components , ...directives, ...pipes, ...validators, AlertDialogComponent],
    imports: [...modules ],
    entryComponents: [LoadingDialogComponent, GeneratePayoutsDialogComponent, PayoutTableComponent, AlertDialogComponent],
    providers: [],
    exports: [...modules, ...components, ...directives, ...pipes, ...validators]
})
export class SharedModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: SharedModule,
            providers: [
                LoggedInGuard, SubscriberService, HttpService, DeviceService, SponsorService,
                TitleService, CategoryService, PendingChangesGuard, MediaService, PayoutModelService,
                AffiliateService, {provide: EventManager, useClass: CustomEventManager},
                {provide: VX_BUTTON_DEFAULT_COLOR, useValue: 'primary'},
                {provide: VX_BUTTON_DEFAULT_VARIATION, useValue: 'raised'}
            ]

        };
    }
}
