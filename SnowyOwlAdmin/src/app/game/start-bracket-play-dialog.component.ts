import {Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import { VxDialogDef, VxDialogRef } from 'vx-components';

@Component({
    template: `
        <h1>Start Bracket Play</h1>
        <form (submit)="start(form)" #form="ngForm">
            <vx-form-field>
                <input vxFormField type="number" label="Start in how many minutes?" [(ngModel)]="startIn" min="0"
                       name="startIn">
            </vx-form-field>
            <div>
                <button vx-button type="button" (click)="dialog.close(null)">Cancel</button>
                <button vx-button type="submit">Start</button>
            </div>
        </form>`,
    styles: [`
        h1 {
            margin-top: 0;
        }
        vx-form-field {
            display: block;
        }
        button {
            float: right;
            margin-left: 10px;
            margin-top: 10px;
        }
    `]
})
export class StartBracketPlayDialogComponent extends VxDialogDef<undefined, number | null> {
    startIn = 1;

    constructor(public dialog: VxDialogRef<StartBracketPlayDialogComponent>) {
        super()
    }

    start(form: NgForm): void {
        if (form.valid) {
            this.dialog.close(this.startIn);
        }
    }
}
