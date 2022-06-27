import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {PreloadAllModules, Route, RouterModule} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {HttpClientJsonpModule, HttpClientModule} from '@angular/common/http';
import {SharedModule} from 'app/shared/shared.module';
import {GlobalErrorHandler} from 'app/global-error-handler';
import {TermsAndConditionsComponent} from 'app/legal/terms-and-conditions.component';
import {PrivacyComponent} from 'app/legal/privacy.component';

const routes: Route[] = [
    {path: '', pathMatch: 'full', redirectTo: '/home/all'},
    {path: 'login', component: LoginComponent},
    {path: 'termsAndConditions', component: TermsAndConditionsComponent},
    {path: 'privacy', component: PrivacyComponent},
    {path: 'home', loadChildren: './home/home.module#HomeModule'},
    {path: 'game', loadChildren: './game/game.module#GameModule'},
    {path: '**', redirectTo: '/home/all'}
];

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        TermsAndConditionsComponent,
        PrivacyComponent
    ],
    imports: [
        BrowserModule,
        RouterModule.forRoot(routes, {preloadingStrategy: PreloadAllModules}),
        HttpClientModule,
        HttpClientJsonpModule,
        SharedModule.forRoot()
    ],
    providers: [{
        provide: ErrorHandler,
        useClass: GlobalErrorHandler
    }],
    bootstrap: [AppComponent]
})
export class AppModule {
}
