import {Pipe, PipeTransform} from '@angular/core';
import {formatRelative} from 'date-fns';

@Pipe({
    name: 'shdate'
})
export class DatePipe implements PipeTransform {

    transform(value: Date, args?: any): any {
        return formatRelative(value, new Date());
    }

}
