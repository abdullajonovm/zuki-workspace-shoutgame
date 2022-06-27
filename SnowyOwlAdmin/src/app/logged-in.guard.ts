import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { SubscriberService } from 'app/shared/services/subscriber.service';

@Injectable()
export class LoggedInGuard implements CanActivate {
    constructor(private subscriberService: SubscriberService, private router: Router) {
    }

    canActivate(): boolean {
        const result = this.subscriberService.isLoggedIn();

        if (!result)
            this.router.navigate(['/login']);

        return true;
    }
}
