import {AbstractControl, FormControl, FormGroup, NgControl} from '@angular/forms';

export class Util {
    static formatDate(date: Date, includeTime = false): string {
        const monthNames = [
            'January', 'February', 'March',
            'April', 'May', 'June', 'July',
            'August', 'September', 'October',
            'November', 'December'
        ];

        function fixNum(num: number): string | number {
            if (num < 10) {
                return '0' + num;
            } else {
                return num;
            }
        }

        const day = date.getDate();
        const monthIndex = date.getMonth();
        const year = date.getFullYear();
        let hours = date.getHours();
        const mins = date.getMinutes();
        const PM = hours >= 12;
        if (hours > 12) {
            hours -= 12;
        }

        if (includeTime) {
            return `${monthNames[monthIndex]} ${fixNum(day)}, ${year} @ ${fixNum(hours)}:${fixNum(mins)} ${PM ? 'PM' : 'AM'}`;
        }

        return monthNames[monthIndex] + ' ' + day + ', ' + year;
    }

    static isEmpty(obj: any): boolean {
        return !obj || Object.keys(obj).length === 0;
    }

    static copyTextToClipboard(text: string): boolean {
        const textArea = document.createElement('textarea');

        // Place in top-left corner of screen regardless of scroll position.
        textArea.style.position = 'fixed';
        textArea.style.top = '0';
        textArea.style.left = '0';

        // Ensure it has a small width and height. Setting to 1px / 1em
        // doesn't work as this gives a negative w/h on some browsers.
        textArea.style.width = '2em';
        textArea.style.height = '2em';
        textArea.style.fontSize = '18px';

        // We don't need padding, reducing the size if it does flash render.
        textArea.style.padding = '0';

        // Clean up any borders.
        textArea.style.border = 'none';
        textArea.style.outline = 'none';
        textArea.style.boxShadow = 'none';

        // Avoid flash of white box if rendered for any reason.
        textArea.style.background = 'transparent';


        textArea.value = text;

        document.body.appendChild(textArea);

        if (navigator.userAgent.match(/ipad|ipod|iphone/i)) {
            textArea.contentEditable = 'true';
            textArea.readOnly = true;
            const range = document.createRange();
            range.selectNodeContents(textArea);
            const selection = window.getSelection();
            if (!selection) {
                return false;
            }
            selection.removeAllRanges();
            selection.addRange(range);
            textArea.setSelectionRange(0, 999999);
        } else {
            textArea.select();
        }

        let successful = false;
        try {
            successful = document.execCommand('copy');
        } catch (err) {
            successful = false;
            console.log('Oops, unable to copy', err);
        }

        document.body.removeChild(textArea);
        return successful;
    }

    /**
     * Searches through an array to find a key that matches a value
     * @example Util.find([{a: 1}, {a: 2}], 'a', 1]) => {a: 1}
     */
    static find<T, X extends keyof T>(arr: T[], key: X, val: T[X]): T | null {
        for (const item of arr) {
            if (item[key] && item[key] === val) {
                return item;
            }
        }
        return null;
    }

    static markAllTouched(control: AbstractControl): void {
        if (control.hasOwnProperty('controls')) {
            control.markAsTouched(); // mark group
            const ctrl = control as any;
            for (const inner in ctrl.controls) {
                if (ctrl.controls.hasOwnProperty(inner))
                    this.markAllTouched(ctrl.controls[inner] as AbstractControl);
            }
        } else {
            ((control) as FormControl).markAsTouched();
        }
    }

    static markAllDisabled(control: AbstractControl, disabled = true): void {
        disabled ? control.disable() : control.enable();

        if (control.hasOwnProperty('controls')) {
            const ctrl = control as any;
            for (const inner in ctrl.controls) {
                if (ctrl.controls.hasOwnProperty(inner))
                    this.markAllDisabled(ctrl.controls[inner] as AbstractControl, disabled);
            }
        }
    }

    static findInvalidControlErrorMessage(control: AbstractControl): string | undefined {
        if (control.hasOwnProperty('controls')) {
            const ctrl = control as FormGroup;

            const controls = ctrl.controls;
            for (const key in controls) {
                if (controls.hasOwnProperty(key)) {
                    const inner = controls[key];
                    if (inner.invalid) {
                        return this.findInvalidControlErrorMessage(inner) || this.getControlErrorMessage(key, inner);
                    }
                }
            }
        }

        return undefined;
    }

    static keyBy<T, X extends keyof T, K extends T[X] & string>(obj: T[], prop: X): { [F in K]: T } {
        const result: any = {};
        for (const item of obj) {
            if (item[prop]) {
                result[item[prop]] = item;
            }
        }
        return result;
    }

    static parseNumber(num: string): number {
        if (!num) return 0;

        return +num.replace(/[^0-9-.]/g, '');
    }

    static getControlErrorMessage(name: string, control: AbstractControl | NgControl): string {
        const errors = control.errors || {};
        if ('min' in errors) {
            return `The ${name} field has a minimum of ${errors.min}.`;
        } else if ('max' in errors) {
            return `The ${name} field has a maximum of ${errors.max}.`;
        } else if ('minDate' in errors) {
            return `The ${name} field cannot be earlier than ${this.formatDate(errors.minDate, true)}.`;
        }  else if ('maxDate' in errors) {
            return `The ${name} field cannot be later than ${this.formatDate(errors.maxDate, true)}.`;
        } else if ('requiredDate' in errors) {
            return `The ${name} field is required, and is not a valid date.`;
        } else {
            return `The ${name} field is required.`;
        }
    }

    static enableJSONDates(): void {
        const oldParse = JSON.parse.bind(JSON);
        const reISO = /^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(\.\d+)?(([+-]\d\d:\d\d)|Z)?$/i;

        const dateParser = (key: any, value: any) => {
            if (typeof value === 'string') {
                if (reISO.exec(value)) return new Date(value);
            }
            return value;
        };

        // Overrides the JSON.parse function to automatically convert date strings into dates
        JSON.parse = (json) => {
            return oldParse(json, dateParser);
        };
    }

}

export function numberMask(text: string): string {
    text = text.replace(/[^0-9.]/g, '');
    if (text.length === 0) return text;
    return (+text).toLocaleString();
}

export function numberMaskNoDecimal(text: string): string {
    text = text.split('.')[0];
    text = text.replace(/[^0-9]/g, '');
    if (text.length === 0) return text;
    return (+text).toLocaleString();
}

export function moneyMask(text: string): string {
    if (text.length === 0) return text;
    const result = ('$' + numberMask(text)).split('.');

    if (result.length === 1) {
        return result[0];
    } else {
        return result[0] + '.' + result[1].slice(0, 2);
    }
}

export function moneyMaskNoDecimal(text: string): string {
    if (text.length === 0) return text;
    return '$' + numberMaskNoDecimal(text);
}
