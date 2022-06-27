import {ApplicationRef, ComponentFactoryResolver, Injectable, Injector, Type} from '@angular/core';
import {BaseDialogOptions, DialogBodyOptions, DialogComponent, OnDialogOpen} from 'app/shared/dialog/dialog.component';

@Injectable()
export class DialogService {
    loadingIndicator?: DialogComponent;

    constructor(private resolver: ComponentFactoryResolver, private injector: Injector, private appRef: ApplicationRef) {
    }

    open(options: DialogBodyOptions): DialogComponent;
    open(component: Type<OnDialogOpen<undefined> | NotOnDialogOpen>, options?: BaseDialogOptions): DialogComponent;
    open<T, K extends T>(component: Type<OnDialogOpen<T>>, options: BaseDialogOptions | null, data: K): DialogComponent;
    open(componentOrOptions: any, optionsOrData?: any, data?: any): DialogComponent {
        const container = createOverlay();

        const factory = this.resolver.resolveComponentFactory(DialogComponent);
        const dialog = factory.create(this.injector, undefined, container);
        this.appRef.attachView(dialog.hostView);

        if (typeof componentOrOptions === 'function') {
            // the input is a component
            dialog.instance.setContent(componentOrOptions, optionsOrData, data);
        } else {
            // the input is dialog options
            dialog.instance.setContent(null, componentOrOptions, optionsOrData);
        }

        dialog.instance.setComponentRef(dialog);
        dialog.instance.setContainer(container);

        return dialog.instance;
    }

    showLoadingIndicator(message = 'loading...'): void {
        if (this.loadingIndicator)
            this.closeLoadingIndicator();

        this.loadingIndicator = this.open({
            body: message,
            loading: true,
            disableClose: true
        });
    }

    closeLoadingIndicator(): void {
        if (this.loadingIndicator) {
            this.loadingIndicator.close();
            this.loadingIndicator = undefined;
        }
    }
}

function createOverlay(): HTMLElement {
    const container = document.createElement('div');

    document.body.appendChild(container);
    return container;
}

interface NotOnDialogOpen {
    onDialogOpen?: undefined;
}
