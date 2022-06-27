import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NavigationService} from '../shared/services/navigation.service';
import {Router} from '@angular/router';

@Component({
    template: `
        <sh-page title="Privacy" [noRefresh]="true">
            <div shHeaderRight (click)="goBack()">Close</div>
            <div shPageBody>
                <div class="card">
                    <iframe #frame frameborder="0" width="100%"></iframe>
                </div>
            </div>
        </sh-page>
    `,
    styles: [`[shHeaderRight] {
        cursor: pointer
    }`]
})
export class PrivacyComponent implements OnInit {
    @ViewChild('frame') frame: ElementRef;

    constructor(private http: HttpClient, private navigationService: NavigationService, private router: Router) {
    }

    ngOnInit(): void {
        const frame: HTMLIFrameElement = this.frame.nativeElement;
        this.http.get('http://millionize.net/legal/privacy.html', {responseType: 'text'}).subscribe((resp) => {
            frame.contentWindow.document.open();
            frame.contentWindow.document.write(resp);
            frame.contentWindow.document.close();
            frame.height = frame.contentWindow.document.body.scrollHeight + 'px';
        })
    }

    goBack(): void {
        if (this.navigationService.lastNavigation) {
            this.router.navigate([this.navigationService.lastNavigation[0].url]);
        } else {
            this.router.navigate(['/home/all']);
        }
    }

}
