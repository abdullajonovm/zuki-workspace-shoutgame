import {Directive, forwardRef, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Util} from '../../util';

@Directive({
    selector: '[max][formGroup], [max][ngModel]', // tslint:disable-line
    providers: [{provide: NG_VALIDATORS, useExisting: forwardRef(() => MaxValidatorDirective), multi: true}]
})
export class MaxValidatorDirective implements Validator {
    @Input() max!: number;

    validate(c: AbstractControl): ValidationErrors | null {
        const value = c.value;
        if (value === undefined || value === null) {
            return null;
        }
        const num = Util.parseNumber(value + '');
        const max = Util.parseNumber(this.max + '');
        return (num > max) ? {max, actual: num} : null;
    }

}
