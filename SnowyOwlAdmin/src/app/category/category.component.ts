import { Component, OnInit } from '@angular/core';
import { TitleService } from 'app/shared/services/title.service';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import { Category } from '../model/category';
import { CategoryService } from '../shared/services/category.service';

@Component({
    selector: 'app-category',
    templateUrl: './category.component.html',
    styleUrls: ['./category.component.scss']
})
export class CategoryComponent extends VxDialogDef<Category | undefined, boolean> {

    category: Category;
    updating = false;
    constructor(private categoryService: CategoryService, private dialog: VxDialogRef<CategoryComponent>) {
        super();

        dialog.onCancel.subscribe(() => dialog.close(false));

        if (dialog.data) {
            this.category = dialog.data;
            this.updating = true;
        } else {
            this.category = new Category();
        }
    }

    async create(): Promise<void> {
        // TODO: Error handling here?
        if (this.updating) {
            const resp = await this.categoryService.updateCategory(this.category);
            if (resp.success) {
                this.dialog.close(true);
            }
        } else {
            const resp = await this.categoryService.createCategory(this.category);
            if (resp.success) {
                this.dialog.close(true);
            }
        }
    }
}
