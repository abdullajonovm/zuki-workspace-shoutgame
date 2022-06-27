import {BaseModel} from 'app/model/base-model';
import {autoserialize} from 'cerialize';

export class Winner extends BaseModel<Winner> {
    @autoserialize email!: string;
    @autoserialize subscriberId!: number;
    @autoserialize nickname!: string;
    @autoserialize amount!: number;
    @autoserialize gameId!: string;
    @autoserialize rank!: number;
}
