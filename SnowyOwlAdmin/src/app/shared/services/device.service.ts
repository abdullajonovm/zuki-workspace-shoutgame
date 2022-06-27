
import {fromEvent as observableFromEvent,  Observable } from 'rxjs';


export class DeviceService {
    width: number;
    height: number;

    get isMobile(): boolean {
        return this.width <= 425;
    }

    constructor() {
        this.width = window.innerWidth;
        this.height = window.innerHeight;

        observableFromEvent(window, 'resize').subscribe(() => {
            this.width = window.innerWidth;
            this.height = window.innerHeight;
        });
    }
}
