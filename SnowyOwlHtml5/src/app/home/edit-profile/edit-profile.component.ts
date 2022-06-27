import {Component, OnInit} from '@angular/core';
import {DeviceService} from 'app/shared/services/device.service';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {environment} from 'environments/environment';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {MediaService} from 'app/home/media.service';
import {PaymentService} from '../../shared/services/payment.service';
import {NavigationService} from '../../shared/services/navigation.service';
import {ActivatedRoute, Route, Router} from '@angular/router';

const STRINGS = environment.strings.subscriber;

@Component({
    selector: 'sh-edit-profile',
    templateUrl: './edit-profile.component.html',
    styleUrls: ['./edit-profile.component.scss']
})
export class EditProfileComponent implements OnInit {

    subscriber = this.subscriberService.subscriber;
    state: State = 'DEFAULT';
    email: string;
    username: string;
    image: string;
    blob?: Blob;

    constructor(public deviceService: DeviceService, private mediaService: MediaService,
                public subscriberService: SubscriberService, private dialogService: DialogService,
                public paymentService: PaymentService, public navigationService: NavigationService, private router: Router,
                private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.fragment.subscribe(fragment => {
            if (fragment === 'wallet') {
                this.state = 'PAYMENT';
            }
        })
        if (!this.subscriberService.isLoggedIn()) {
            this.router.navigate(['/home/me']);
        }
    }

    async saveEmail(): Promise<void> {
        this.dialogService.showLoadingIndicator(STRINGS.saving);
        try {
            await this.subscriberService.update({email: this.email});
            this.state = 'DEFAULT';
        } catch (e) {
            this.dialogService.open({
                body: e.message,
                buttons: [{
                    text: STRINGS.core.ok
                }]
            });
        }
        this.dialogService.closeLoadingIndicator();
    }

    async saveUsername(): Promise<void> {
        this.dialogService.showLoadingIndicator(STRINGS.saving);
        try {
            await this.subscriberService.update({nickname: this.username});
            this.state = 'DEFAULT';
        } catch (e) {
            this.dialogService.open({
                body: e.message,
                buttons: [{
                    text: STRINGS.core.ok
                }]
            });
        }
        this.dialogService.closeLoadingIndicator();
    }

    async saveImage(): Promise<void> {
        if (!this.blob)
            return;

        this.dialogService.showLoadingIndicator(STRINGS.saving);
        try {
            const imageUrl = await this.mediaService.uploadImage(this.blob);
            await this.subscriberService.update({photoUrl: imageUrl});
            this.state = 'DEFAULT';
        } catch (e) {
            this.dialogService.open({
                body: e.message,
                buttons: [{
                    text: STRINGS.core.ok
                }]
            });
        }
        this.dialogService.closeLoadingIndicator();
    }

    handleFile(file: Blob): void {
        this.blob = undefined;
        if (file && file.type.includes('image')) {
            const reader = new FileReader();

            reader.onloadend = () => {
                this.image = reader.result;
            };

            reader.readAsDataURL(file);
            this.blob = file;
        }
    }

    goToHome(): void {
        let previousUrl = '/home/me';
        if (this.navigationService.lastNavigation) {
            previousUrl = this.navigationService.lastNavigation[0].url;
        }
        this.router.navigate([previousUrl]);

    }
}

type State = 'DEFAULT' | 'NAME' | 'EMAIL' | 'NOTIFIACTION' | 'IMAGE' | 'PAYMENT';
