import {ContentChild, Directive, HostListener, Input, OnDestroy, Optional} from '@angular/core';
import {AbstractControl, NgControl} from '@angular/forms';
import {Subscription} from 'rxjs';
import {Util} from './util';
import {debounceTime} from 'rxjs/operators';

@Directive({
    selector: '[appTooltip]'
})
export class FocusTooltipDirective implements OnDestroy {
    // @Input() name!: string;
    //
    // private touched = false;
    // private subscription?: Subscription;
    // private oldMessage?: string;
    // constructor(@Optional() private control: NgControl) {
    //     if (control && control.statusChanges) {
    //         this.subscription = control.statusChanges.pipe(debounceTime(100)).subscribe(a => {
    //             this.checkForError();
    //         })
    //     }
    // }
    //
    // ngOnDestroy(): void {
    //     if (this.subscription) {
    //         this.subscription.unsubscribe();
    //     }
    // }
    //
    //
    // @HostListener('focusin') onFocusIn(): void {
    //     if (this.tooltip) {
    //         setTimeout(() => this.tooltip.show(), 100);
    //     }
    // }
    //
    // @HostListener('focusout') onFocusOut(): void {
    //     if (this.tooltip) {
    //         this.tooltip.hide();
    //     }
    //     this.touched = true;
    //     this.checkForError();
    // }
    //
    // private checkForError(): void {
    //     const control = this.control;
    //     if (!control) {
    //         return;
    //     }
    //
    //     if (control.invalid && (control.touched || this.touched)) {
    //         this.oldMessage = this.oldMessage || this.tooltip.message;
    //         this.tooltip.message = Util.getControlErrorMessage(this.name, control);
    //         this.tooltip.tooltipClass = 'invalid';
    //         this.tooltip.show(0);
    //     } else if (this.oldMessage) {
    //         this.tooltip.message = this.oldMessage;
    //         this.tooltip.tooltipClass = '';
    //         this.oldMessage = undefined;
    //     }
    // }

}
