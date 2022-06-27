import { VxDialogDef, VxDialogRef } from 'vx-components';
import {Util} from '../util';
import {Component} from '@angular/core';
@Component({
    template: `<h2>{{ title }}</h2> <div><vx-spinner></vx-spinner></div> <p *ngIf="message">{{ message }}</p>`,
    styles: ['vx-spinner { width: 75px; height: 75px;} h2 {margin-top: 0;} div {text-align: center}']
})
export class LoadingDialogComponent extends VxDialogDef<LoadingInfo | string> {
    title = 'Loading...';
    message?: string;

    constructor(private dialog: VxDialogRef<LoadingDialogComponent>) {
        super();

        const content = dialog.data;
        if (typeof content === 'string') {
            this.title = content;
        } else {
            this.title = content.title;
            this.message = content.message;
        }
    }
}

interface LoadingInfo {
    title: string;
    message: string;
}
