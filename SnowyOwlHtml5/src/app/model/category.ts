import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class Category extends BaseModel<Category> {
    @autoserialize id: string;
    @autoserialize categoryKey: string;
    @autoserializeAs(Object) categoryName: any;
}
