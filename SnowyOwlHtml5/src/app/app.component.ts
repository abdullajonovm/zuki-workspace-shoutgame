import {Component} from '@angular/core';
import * as Offline from 'offline-js';
import {NavigationEnd, Router} from '@angular/router';
import 'rxjs/add/operator/pairwise';
import 'rxjs/add/operator/filter';
import {NavigationService} from './shared/services/navigation.service';

@Component({
    selector: 'sh-root',
    template: `
        <router-outlet></router-outlet>`
})
export class AppComponent {

    constructor(private router: Router, private navigationService: NavigationService) {
        console.log(Offline);

        this.router.events.filter(e => e instanceof NavigationEnd).pairwise().subscribe((e) => {
            navigationService.lastNavigation = e as any;
        });
    }
}
