import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs } from 'cerialize';

export class Category extends BaseModel<Category> {
    @autoserialize categoryKey!: string;
    @autoserialize id!: string;
    @autoserializeAs(Object) categoryName!: any;

    protected initialize(): void {
        this.categoryName = {};
    }
}
