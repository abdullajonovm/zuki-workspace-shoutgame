import { HttpService, ShoutResponse } from 'app/shared/services/http.service';
import { Injectable } from '@angular/core';
import { Category } from 'app/model/category';

import { VxDialog } from 'vx-components';
import {Util} from '../../util';
import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';

@Injectable()
export class CategoryService {

    categories: Category[] = [];
    keyedCategories: {[id: string]: Category} = {};

    constructor(private httpService: HttpService, private dialog: VxDialog) {
        this.loadCategories();
    }

    async loadCategories(): Promise<void> {
        const resp = await this.httpService.sendRequest<LoadIdsToKeysResponse>('/snowladmin/categories/getIdsToKeys').toPromise();
        if (resp.success) {
            const categoryIds = resp.result.map(cat => cat.key);
            const categories = await this.httpService.sendRequest<CategoryResponse>('/snowl/getQuestionCategoriesFromCategoryIds', {categories: categoryIds}).toPromise();

            this.categories = categories.questionCategories.map(cat => new Category(cat));
            this.keyedCategories = Util.keyBy(this.categories, 'id');
        }
    }

    async createCategory(category: Category): Promise<CreateResponse> {
        const resp = await this.httpService.sendRequest<CreateResponse>('/snowladmin/category/create', {category}).toPromise();
        if (!resp.success) {
            this.dialog.open(AlertDialogComponent, {
                title: 'Error',
                body: 'Failed to create category: category already exists.',
                buttons: ['Dismiss']
            });
        }

        return resp;
    }

    async updateCategory(category: Category): Promise<ShoutResponse> {
        const resp = await this.httpService.sendRequest<CreateResponse>('/snowladmin/category/update', {category}).toPromise();
        if (!resp.success) {
            this.dialog.open(AlertDialogComponent, {
                title: 'Error updating category',
                body: JSON.stringify(resp),
                buttons: ['Dismiss']
            });
        }
        return resp;
    }
}

interface LoadIdsToKeysResponse extends ShoutResponse {
    result: {key: string}[];
}
interface CategoryResponse extends ShoutResponse {
    questionCategories: Category[];
}
interface CreateResponse extends ShoutResponse {
    categoryAlreadyExists?: boolean;
    category: Category;
}
