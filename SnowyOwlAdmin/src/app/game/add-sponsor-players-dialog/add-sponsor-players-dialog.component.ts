import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { createNumberMask } from 'text-mask-addons/dist/textMaskAddons';
import { VxDialog, VxDialogDef, VxDialogRef, VxToast } from 'vx-components';
import { Subscriber } from '../../model/subscriber';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import { LoadingDialogComponent } from '../../shared/loading-dialog.component';
import { SponsorService } from '../../shared/services/sponsor.service';
import { SubscriberService } from '../../shared/services/subscriber.service';
import { Util } from '../../util';

@Component({
    selector: 'app-add-sponsor-players-dialog',
    templateUrl: 'add-sponsor-players-dialog.component.html',
    styleUrls: ['add-sponsor-players-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class AddSponsorPlayersDialogComponent extends VxDialogDef<string> implements OnInit {
    numberMask = createNumberMask({prefix: '', allowDecimal: false});

    allSponsors: Subscriber[] = [];
    loading = false;

    numPlayers = '';
    sponsorEmail = '';

    private gameId: string;
    constructor(private sponsorService: SponsorService, private subscriberService: SubscriberService,
                private vxDialog: VxDialog, private cdr: ChangeDetectorRef,
                public dialog: VxDialogRef<AddSponsorPlayersDialogComponent>,
                private toast: VxToast) {
        super();
        this.gameId = dialog.data;
    }

    ngOnInit(): void {
        this.search();
    }

    addPlayers(): void {
        const loader = this.vxDialog.open(LoadingDialogComponent,  'Adding Sponsor Players...');

        this.sponsorService.addSponsorPlayersToGame(this.gameId, Util.parseNumber(this.numPlayers), this.sponsorEmail).subscribe(resp => {
            loader.close();
            if (!resp.success) {
                if (resp.notEnoughSponsorPoolCash) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error',
                        body: `Not enough money in the sponsor's pool.`,
                        buttons: ['Dismiss']
                    })
                } else if (resp.invalidParam || resp.notASponsor) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error',
                        body: resp.invalidParam ? `Invalid sponsor email address.` : `The provided email does not belong to a sponsor.`,
                        buttons: ['Dismiss']
                    })
                } else {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error',
                        body: `An unexpected error occurred: ${JSON.stringify(resp)}`,
                        buttons: ['Dismiss']
                    })
                }
            } else {
                this.dialog.close();
                this.toast.open({
                    type: 'success',
                    title: 'Success!',
                    text: `Successfully added subscriber players!`
                });
            }
        })
    }

    search(): void {
        this.loading = true;
        this.cdr.markForCheck();
        this.subscriberService.searchForSubscribers({role: 'SPONSOR'}).subscribe(sponsors => {
            this.loading = false;
            this.allSponsors = sponsors;
            this.cdr.markForCheck();
        })
    }
}
