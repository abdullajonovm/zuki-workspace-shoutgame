import { NgModule } from '@angular/core';
import { SortPipe } from 'app/data-table/sort.pipe';
import { DataTableComponent } from './data-table.component';
import { PagePipe } from 'app/data-table/page.pipe';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {VxFormFieldModule, VxSpinnerModule} from 'vx-components';
import { ColumnBodyComponent, ColumnComponent } from 'app/data-table/column.component';

@NgModule({
    declarations: [DataTableComponent, SortPipe, PagePipe, ColumnComponent, ColumnBodyComponent],
    imports: [CommonModule, FormsModule, VxFormFieldModule, VxSpinnerModule],
    exports: [DataTableComponent, ColumnComponent]
})
export class DataTableModule {
}
