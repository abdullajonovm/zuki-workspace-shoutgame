import {
    ChangeDetectionStrategy, ChangeDetectorRef, Component, ContentChildren, EventEmitter, Input, Output,
    QueryList
} from '@angular/core';
import { ColumnComponent } from 'app/data-table/column.component';

@Component({
    selector: 'app-data-table',
    templateUrl: './data-table.component.html',
    styleUrls: ['./data-table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataTableComponent<T> {
    @ContentChildren(ColumnComponent) columns!: QueryList<ColumnComponent<T>>;

    @Input() pageable = false;
    @Input()
    get items(): T[] {
        return this._items;
    }
    set items(value: T[]) {
        this._items = value;
        this.reloadFilteredItems();
    }

    @Input() selectedItem: any;
    @Input() itemsPerPage = 10;
    @Input() loading = false;
    @Input() selectable = true;

    @Input() set currentPage(value: number) {
        if (value > this.maxPage)
            this._currentPage = this.maxPage;
        else if (value < 1 || !value)
            this._currentPage = 1;
        else
            this._currentPage = value;
    }

    get currentPage(): number {
        return this._currentPage;
    }

    @Input() sortColumn?: ColumnComponent<T>;
    @Input() sortDirection: SortDirection = 'up';

    @Input() filterable = true;

    @Output() readonly rowDoubleClick = new EventEmitter();
    @Output() readonly selectedItemChange = new EventEmitter<T>();

    filteredItems: T[] = [];
    getValueForField = getValueForField;

    get filterKeys(): FilterTransform[] {
        return this.columns.map(column => {
            return {filterTransform: column.filterTransform, sortKey: column.field, defaultValue: column.defaultValue};
        });
    }

    get maxPage(): number {
        return Math.ceil(this.filteredItems.length / this.itemsPerPage);
    }

    get filterStr(): string {
        return this._filterStr;
    }

    set filterStr(value: string) {
        this.currentPage = 1;
        this._filterStr = value;
        this.reloadFilteredItems();
    }

    private currentTappedItem?: T;
    private _currentPage = 1;
    private _filterStr = '';
    private _items: T[] = [];

    constructor(private changeDetector: ChangeDetectorRef) {}

    markAsChanged(): void {
        this.changeDetector.markForCheck();
    }

    handleRowClick(item: T): void {
        if (this.currentTappedItem === item) {
            this.rowDoubleClick.next(item);
            return;
        }
        if (this.selectable) {
            this.selectedItem = item;
            this.selectedItemChange.emit(item);
        }
        this.currentTappedItem = item;

        setTimeout(() => {
            this.currentTappedItem = undefined;
        }, 300);
    }

    toggleSortColumn(column: ColumnComponent<T>): void {
        if (column === this.sortColumn) {
            if (this.sortDirection === 'up')
                this.sortDirection = 'down';
            else
                this.sortColumn = undefined;
        } else if (column.field) {
            this.sortColumn = column;
            this.sortDirection = 'up';
        } else {
            this.sortColumn = undefined;
        }
    }

    private reloadFilteredItems(): void {
        if (!this._items) {
            this.filteredItems =  [];
            return;
        }

        if (!this._filterStr || !this._filterStr.length) {
            this.filteredItems = this._items;
            return;
        }

        const filterStr = this._filterStr.toUpperCase();
        const filterKeys = this.filterKeys;

        this.filteredItems = this._items.filter((item) => {
            let searchStr = '';
            filterKeys.forEach(key => {
                if (key.sortKey) {
                    const it = getValueForField(item, key.sortKey, key.defaultValue);
                    let str: string = it;
                    if (key.filterTransform) {
                        str = key.filterTransform(it);
                    }
                    searchStr += str;
                }
            });
            searchStr = searchStr.toUpperCase();

            return searchStr.indexOf(filterStr) !== -1;
        })
    }
}
type SortDirection = 'up' | 'down';


function getValueForField(item: any, field: string, defaultVal = ''): any {
    try {
        field.split('.').forEach(key => item = item[key]);
    } catch (e) {
        item = defaultVal;
    }

    return item || defaultVal;
}

interface FilterTransform {
    sortKey: string;
    filterTransform?(item: any): string;
    defaultValue?: string;
}
