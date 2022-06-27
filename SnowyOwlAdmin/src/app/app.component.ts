import {Component, OnInit, ViewChild} from '@angular/core';
import { DeviceService } from './shared/services/device.service';
import { SubscriberService } from './shared/services/subscriber.service';
import { TitleService } from './shared/services/title.service';
import {VxDialog, VxMenuComponent} from 'vx-components';
import {APP_VERSION} from 'environments/version';
import {ChangelogComponent} from './changelog/changelog.component';
import {lastSeenVersionLS} from './constants';
import {HttpService} from './shared/services/http.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    opened = false;
    appVersion = APP_VERSION;
    hasNewChangelog = false;

    @ViewChild('drop', { static: false }) dropdown!: VxMenuComponent<null>;

    private versionClicks = 0;
    constructor(public deviceService: DeviceService, public subscriberService: SubscriberService,
                public titleService: TitleService, private dialog: VxDialog, public httpService: HttpService) {
    }

    push(): void {
        this.httpService.sendRequest('/snowladmin/pushTest').subscribe(() => {
            console.log('Success!')
        },
            err => {
            console.log('Error', err);
            })
    }

    logout(): void {
        this.opened = false;
        this.dropdown.visible = false;
        this.subscriberService.logout();
    }

    ngOnInit(): void {
        const lastSeenVersion = +(localStorage.getItem(lastSeenVersionLS) || 0);
        this.hasNewChangelog = APP_VERSION > lastSeenVersion;
    }

    versionClick(): void {
        this.versionClicks++;
        setTimeout(() => {
            this.versionClicks --;
        }, 1350);
        if (this.versionClicks === 5) {
            this.dialog.open(ChangelogComponent);
            this.hasNewChangelog = false;
        }
    }
}
