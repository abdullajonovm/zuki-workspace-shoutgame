import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { AlertDialogComponent } from '../shared/alert-dialog/alert-dialog.component';
import {TitleService} from '../shared/services/title.service';
import {AffiliatePlan} from '../model/affiliate-plan';
import { VxDialog, VxDialogRef, VxToast } from 'vx-components';
import {AffiliateService} from '../shared/services/affiliate.service';
import {LoadingDialogComponent} from '../shared/loading-dialog.component';

@Component({
    templateUrl: 'affiliate-plan.component.html',
    styleUrls: ['affiliate-plan.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class AffiliatePlanComponent implements OnInit {
    affiliatePlan = new AffiliatePlan();
    loading = true;

    @ViewChild('dialogTemplate', { static: true }) dialogTemplate!: TemplateRef<any>;
    @ViewChild('errorTemplate', { static: true }) errorTemplate!: TemplateRef<any>;

    get isSame(): boolean {
        return !!this.savedPlan
            && this.savedPlan.affiliateTertiaryPayoutPct === this.affiliatePlan.affiliateTertiaryPayoutPct
            && this.savedPlan.affiliateSecondaryPayoutPct === this.affiliatePlan.affiliateSecondaryPayoutPct
            && this.savedPlan.affiliateDirectPayoutPct === this.affiliatePlan.affiliateDirectPayoutPct
            && this.savedPlan.playerInitialPayoutPct === this.affiliatePlan.playerInitialPayoutPct;

    }

    private dialogInst?: VxDialogRef;
    private savedPlan?: AffiliatePlan;

    constructor(private titleService: TitleService, private dialog: VxDialog,
                private affiliateService: AffiliateService, private cdr: ChangeDetectorRef,
                private toast: VxToast) {
    }

    ngOnInit(): void {
        this.titleService.title = 'Affiliate Plan';
        this.affiliateService.getCurrentAffiliatePlan().subscribe(plan => {
            plan.affiliateDirectPayoutPct *= 100;
            plan.affiliateSecondaryPayoutPct *= 100;
            plan.affiliateTertiaryPayoutPct *= 100;
            plan.playerInitialPayoutPct *= 100;
            this.affiliatePlan = plan;
            this.savedPlan = new AffiliatePlan(plan);

            this.loading = false;
            this.cdr.markForCheck();
        });
    }

    savePlan(): void {
        const secondaryPlusPlayer = this.affiliatePlan.affiliateSecondaryPayoutPct + this.affiliatePlan.playerInitialPayoutPct;
        const tertiaryPlusPlayer = this.affiliatePlan.affiliateTertiaryPayoutPct + this.affiliatePlan.playerInitialPayoutPct;
        if (secondaryPlusPlayer > 100 || tertiaryPlusPlayer > 100) {
            this.dialogInst = this.dialog.open(this.errorTemplate, undefined);
        } else {
            this.dialogInst = this.dialog.open(this.dialogTemplate, undefined);
        }
    }

    confirm(): void {
        if (this.dialogInst) {
            this.dialogInst.close();
        }

        const loader = this.dialog.open(LoadingDialogComponent,  'Setting Affiliate Plan...');
        this.affiliateService.setAffiliatePlan(new AffiliatePlan({
            affiliateDirectPayoutPct: this.affiliatePlan.affiliateDirectPayoutPct / 100,
            affiliateSecondaryPayoutPct: this.affiliatePlan.affiliateSecondaryPayoutPct / 100,
            affiliateTertiaryPayoutPct: this.affiliatePlan.affiliateTertiaryPayoutPct / 100,
            playerInitialPayoutPct: this.affiliatePlan.playerInitialPayoutPct / 100
        })).subscribe((resp) => {
            loader.close();

            if (resp.success) {
                this.toast.open({
                    title: 'Success!',
                    text: 'Successfully Set Affiliate Plan',
                    type: 'success'
                });
            } else {
                this.dialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: 'An Unexpected Error Has Occurred: ' + JSON.stringify(resp),
                    buttons: ['Dismiss']
                })
            }

            this.savedPlan = new AffiliatePlan(this.affiliatePlan);
        });
    }

    cancel(): void {
        if (this.dialogInst) {
            this.dialogInst.close();
        }
    }
}

