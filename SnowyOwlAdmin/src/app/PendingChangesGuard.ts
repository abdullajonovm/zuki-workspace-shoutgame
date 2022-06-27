import {CanDeactivate} from '@angular/router';
import { Observable } from 'rxjs';
import {environment} from 'environments/environment';

export interface ComponentCanDeactivate {
    canDeactivate(): boolean | Observable<boolean>;
}

export class PendingChangesGuard implements CanDeactivate<ComponentCanDeactivate> {
    canDeactivate(component: ComponentCanDeactivate): boolean | Observable<boolean> {
        if (!environment.production)
            return true;

        return component.canDeactivate();
    }
}
