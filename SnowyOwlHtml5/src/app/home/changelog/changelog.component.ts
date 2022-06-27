import {Component, OnInit} from '@angular/core';
import {DialogComponent, OnDialogOpen} from '../../shared/dialog/dialog.component';
import {lastSeenVersionLS} from '../../constants';
import {APP_VERSION} from '../../../environments/version';

@Component({
    selector: 'sh-changelog',
    templateUrl: './changelog.component.html',
    styleUrls: ['./changelog.component.scss']
})
export class ChangelogComponent implements OnInit, OnDialogOpen {
    dialog: DialogComponent;

    versions: Version[] = [{
        number: 179,
        changes: [
            'Added "A Shout® company" to bottom of login and signup pages.'
        ]
    }, {
        number: 178,
        changes: [
            'Fixed signup'
        ]
    }, {
        number: 177,
        changes: [
            'Added a way to filter games by categories'
        ]
    }, {
        number: 176,
        changes: [
            'Added the new logo and updated colors',
            'Fixed weird animation with rounds on the game screen.',
            'On the game screen tapping on your money takes you to your wallet.',
            'You can now successfully withdraw to your bank.'
        ]
    }, {
      number: 175,
      changes: [
          'No longer forces you to be logged in until you attempt to play a game.',
          'Gave login and signup their correct autocomplete attributes',
          'Game images no longer flash the incorrect image',
          'Added coupon code redemption.'
      ]
    }, {
        number: 174,
        changes: [
            'EChecks are now fully functional!',
            'Fixed the weird spinner behavior'
        ]
    }];

    constructor() {
    }

    ngOnInit(): void {
    }

    onDialogOpen(dialog: DialogComponent): void {
        this.dialog = dialog;
        localStorage.setItem(lastSeenVersionLS, APP_VERSION + '');
    }

}

interface Version {
    number: number;
    changes: string[];
}
