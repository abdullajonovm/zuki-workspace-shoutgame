import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'phone'
})
export class PhonePipe implements PipeTransform {
    transform(value = ''): string {
        const first = value.slice(0, 3);
        const second = value.slice(3, 6);
        const third = value.slice(6, 10);
        return `(${first}) ${second}-${third}`;
    }
}
