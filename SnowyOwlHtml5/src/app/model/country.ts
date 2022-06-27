import {BaseModel} from './base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class Country extends BaseModel<Country> {
    @autoserializeAs(Object) countryNames: any;
    @autoserialize countryCode: string;
    @autoserialize dialCode: number;
    @autoserialize sortOrder: number;
}
