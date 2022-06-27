import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from 'app/app.module';
import {environment} from 'environments/environment';
import {Util} from './app/util';

Util.enableJSONDates();

if (environment.production) {
    enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule);
