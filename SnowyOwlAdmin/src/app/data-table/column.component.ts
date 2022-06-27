import {
    ChangeDetectionStrategy,
    Component, ContentChild, EmbeddedViewRef, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, TemplateRef,
    ViewContainerRef
} from '@angular/core';

@Component({
    selector: 'app-column',
    template: '<span></span>',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ColumnComponent<T> {
    @ContentChild(TemplateRef, {static: true}) templateRef!: TemplateRef<ColumnViewData>;
    @Input('header') header = '';
    @Input('field') field!: string;
    @Input() filterTransform?: (item: T) => string | string;
    @Input() defaultValue?: string;
}

@Component({
    selector: 'app-column-body',
    template: '<span></span>'
})
export class ColumnBodyComponent<T> implements OnInit, OnChanges, OnDestroy {

    @Input() column!: ColumnComponent<T>;

    @Input() rowData!: T;
    @Input() selected = false;

    view!: EmbeddedViewRef<ColumnViewData>;

    constructor(public viewContainer: ViewContainerRef) {}

    ngOnInit(): void {
        this.view = this.viewContainer.createEmbeddedView(this.column.templateRef, {
            $implicit: this.rowData,
            selected: this.selected
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!this.view) {
            return;
        }

        if ('selected' in changes) {
            this.view.context.selected = changes['selected'].currentValue;
        }
    }

    ngOnDestroy(): void {
        this.view.destroy();
    }
}

interface ColumnViewData {
    $implicit: any;
    selected: boolean;
}
