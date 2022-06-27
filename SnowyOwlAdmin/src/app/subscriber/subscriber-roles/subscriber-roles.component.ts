import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import { VxDialog, VxDialogDef, VxDialogRef, VxToast } from 'vx-components';
import {Subscriber} from '../../model/subscriber';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {LoadingDialogComponent} from '../../shared/loading-dialog.component';
import {SubscriberService} from '../../shared/services/subscriber.service';
import {zip} from 'rxjs';
import {ShoutResponse} from 'app/shared/services/http.service';

@Component({
    selector: 'app-subscriber-roles',
    templateUrl: './subscriber-roles.component.html',
    styleUrls: ['./subscriber-roles.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SubscriberRolesComponent extends VxDialogDef<Subscriber> {
    subscriber: Subscriber;
    loading = true;

    allRoles: string[] = [];
    roles: string[] = [];

    private savedRoles: string[] = [];

    constructor(private vxDialog: VxDialog, private subscriberService: SubscriberService,
                private cdr: ChangeDetectorRef, public dialog: VxDialogRef<SubscriberRolesComponent>,
                private toast: VxToast) {
        super();
        this.subscriber = dialog.data;

        const roles$ = this.subscriberService.getRoles();
        const subRoles$ = this.subscriberService.getSubscriberRoles(this.subscriber.email);

        zip(roles$, subRoles$).subscribe(([roles, subRoles]) => {
            this.allRoles = roles;
            this.savedRoles = subRoles.slice(0);
            this.roles = subRoles;
            this.loading = false;
            this.cdr.markForCheck();
        })
    }

    save(): void {
        const rolesToRemove = this.savedRoles.filter(role => this.roles.indexOf(role) === -1);
        const rolesToAdd = this.roles.filter(role => this.savedRoles.indexOf(role) === -1);

        if (rolesToRemove.length || rolesToAdd.length) {
            this.vxDialog.open(AlertDialogComponent, {
                title: 'Confirm',
                body: `Are you sure you want to do this for subscriber: ${this.subscriber.nickname}?` +
                    (rolesToAdd.length ? ` Adding Roles: ${rolesToAdd.join()}` : '') +
                    (rolesToRemove.length ? ` Removing Roles: ${rolesToRemove.join()}` : ''),
                buttons: ['Confirm', 'Cancel']
            }).onClose.subscribe(resp => {
                if (resp === 'Confirm') {
                    this.saveRoles(rolesToAdd, rolesToRemove);
                }
            })

        } else {
            this.dialog.close();
        }
    }

    private saveRoles(toAdd: string[], toRemove: string[]): void {
        const loader = this.vxDialog.open(LoadingDialogComponent,  'Saving Roles...');
        zip(
            ...toAdd.map((role) => this.subscriberService.addRole(this.subscriber.email, role)),
            ...toRemove.map((role) => this.subscriberService.removeRole(this.subscriber.email, role))
        ).subscribe((responses: ShoutResponse[]) => {
            loader.close();
            for (const response of responses) {
                if (!response.success) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error!',
                        body: 'An unexpected error occurred: ' + JSON.stringify(response, null, 2),
                        buttons: ['Dismiss']
                    });
                    return;
                }
            }

            this.dialog.close();
            this.toast.open({
                type: 'success',
                title: 'Success!',
                text: `Successfully updated roles for subscriber: ${this.subscriber.nickname}`
            });
        });
    }
}
