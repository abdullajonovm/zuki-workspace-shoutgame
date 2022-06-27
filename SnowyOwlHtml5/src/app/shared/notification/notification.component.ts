import {Component, EventEmitter, Output} from '@angular/core';
import {DialogComponent, OnDialogOpen} from 'app/shared/dialog/dialog.component';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {environment} from 'environments/environment';

const STRINGS = environment.strings.notification;

@Component({
    selector: 'sh-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnDialogOpen {

    phoneMask = ['(', /[1-9]/, /\d/, /\d/, ')', ' ', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];
    codeMask = [/\d/, /\d/, /\d/, /\d/, /\d/, /\d/];
    code: string;
    preference: 'EMAIL' | 'SMS' | 'NONE';
    activeStep = 1;
    phone: string;
    verified = false;
    validState: 'none' | 'correct' | 'incorrect' | 'loading' = 'none';
    hasPreference = false;
    @Output()
    onFinish = new EventEmitter();

    dialog?: DialogComponent;

    constructor(private dialogService: DialogService, private subscriberService: SubscriberService) {
        this.phone = this.subscriberService.subscriber.phone || '';
        if (subscriberService.subscriber.roundNotificationPref) {
            this.preference = subscriberService.subscriber.roundNotificationPref;
            this.hasPreference = true;
        }
    }

    onDialogOpen(dialog: DialogComponent): void {
        dialog.dialogOptions.disableClose = true;
        this.dialog = dialog;
    }

    goToStep2(): void {
        if (this.preference === 'NONE') {
            const confirm = this.dialogService.open({
                body: `Are you sure you don't want to be notified? It is highly recommended.`,
                buttons: [
                    {value: 'yes', text: 'Yes'},
                    {text: 'No'}
                ],
                disableClose: true
            });
            confirm.onClose.subscribe((resp) => {
                if (resp === 'yes') {
                    this.subscriberService.setNotificationPreference(this.preference);
                    if (this.dialog)
                        this.dialog.close();

                    this.onFinish.next();
                }
            })
        } else if (this.preference === 'EMAIL') {
            this.subscriberService.setNotificationPreference(this.preference);
            if (this.dialog)
                this.dialog.close();

            this.onFinish.next();
        } else {
            this.activeStep = 2;
        }
    }

    showInfoSms(e: Event): void {
        this.dialogService.open({
            body: STRINGS.smsInfo,
            buttons: [{text: STRINGS.core.ok}]
        });
        e.preventDefault();
    }

    showInfoEmail(e: Event): void {
        this.dialogService.open({
            body: STRINGS.emailInfo,
            buttons: [{text: STRINGS.core.ok}]
        });
        e.preventDefault();
    }

    async sendCode(): Promise<void> {
        const phoneNumber = this.phone.replace(/\(?\)? ?-?/g, '');
        this.dialogService.showLoadingIndicator(STRINGS.sending);
        await this.subscriberService.sendVerificationCode(+phoneNumber);
        this.dialogService.closeLoadingIndicator();
        this.verified = true;
        this.validState = 'none';
    }

    async verify(): Promise<void> {
        this.validState = 'loading';
        const resp = await this.subscriberService.verify(+this.code);
        this.validState = resp.success ? 'correct' : 'incorrect';
    }

    async savePrefs(): Promise<void> {
        if (this.dialog)
            this.dialog.close();

        this.onFinish.next();
        this.subscriberService.setNotificationPreference(this.preference);
    }
}
