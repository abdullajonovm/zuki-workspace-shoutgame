import {Injectable} from '@angular/core';
import {NavigationEnd} from '@angular/router';

@Injectable()
export class NavigationService {
    lastNavigation?: [NavigationEnd, NavigationEnd];

}
