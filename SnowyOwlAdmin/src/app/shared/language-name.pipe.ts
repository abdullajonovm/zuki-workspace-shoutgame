import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'languageName'
})
export class LanguageNamePipe implements PipeTransform {
    transform(code: string): any {
        switch (code) {
            case 'en':
                return 'English';
            case 'es':
                return 'Spanish';
            default:
                return 'Unknown';
        }
    }

}
