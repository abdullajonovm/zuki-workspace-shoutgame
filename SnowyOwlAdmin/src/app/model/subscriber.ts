import {autoserialize, autoserializeAs} from 'cerialize';
import { BaseModel } from 'app/model/base-model';
export class Subscriber extends BaseModel<Subscriber> {
    @autoserialize subscriberId!: number;
    @autoserialize encryptKey!: string;
    @autoserialize sha256Hash!: string;
    @autoserialize nickname!: string;
    @autoserialize photoUrl!: string;
    @autoserializeAs('firstname') firstName!: string;
    @autoserializeAs('lastname') lastName!: string;
    @autoserialize email!: string;
    @autoserialize role!: string;
}
