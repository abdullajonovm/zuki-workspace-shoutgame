import {ModuleWithProviders, NgModule} from '@angular/core';
import {NavHeaderLeftDirective, NavHeaderRightDirective, NavItemDirective, PageBodyDirective, PageComponent} from './page/page.component';
import {PaymentComponent} from './payment/payment.component';
import {CommonModule} from '@angular/common';
import {HttpService} from 'app/shared/services/http.service';
import {GameService} from 'app/shared/services/game.service';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {DialogModule} from 'app/shared/dialog/dialog.module';
import {GameplayService} from 'app/shared/services/gameplay.service';
import {I18nPipe} from 'app/shared/i18n.pipe';
import {NotificationComponent} from './notification/notification.component';
import {TextMaskModule} from 'angular2-text-mask';
import {FormsModule} from '@angular/forms';
import {ValidPhoneDirective} from 'app/shared/validPhone.directive';
import {PhonePipe} from 'app/shared/phone.pipe';
import {DefaultImageDirective} from 'app/shared/default-image.directive';
import {CategoryNamePipe} from './category.pipe';
import {GameCardComponent} from './game-card/game-card.component';
import {DeviceService} from './services/device.service';
import {PaymentService} from './services/payment.service';
import {SyncService} from './services/sync.service';
import {LogService} from 'app/shared/services/log.service';
import {VxCheckboxModule, VxRadioModule, VxStepperModule} from 'vx-components';
import {NavigationService} from './services/navigation.service';
import {DatePipe} from './date.pipe';
import { EcheckComponent } from './payment/echeck/echeck.component';
import { CardComponent } from './payment/card/card.component';

const declarations = [
    PageComponent, NavItemDirective, PageBodyDirective, NavHeaderRightDirective, NavHeaderLeftDirective,
    PaymentComponent, I18nPipe, NotificationComponent, ValidPhoneDirective, PhonePipe, DefaultImageDirective,
    CategoryNamePipe, GameCardComponent, DatePipe, EcheckComponent, CardComponent
];

const imports = [CommonModule, DialogModule, TextMaskModule, FormsModule, VxCheckboxModule, VxStepperModule, VxRadioModule];

@NgModule({
    imports,
    entryComponents: [NotificationComponent, PaymentComponent],
    exports: [...declarations, ...imports],
    providers: [],
    declarations: [...declarations]
})
export class SharedModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: SharedModule,
            providers: [
                HttpService,
                GameService,
                SubscriberService,
                GameplayService,
                DeviceService,
                PaymentService,
                SyncService,
                LogService,
                NavigationService
            ]

        };
    }
}
