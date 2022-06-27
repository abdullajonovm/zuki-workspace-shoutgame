import {Component, ContentChildren, Directive, ElementRef, EventEmitter, Input, Output, QueryList, ViewChild} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/take';
import {DeviceService} from '../services/device.service';

@Directive({
    selector: '[shHeaderRight]'
})
export class NavHeaderRightDirective {
}

@Directive({
    selector: '[shHeaderLeft]'
})
export class NavHeaderLeftDirective {
}

@Directive({
    selector: '[shNavItem]'
})
export class NavItemDirective {

}

@Directive({
    selector: '[shPageBody]'
})
export class PageBodyDirective {

}

const PULL_DISTANCE = 60;
const MIN_PULL_TIME = 1000;

@Component({
    selector: 'sh-page',
    templateUrl: './page.component.html',
    styleUrls: ['./page.component.scss']
})
export class PageComponent {
    @ContentChildren(NavItemDirective) navItems: QueryList<ElementRef>;

    @ViewChild('puller') puller: ElementRef;
    lastTouchY?: number;
    currentTransform = 0;
    startedPulling = false;
    pullState: PullState = 'default';

    @Input() noRefresh = false;
    @Input() clickable = false;

    @Output() onRefresh = new EventEmitter<Subject<void>>();
    @Output() onTitleClicked = new EventEmitter();

    private minTimePassed = false;

    onTouchMove(event: TouchEvent): boolean | void {
        let returnValue = true;
        if (this.noRefresh || this.pullState === 'refreshing') {
            return;
        }

        const scrollTop = document.scrollingElement!.scrollTop;
        const newLocation = event.touches[0].screenY;
        if (this.lastTouchY) {
            const change = newLocation - this.lastTouchY;
            this.currentTransform += change;

            if (!this.startedPulling && change > 0 && scrollTop === 0) {
                this.startedPulling = true;
                this.currentTransform = 0;
            } else if (this.startedPulling && (this.currentTransform <= 0 || scrollTop < 0)) {
                this.clearPulling();
            }

            if (this.startedPulling) {
                const toMove = this.currentTransform > PULL_DISTANCE ? PULL_DISTANCE : this.currentTransform;
                this.puller.nativeElement.style.transform = `translateY(${toMove}px)`;
                returnValue = false;
            }
        }


        if (this.currentTransform >= PULL_DISTANCE || document.body.scrollTop <= -PULL_DISTANCE) {
            this.pullState = 'pulled';
        } else if ((this.currentTransform < PULL_DISTANCE || (scrollTop < 0 && scrollTop > -PULL_DISTANCE)) && this.pullState === 'pulled') {
            this.pullState = 'default';
        }
        this.lastTouchY = newLocation;

        return returnValue;
    }

    onTouchEnd(): void {
        this.clearPulling(false);
        if (this.pullState === 'pulled') {
            this.pullState = 'refreshing';
            this.animateTopToNumber(PULL_DISTANCE);

            this.minTimePassed = false;
            const refreshDoneSubject = new Subject<void>();
            setTimeout(() => {
                if (refreshDoneSubject.isStopped) {
                    this.animateTopToNumber(0);
                    setTimeout(() => {
                        this.pullState = 'default';
                    }, 100)
                }
                this.minTimePassed = true;
            }, MIN_PULL_TIME);

            refreshDoneSubject.take(1).subscribe(() => {
                refreshDoneSubject.complete();
                if (this.minTimePassed) {
                    this.animateTopToNumber(0);
                    setTimeout(() => {
                        this.pullState = 'default';
                    }, 100)
                }
            });

            this.onRefresh.next(refreshDoneSubject);
        } else if (this.pullState === 'default') {
            this.clearPulling(true);
        }
    }

    onTitleClick(): void {
        if (this.clickable)
            this.onTitleClicked.emit();
    }

    private clearPulling(resetTransform = true): void {
        this.currentTransform = 0;
        this.startedPulling = false;
        this.lastTouchY = undefined;
        if (resetTransform) {
            this.animateTopToNumber(0);
        }
    }

    private animateTopToNumber(num: number): void {
        this.puller.nativeElement.style.transition = 'transform 100ms';
        this.puller.nativeElement.style.transform = `translateY(${num}px)`;
        setTimeout(() => {
            this.puller.nativeElement.style.transition = '';
        }, 100)
    }

}

type PullState = 'default' | 'refreshing' | 'pulled';
