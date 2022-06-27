import { coerceBooleanProperty } from '@angular/cdk/coercion';
import {
    Directive,
    ElementRef,
    EventEmitter,
    forwardRef,
    HostBinding,
    Input,
    OnChanges,
    OnInit,
    Output, SimpleChanges
} from '@angular/core';
import { AbstractControl, NG_VALIDATORS, NgModel, ValidationErrors, Validator } from '@angular/forms';
import format from 'date-fns/format';

export const dateMask = {
    mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/, /\d/, /\d/, ' ', /\d/, /\d/, ':', /\d/, /\d/, ' ', /[APap]/, 'M'],
    keepCharPositions: true,
    guide: true,
    showMask: true,
    pipe: createAutoCorrectedDatePipe()
};

export const dateMaskShort = {
    mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/, /\d/, /\d/],
    keepCharPositions: true,
    guide: true,
    showMask: true,
    pipe: createAutoCorrectedDatePipe('MM/DD/YYYY')
};

@Directive({
    selector: 'input[appDate][ngModel]'
})
export class DateDirective implements OnInit {

    @HostBinding('value')
    value!: string;

    @Input()
    get appDate(): Date {
        return this._appDate;
    }

    set appDate(value: Date) {
        this._appDate = value;
        if (this.lastParsedDate !== value) {
            this.lastParsedDate = value;
            setTimeout(() => {
                const parsed = format(value, 'MM/DD/YYYY hh:mm A');
                if (this.ngModel.control.value !== parsed) {
                    this.ngModel.control.setValue(parsed);
                    this.validate();
                }
            })
        }
    }


    @Input()
    get maxDate(): Date | undefined {
        return this._maxDate;
    }

    set maxDate(value: Date | undefined) {
        if (this.maxDate !== value) {
            this._maxDate = value;
            this.validate();
        }
    }
    private _maxDate?: Date;

    @Input()
    get minDate(): Date | undefined {
        return this._minDate;
    }

    set minDate(value: Date | undefined) {
        if (this.minDate !== value) {
            this._minDate = value;
            this.validate();
        }
    }
    private _minDate?: Date;

    @Input() set required(required: boolean) {
        this._required = coerceBooleanProperty(required);
        this.validate();
    }
    get required(): boolean {
        return this._required;
    }

    @Output()
    readonly appDateChange = new EventEmitter<Date>();

    private lastParsedDate!: Date;
    private _appDate!: Date;
    private _required = false;
    private errors: any;
    constructor(private ngModel: NgModel, private elementRef: ElementRef) {}

    ngOnInit(): void {

        this.ngModel.valueChanges!.subscribe((val) => {
            if (!val) {
                // this.appDateChange.emit();
                return;
            }

            val = val.replace(/\u2000/g, ' ');
            const parsed = new Date(val);
            if (val) {
                const time = parsed.getTime();
                this.lastParsedDate = parsed;

                if (this.appDate && time && time !== this.appDate.getTime()) {
                    this.appDateChange.emit(parsed);
                } else if (time && !this.appDate) {
                    this.appDateChange.emit(parsed);
                }
                this.validate();
            }

        });
    }

    validate(): void {
        let errors = null;

        if (this.required && (!this.lastParsedDate || !this.lastParsedDate.getTime())) {
            errors = {requiredDate: true};
        } else if (this._minDate && this.lastParsedDate < this._minDate) {
            errors = {minDate: this._minDate};
        } else if (this._maxDate && this.lastParsedDate > this._maxDate) {
            errors = {maxDate: this._maxDate};
        }

        if (JSON.stringify(errors) !== JSON.stringify(this.errors) && this.ngModel && this.ngModel.control) {
            this.errors = errors;
            this.ngModel.control.setErrors(errors);
        }
    }

}


function createAutoCorrectedDatePipe(dateFormat = 'MM/DD/YYYY hh:mm'): Function {
    return (conformedValue: string, config: any) => {
        const caret = config.currentCaretPosition - 1;
        const configVal = config.rawValue.charAt(caret);
        const conformedVal = conformedValue.charAt(caret);

        if (conformedVal !== configVal && config.placeholder.indexOf(conformedVal) === -1) {
            conformedValue = conformedValue.substr(0, caret) + configVal + conformedValue.substr(caret + 1);
        }

        const indexesOfPipedChars: number[] = [];
        const dateFormatArray = dateFormat.split(/[^hmDMY]+/);
        const maxValue: {[key: string]: number} = {DD: 31, MM: 12, YY: 99, YYYY: 9999, hh: 12, mm: 59, ss: 59};
        const minValue: {[key: string]: number} = {DD: 1, MM: 1, YY: 0, YYYY: 1, hh: 1, mm: 0, ss: 59};
        const conformedValueArr = conformedValue.split('');

        // Check first digit
        dateFormatArray.forEach((formatted) => {
            const position = dateFormat.indexOf(formatted);
            const maxFirstDigit = parseInt(maxValue[formatted].toString().substr(0, 1), 10);

            if (parseInt(conformedValueArr[position], 10) > maxFirstDigit) {
                conformedValueArr[position + 1] = conformedValueArr[position];
                conformedValueArr[position] = '0';
                indexesOfPipedChars.push(position)
            }
        });

        // Check for invalid date
        const isInvalid = dateFormatArray.some((formatted) => {
            const position = dateFormat.indexOf(formatted);
            const length = formatted.length;
            const textValue = conformedValue.substr(position, length).replace(/\D/g, '');
            const value = parseInt(textValue, 10);

            return value > maxValue[formatted] || (textValue.length === length && value < minValue[formatted])
        });

        if (isInvalid) {
            return false
        }

        return {
            value: conformedValueArr.join('').replace('aM', 'AM').replace('pM', 'PM'),
            indexesOfPipedChars
        }
    }
}
