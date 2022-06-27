import { Directive, DoCheck, ElementRef, HostListener, Input, OnDestroy, Optional } from '@angular/core';
import { NgControl, NgForm } from '@angular/forms';
import { Util } from '../util';

@Directive({
    selector: '[appTooltip], [ngModel]'
})
export class TooltipDirective implements OnDestroy, DoCheck {
    @Input('appTooltip') text?: string;
    @Input() name?: string;

    tooltip = createTooltip();


    private visible = false;
    private errorMessage?: string;
    constructor(private el: ElementRef<HTMLElement>,
                @Optional() private control?: NgControl,
                @Optional() private form?: NgForm) {

    }

    @HostListener('mouseover')
    @HostListener('focusin')
    show(): void {
        if (this.visible) return;
        if (!this.text && !this.errorMessage) return;

        this.visible = true;
        document.body.appendChild(this.tooltip);
        this.updateTooltip();
    }

    @HostListener('mouseout')
    @HostListener('focusout')
    // @HostListener('window:focusout')
    hide(): void {
        if (!this.visible) return;
        this.visible = false;
        this.tooltip.remove();
    }

    ngOnDestroy(): void {
        this.tooltip.remove();
    }

    ngDoCheck(): void {
        const control = this.control;
        if (control && this.name) {
            if (control.invalid && (control.touched || (this.form && this.form.submitted))) {
                if (this.errorMessage) return;

                this.errorMessage = Util.getControlErrorMessage(this.name, control);
                this.tooltip.className += ' invalid';
                this.updateTooltip();
            } else if (this.errorMessage) {
                this.errorMessage = undefined;
                this.tooltip.className = this.tooltip.className.replace(' invalid', '');
                this.updateTooltip();
            }
        }
    }

    private updateTooltip(): void {
        if (!this.visible) return;
        if (!this.errorMessage && !this.text) {
            return this.hide();
        }

        this.tooltip.innerText = this.errorMessage! || this.text!;

        const {top, left, height} = this.el.nativeElement.getBoundingClientRect();
        this.tooltip.style.top = `${top + height + 10}px`;
        this.tooltip.style.left = `${left}px`;
    }
}

function createTooltip(): HTMLDivElement {
    const result = document.createElement('div');
    result.className = 'app-tooltip';
    return result;
}
