import { Component, OnInit } from '@angular/core';
import { SubscriberService } from 'app/shared/services/subscriber.service';
import { Router } from '@angular/router';
import { TitleService } from '../shared/services/title.service';
import {VxDialog} from 'vx-components';
import {LoadingDialogComponent} from '../shared/loading-dialog.component';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    username = '';
    password = '';

    constructor(private subscriberService: SubscriberService, private router: Router, private titleService: TitleService,
                private dialog: VxDialog) {
    }

    ngOnInit(): void {
        if (this.subscriberService.isLoggedIn()) {
            this.router.navigate(['/game']);
        }
        this.username = '';
        this.password = '';
        this.titleService.title = 'Shout Trivia';
    }

    async login(): Promise<void> {
        const dialog = this.dialog.open(LoadingDialogComponent,  'Logging in...');
        await this.subscriberService.login(this.username, this.password);
        dialog.close();
    }
}
