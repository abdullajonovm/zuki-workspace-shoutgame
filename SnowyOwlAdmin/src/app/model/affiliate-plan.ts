import { BaseModel } from 'app/model/base-model';
import { autoserialize, autoserializeAs } from 'cerialize';

export class AffiliatePlan extends BaseModel<AffiliatePlan> {
    // All percents are doubles 0.0-1.0
    @autoserialize affiliatePlanId!: number;
    @autoserialize affiliateDirectPayoutPct!: number;
    @autoserialize affiliateSecondaryPayoutPct!: number;
    @autoserialize affiliateTertiaryPayoutPct!: number;
    @autoserialize playerInitialPayoutPct!: number;
    @autoserialize current!: boolean;
}
