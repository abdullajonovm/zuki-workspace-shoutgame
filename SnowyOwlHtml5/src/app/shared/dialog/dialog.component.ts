import {AfterViewInit, Component, ComponentFactoryResolver, ComponentRef, ElementRef, OnDestroy, Type, ViewChild, ViewContainerRef} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {environment} from '../../../environments/environment';

const STRINGS = environment.strings.core;
const CLOSE_ANIMATION_TIME = 300;

@Component({
    selector: 'sh-dialog',
    templateUrl: './dialog.component.html',
    styleUrls: ['./dialog.component.scss']
})
export class DialogComponent implements OnDestroy, AfterViewInit {
    @ViewChild('content', {read: ViewContainerRef}) content: ViewContainerRef;
    onClose = new Subject<any>();
    closing = false;
    closed = false;
    dialogOptions: DialogOptions = {body: ''};
    hasComponent = false;
    @ViewChild('buttons') buttons: ElementRef;

    private componentRef: ComponentRef<this>;
    private contentComponentRef?: ComponentRef<any>;
    private container: HTMLElement;

    constructor(private resolver: ComponentFactoryResolver) {
    }

    setContent(content: Type<any> | null, options?: DialogOptions | null, data?: any): void {
        this.dialogOptions = options || {body: ''};
        // if (!this.dialogOptions.buttons || !this.dialogOptions.buttons.length)
        //     this.dialogOptions.buttons = [{text: STRINGS.ok}];

        if (content) {
            this.hasComponent = true;
            const factory = this.resolver.resolveComponentFactory(content);
            const created = this.content.createComponent(factory);
            const instance = created.instance;
            if (instance.onDialogOpen) {
                instance.onDialogOpen(this, data);
            }
            this.contentComponentRef = created;
        }
    }

    ngAfterViewInit(): void {
        if (this.buttons) {
            const buttons = this.buttons.nativeElement as HTMLDivElement;
            if (buttons.children && buttons.children.length) {
                (buttons.children[0] as HTMLButtonElement).focus();
            }
        }
    }

    setComponentRef(componentRef: ComponentRef<this>): void {
        this.componentRef = componentRef;
    }

    close(closeData?: any): void {
        this.onClose.next(closeData);
        this.closing = true;

        setTimeout(() => {
            this.closed = true;
            this.componentRef.destroy();
        }, CLOSE_ANIMATION_TIME);
    }

    setContainer(container: HTMLElement): void {
        this.container = container;
    }

    _overlayClick(): void {
        if (this.dialogOptions && this.dialogOptions.disableClose) {
            return;
        }
        this.close();
    }

    ngOnDestroy(): void {
        if (this.contentComponentRef)
            this.contentComponentRef.destroy();
        if (this.container)
            this.container.remove();
        this.onClose.complete();
    }
}

interface DialogButton {
    text: string;
    value?: any;
}

type DialogOptions = DialogBodyOptions & BaseDialogOptions;

export interface BaseDialogOptions {
    disableClose?: boolean;
    width?: string;
    height?: string;
}

export interface DialogBodyOptions extends BaseDialogOptions {
    body: string;
    title?: string;
    buttons?: DialogButton[];
    loading?: boolean;
}

export interface OnDialogOpen<T = undefined> {
    onDialogOpen(dialog: DialogComponent, data: T): void;
}
