import { Pipe, PipeTransform } from '@angular/core';
import { ColumnComponent } from './column.component';
import {DataTableUtil} from 'app/data-table/data-table-util';

@Pipe({
    name: 'sort'
})
export class SortPipe implements PipeTransform {

    transform<T>(items: T[], sortDirection: 'up' | 'down', sortColumn?: ColumnComponent<T>): T[] {
        if (!sortColumn || !items) {
            return items;
        }
        const result: T[] = [];
        let isString: boolean | null = null;

        items.sort((a: any, b: any) => {
            a = getSortItem(a, sortColumn.field);
            b = getSortItem(b, sortColumn.field);

            if (isString || DataTableUtil.isString(a)) {
                isString = true;
                a = a ? (a + '').toLowerCase() : a;
                b = b ? (b + '').toLowerCase() : b;
            } else if (isString === null && DataTableUtil.isDefined(a)) {
                isString = DataTableUtil.isString(a);
            } else {
                // Either it wasn't a string or it was undefined
            }


            const bigger = a > b;
            if (sortDirection === 'up') {
                return bigger ? 1 : -1;
            } else {
                return bigger ? -1 : 1;
            }
        }).map(item => result.push(item));

        return result;
    }

}

function getSortItem(item: any, sortKey: string): any {
    sortKey.split('.').forEach(key => item = item[key]);

    return item;
}
