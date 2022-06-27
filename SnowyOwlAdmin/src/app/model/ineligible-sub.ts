import {BaseModel} from './base-model';
import {autoserialize, autoserializeAs} from 'cerialize';

export class IneligibleSub extends BaseModel<IneligibleSub> {
    @autoserialize isId!: number;
    @autoserialize subscriberId!: number;
    @autoserialize email!: string;
    @autoserialize linkedSubscriberId?: number;
    @autoserialize linkedEmail?: string;
    @autoserialize reason!: 'EMPLOYEE' | 'IMMEDIATE_FAMILY_MEMBER';
    @autoserializeAs(Date) createDate!: Date;
}
