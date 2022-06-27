import { Component, OnInit } from '@angular/core';
import { VxDialogDef, VxDialogRef } from 'vx-components';

@Component({
  selector: 'app-alert-dialog',
  templateUrl: './alert-dialog.component.html',
  styleUrls: ['./alert-dialog.component.scss']
})
export class AlertDialogComponent extends VxDialogDef<AlertDialogData, string> {
    data: AlertDialogData;
    constructor(public dialog: VxDialogRef<AlertDialogData>) {
        super();
        this.data = dialog.data;
    }
}

export interface AlertDialogData {
    title: string,
    body: string,
    buttons: string[]
}

