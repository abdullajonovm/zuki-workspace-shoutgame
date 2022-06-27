import {Component, OnInit} from '@angular/core';
import { VxToast } from 'vx-components';
import {TitleService} from '../shared/services/title.service';
import {OtherService} from './other.service';

@Component({
    selector: 'app-other',
    templateUrl: './other.component.html',
    styleUrls: ['./other.component.scss']
})
export class OtherComponent implements OnInit {
    loading = true;
    canSeeContent = false;

    constructor(private titleService: TitleService,
                private otherService: OtherService,
                private vxToast: VxToast) {
    }

    async ngOnInit(): Promise<void> {
        this.titleService.title = 'Other';

        this.canSeeContent = await this.otherService.getCanSeeContentWithoutLogin();

        this.loading = false;
    }

    async setCanSeeContent(canSee: boolean): Promise<void> {
        this.loading = true;
        try {
            const resp = await this.otherService.setCanSeeContentWithoutLogin(canSee);

            if (!resp.success) {
                this.errorSettingCanSee();
                return;
            }

            this.canSeeContent = canSee;

            this.vxToast.open({
                title: 'Success!',
                text: canSee ? 'No longer forcing users to login.' : 'Now forcing users to login.',
                type: 'success'
            })
        } catch {
            this.errorSettingCanSee();
        } finally {
            this.loading = false;
        }

    }

    private errorSettingCanSee(): void {
        this.vxToast.open({
            title: 'Error!',
            text: 'An unexpected error occurred.  Please try again later.',
            type: 'error'
        })
    }

}
