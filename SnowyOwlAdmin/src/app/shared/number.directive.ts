import {Directive, EventEmitter, Inject, Input, OnDestroy, OnInit, Output, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, FormControl, NgControl, NgModel} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil, distinctUntilChanged} from 'rxjs/operators';
import {Util} from '../util';

@Directive({
    selector: 'input[appNumber][ngModel]'
})
export class NumberDirective implements OnDestroy {
    @Output() appNumberChange = new EventEmitter<number>();

    private number?: number;
    private onDestroy$ = new Subject();
    private setting = false;
    constructor(@Self() private ngControl: NgControl) {
        ngControl.valueChanges!.pipe(takeUntil(this.onDestroy$), distinctUntilChanged()).subscribe(value => {
            if (this.setting)
                return;

            value = value || '';
            value = Util.parseNumber(value) || undefined;
            if (value !== this.number) {
                this.appNumberChange.emit(value);
                this.number = value;
            }
        })
    }

    ngOnDestroy(): void {
        this.onDestroy$.next();
        this.onDestroy$.complete();
    }

    @Input()
    set appNumber(num: number) {
        if (num !== this.number) {
            this.number = num;
            this.setting = true;
            setTimeout(() => {
                this.ngControl.control!.setValue(num + '');
                this.setting = false;
            });
        }
    }
}
