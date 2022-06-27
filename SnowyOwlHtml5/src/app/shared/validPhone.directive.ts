import {AbstractControl, NG_VALIDATORS, Validator} from '@angular/forms';
import {Directive} from '@angular/core';

@Directive({
    selector: '[validPhone]',
    providers: [{provide: NG_VALIDATORS, useExisting: ValidPhoneDirective, multi: true}]
})
export class ValidPhoneDirective implements Validator {
    validate(control: AbstractControl): { [key: string]: any } | null {
        const regex = /\([1-9]\d{2}\) \d{3}-\d{4}/;
        if (regex.test(control.value)) {
            return null;
        } else {
            return {
                invalidPhone: true
            };
        }
    }
}
