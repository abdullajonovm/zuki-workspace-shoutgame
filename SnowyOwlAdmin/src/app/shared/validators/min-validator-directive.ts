import {Directive, forwardRef, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Util} from '../../util';

@Directive({
    selector: '[min][formGroup], [min][ngModel]', // tslint:disable-line
    providers: [{provide: NG_VALIDATORS, useExisting: forwardRef(() => MinValidatorDirective), multi: true}]
})
export class MinValidatorDirective implements Validator {
    @Input() min!: number;

    validate(c: AbstractControl): ValidationErrors | null {
        const value = c.value;
        if (value === undefined || value === null) {
            return null;
        }
        const num = Util.parseNumber(value + '');
        const min = Util.parseNumber(this.min + '');
        return (num < min) ? {min, actual: num} : null;
    }

}
