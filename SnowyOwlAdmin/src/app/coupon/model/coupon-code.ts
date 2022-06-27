import {BaseModel} from 'app/model/base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class CouponCode extends BaseModel<CouponCode> {
    @autoserialize couponCode!: string;
    @autoserialize batchId!: number;
    @autoserialize amount!: number;
    @autoserializeAs(Date) createDate!: Date;
    @autoserializeAs(Date) expireDate!: Date;
    @autoserialize cancelled?: boolean;
    @autoserializeAs(Date) cancelledDate?: Date;
    @autoserialize redeemedBySubscriberId?: number;
    @autoserializeAs(Date) redeemedDate?: string;
    get status(): string {
        return this.redeemedDate ? 'Redeemed' : (this.cancelled ? 'Cancelled' : 'New');
    }
}
