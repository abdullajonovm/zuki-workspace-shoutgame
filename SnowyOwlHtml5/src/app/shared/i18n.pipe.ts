import {Pipe, PipeTransform} from '@angular/core';
import {environment} from 'environments/environment';
import {LocalizedText} from 'app/model/localized-text';
import {Util} from 'app/util';

const STRINGS = environment.strings;
const LC: keyof LocalizedText = environment.languageCode as any;

const placeholderRegex = /{{.*?}}/g;

@Pipe({
    name: 'i18n'
})
export class I18nPipe implements PipeTransform {
    cache: { [key: string]: string } = {};

    transform(value: string | LocalizedText, ...args: string[]): string | void {
        if (!value)
            return;

        const defaultVal = '***BROKEN STRING***';
        let result: string;
        if (Util.isString(value)) {
            result = this.cache[value] || Util.get(STRINGS, value, defaultVal);
            this.cache[value] = result;

            const matches = result.match(placeholderRegex);
            if (matches && args) {
                matches.forEach((match, i) => {
                    result = result.replace(match, args[i]);
                });
            }
        } else {
            result = value[LC] || value['en'];
        }
        // TODO: add mixins
        return result || defaultVal;
    }
}
