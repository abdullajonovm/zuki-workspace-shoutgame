import {Component, ElementRef, ViewChild} from '@angular/core';
import {DialogComponent, OnDialogOpen} from '../shared/dialog/dialog.component';
import {Game} from '../model/game';
import {environment} from '../../environments/environment';

@Component({
    template: `
    <iframe #frame frameborder="0" height="0"></iframe>
    `
})
export class GameGuideDialogComponent implements OnDialogOpen<Game> {

    @ViewChild('frame') frame: ElementRef;

    onDialogOpen(dialog: DialogComponent, game: Game): void {
        const frame: HTMLIFrameElement = this.frame.nativeElement;
        setTimeout(() => {
            frame.contentWindow.document.open();
            const toWrite = game.guideHtmls[environment.languageCode] || game.guideHtmls['en'];
            frame.contentWindow.document.write(toWrite);
            frame.contentWindow.document.head.innerHTML += '<base target="_parent">';
            frame.contentWindow.document.close();
            frame.height = frame.contentWindow.document.body.scrollHeight + 'px';
            frame.width = frame.contentWindow.document.body.scrollWidth + 'px';
        });
    }
}
