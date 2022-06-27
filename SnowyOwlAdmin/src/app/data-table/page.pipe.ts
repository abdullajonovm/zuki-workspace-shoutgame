import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'page'
})
export class PagePipe implements PipeTransform {
    transform(items: any[], enabled: boolean, itemsPerPage: number, currentPage: number): any {
        if (!items || !enabled)
            return items;
        if (typeof currentPage !== 'number') {
            currentPage = 1;
        }
        currentPage--;
        const startIdx = currentPage * itemsPerPage;
        return items.slice(startIdx, startIdx + itemsPerPage);
    }
}

function getFilterItem(item: any, filterKey: string): any {
    filterKey.split('.').forEach(key => item = item[key]);

    return item;
}
