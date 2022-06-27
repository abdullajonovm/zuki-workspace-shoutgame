import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {environment} from 'environments/environment';
import createAutoCorrectedDatePipe from 'text-mask-addons/dist/createAutoCorrectedDatePipe';
import {NgForm} from '@angular/forms';
import {NavigationService} from '../shared/services/navigation.service';

const STRINGS = environment.strings.login;

@Component({
    selector: 'sh-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
    dateMask = {
        mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/, /\d/, /\d/],
        pipe: createAutoCorrectedDatePipe('mm/dd/yyyy')
    };

    email: string;
    username: string;
    password: string;
    passwordConf: string;

    birthDate: string;


    // If false means we are signing up
    loggingIn: boolean;

    @ViewChild('form') form: NgForm;
    @ViewChild('signup') signupForm: NgForm;

    constructor(private router: Router, private subscriberService: SubscriberService,
                private dialogService: DialogService, private route: ActivatedRoute, private navigation: NavigationService) {
    }

    ngOnInit(): void {
        this.clear();
        if (this.subscriberService.isLoggedIn()) {
            this.router.navigate([this.subscriberService.redirectUrl || '/home']);
        }
        this.route.fragment.subscribe(f => {
            if (f === 'signup') {
                this.loggingIn = false;
            }
        })
    }

    async submit(): Promise<void> {
        if (this.dialogService.loadingIndicator)
            return;

        if (!this.loggingIn && this.password !== this.passwordConf) {
            this.dialogService.open({
                body: 'Your passwords are not the same.',
                buttons: [{text: STRINGS.core.ok}]
            });
            return;
        }

        this.dialogService.showLoadingIndicator(this.loggingIn ? STRINGS.loggingIn : STRINGS.signingUp);

        try {
            if (this.loggingIn) {
                await this.subscriberService.login(this.email, this.password);
            } else {

                const birthdate = new Date(this.birthDate);

                await this.subscriberService.signup(this.email, this.password, birthdate, this.username);
            }
            setTimeout(() => {
                this.router.navigate([this.subscriberService.redirectUrl || '/home']);

                // We don't want a login redirect to behave as a navigation, so remove that navigation from history
                if (this.subscriberService.redirectUrl) {
                    this.subscriberService.redirectUrl = undefined;
                    setTimeout(() => {
                        this.navigation.lastNavigation = undefined;
                    }, 200);
                }
            });
            this.clear();
        } catch (e) {
            this.dialogService.open({body: e.message});
        }
        this.dialogService.closeLoadingIndicator();
    }

    private clear(): void {
        this.email = '';
        this.username = '';
        this.password = '';
        this.passwordConf = '';
        this.birthDate = '';
        this.loggingIn = true;
        if (this.form)
            this.form.reset();
        if (this.signupForm)
            this.signupForm.reset();
    }
}
