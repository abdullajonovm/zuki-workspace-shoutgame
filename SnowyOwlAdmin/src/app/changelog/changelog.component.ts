import {Component, OnInit} from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';
import {lastSeenVersionLS} from '../constants';
import {APP_VERSION} from '../../environments/version';

@Component({
    templateUrl: './changelog.component.html',
    styles: ['button {float: right} h4 {margin-bottom: 0}']
})
export class ChangelogComponent extends VxDialogDef implements OnInit {

    versions: Version[] = [{
        number: 156,
        changes: [
            'Added The new payout model system'
        ]
    }, {
       number: 154,
       changes: [
           'Fixed the copy to clipboard',
           'Now redirects from millionize.net to millionize.com'
       ]
    }, {
        number: 153,
        changes: [
            'Added the ability to clone Questions',
            'Fixed the generate payout table button.'
        ]
    }, {
        number: 152,
        changes: [
            'Updated the color scheme to match the play app',
            'Added categories to the Questions page',
            'Fixed spinners across the app',
            'Added the ability to toggle locking people out of app until login.'
        ]
    }, {
        number: 151,
        changes: [
            'You can now generate a payout table for more than 1,000 players.'
        ]
    }, {
        number: 150,
        changes: [
            'Changed the clone game button to always take you to the advanced clone.',
            'Changed all of the Create buttons to use the word Submit.',
            'Questions now default with the language of english',
            'Questions now default to 3 answers',
            'Fixed a bug with large http requests failing'
        ]
    }, {
        number: 149,
        changes: ['Added a way to view the payout table.']
    }];

    constructor(private dialog: VxDialogRef<ChangelogComponent>) {
        super()
    }

    ngOnInit(): void {
        localStorage.setItem(lastSeenVersionLS, APP_VERSION + '');
    }


}

interface Version {
    number: number;
    changes: string[];
}
