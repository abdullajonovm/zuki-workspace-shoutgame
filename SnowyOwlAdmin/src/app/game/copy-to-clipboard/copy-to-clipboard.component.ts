import { Component, OnInit } from '@angular/core';
import {utils} from 'protractor';
import { VxDialogDef, VxDialogRef, VxToast } from 'vx-components';
import {Util} from '../../util';

@Component({
  selector: 'app-copy-to-clipboard',
  templateUrl: './copy-to-clipboard.component.html',
  styleUrls: ['./copy-to-clipboard.component.scss']
})
export class CopyToClipboardComponent extends VxDialogDef<string> {
    gameId: string;
    userName = '';

    constructor(private dialog: VxDialogRef<CopyToClipboardComponent>, private toast: VxToast) {
        super();
        this.gameId = dialog.data;
    }

    get url(): string {
        if (this.userName && this.userName.length) {
            return `https://${this.userName}.shouttrivia.com/play/game/${this.gameId}`;
        } else {
            return `https://shouttrivia.com/play/game/${this.gameId}`;
        }
    }

    close(): void {
        this.dialog.close();
    }

    copy(): void {
        const successful = Util.copyTextToClipboard(this.url);
        this.toast.open({
            text: (successful ? 'Copied to clipboard!' : 'Could not copy to clipboard.'),
            type: successful ? 'success' : 'error'
        });

        if (successful) {
            this.close();
        }
    }
}
