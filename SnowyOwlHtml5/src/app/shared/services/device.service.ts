import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/fromEvent';

export class DeviceService {
    width: number;
    height: number;

    constructor() {
        this.width = window.innerWidth;
        this.height = window.innerHeight;

        Observable.fromEvent(window, 'resize').subscribe(() => {
            this.width = window.innerWidth;
            this.height = window.innerHeight;
        });
    }

    get isMobile(): boolean {
        return this.width <= 480;
    }
}
