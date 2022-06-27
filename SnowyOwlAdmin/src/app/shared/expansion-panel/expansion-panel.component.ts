import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-expansion-panel',
    templateUrl: 'expansion-panel.component.html',
    styleUrls: ['expansion-panel.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        class: 'card'
    }
})
export class ExpansionPanelComponent {
    @Input() expanded = false;
    @Input() title = '';
}
