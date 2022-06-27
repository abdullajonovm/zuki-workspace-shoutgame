import { Component, OnInit, ViewChild } from '@angular/core';
import { CategoryService } from '../../shared/services/category.service';
import { Category } from '../../model/category';
import { VxDialog } from 'vx-components';
import { CategoryComponent } from '../category.component';
import { TitleService } from '../../shared/services/title.service';
import { DataTableComponent } from '../../data-table/data-table.component';
import { Data } from '@angular/router';

@Component({
    selector: 'app-find-category',
    templateUrl: './categories.component.html',
    styleUrls: ['./categories.component.scss']
})
export class FindCategoryComponent implements OnInit {

    selectedCategory?: Category;
    @ViewChild(DataTableComponent, { static: true }) dataTable!: DataTableComponent<Category>;
    loading = true;

    constructor(public categoryService: CategoryService, private dialog: VxDialog, private titleService: TitleService) {
    }

    ngOnInit(): void {
        this.titleService.title = 'Categories';
        this.categoryService.loadCategories().then(() => this.loading = false);
    }

    createOrUpdate(category?: Category): void {
        const dialog = this.dialog.open(CategoryComponent, category!);
        dialog.onClose.subscribe((refresh: boolean) => {
            if (refresh) {
                this.loading = true;
                this.categoryService.loadCategories().then(() => this.loading = false);
            }
        })
    }

}
