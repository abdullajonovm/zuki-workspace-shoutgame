import {Component, Input, OnInit} from '@angular/core';
import {Game} from '../../model/game';
import {Util} from '../../util';
import {ControlContainer, NgForm} from '@angular/forms';

const regex = /<a.*?href="(.*?)".*?>(.*?)<\/a/gi;
const startingHtml = `<style> body {font-family: HelveticaNeue-Light,'Helvetica Neue Light','Helvetica Neue',Helvetica,Arial,'Lucida Grande',sans-serif; }a { display: block; color: black; } 
                    a:not(:first-child) {margin-top: 10px;} h2 {padding-top: 0}</style><h2>Game Guide</h2>`;
@Component({
    selector: 'app-game-guide',
    templateUrl: './game-guide.component.html',
    styleUrls: ['./game-guide.component.scss'],
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class GameGuideComponent implements OnInit {

    @Input() game!: Game;
    @Input() editable = true;

    guideType: GuideType = 'none';
    guideUrls: GuideUrl[] = [{text: {}, url: {}}];

    private cachedHtmls?: any;
    private cachedUrl?: string;


    ngOnInit(): void {

    }

    initFromGame(game: Game): void {
        if (!Util.isEmpty(this.game.guideHtmls)) {
            if (this.game.guideHtmls.en.includes(startingHtml)) {
                this.guideType = 'multiple';
                this.getGuideUrlsFromHtml(this.game.guideHtmls);
            } else {
                this.guideType = 'html'
            }
        } else if (this.game.guideUrl) {
            this.guideType = 'url';
        } else {
            this.guideType = 'none';
        }
    }

    removeGuideUrl(guideUrl: GuideUrl): void {
        this.guideUrls.splice(this.guideUrls.indexOf(guideUrl), 1);
    }

    prepareForLocalStorage(json: any): void {
        json._guideUrls = this.guideUrls;
        json._guideType = this.guideType;
    }

    fromLocalStorage(json: any): void {
        this.guideUrls = json._guideUrls || this.guideUrls;
        this.guideType = json._guideType || this.guideType;
    }

    private generateGuideHtml(): void {
        const htmls: any = {};
        for (const code of this.game.allowableLanguageCodes) {
            let html = startingHtml;
            html += this.guideUrls.map(guide => `<a target="_blank" href="${guide.url[code]}">${guide.text[code]}</a>`).join('');
            htmls[code] = html;
        }
        this.game.guideHtmls = htmls;
    }

    prepareForServer(): void {
        if (this.guideType === 'url') {
            this.game.guideHtmls = undefined;
        } else if (this.guideType === 'html') {
            this.game.guideUrl = undefined;
        } else if (this.guideType === 'multiple') {
            this.game.guideUrl = undefined;
            this.generateGuideHtml();
        } else {
            this.game.guideHtmls = undefined;
            this.game.guideUrl = undefined;
        }
    }

    private getGuideUrlsFromHtml(guideHtmls: { [key: string]: string }): void {
        this.guideUrls = [];
        for (const code of this.game.allowableLanguageCodes) {
            const html = guideHtmls[code];
            let matches, i = 0;
            while (matches = regex.exec(html)) {
                if (!this.guideUrls[i])
                    this.guideUrls[i] = {url: {}, text: {}};

                this.guideUrls[i].url[code] = matches[1];
                this.guideUrls[i].text[code] = matches[2];
                i++;
            }
        }
    }
}

type GuideType = 'none' | 'url' | 'multiple' | 'html';

interface GuideUrl {
    url: any;
    text: any;
}
