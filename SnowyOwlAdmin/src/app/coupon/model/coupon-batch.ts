import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class CouponBatch extends BaseModel<CouponBatch> {
    @autoserialize batchId!: number;
    @autoserialize batchName!: string;
    @autoserialize amount!: number;
    @autoserialize quantity!: number;
    @autoserializeAs(Date) expireDate?: Date;
    @autoserializeAs(Date) createDate!: Date;
}
