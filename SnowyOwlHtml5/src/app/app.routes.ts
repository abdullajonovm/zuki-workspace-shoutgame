import {LoginComponent} from './login/login.component';
import {TermsAndConditionsComponent} from './legal/terms-and-conditions.component';
import {PrivacyComponent} from './legal/privacy.component';
import {Route} from '@angular/router';

export const routes: Route[] = [
    {path: '', pathMatch: 'full', redirectTo: '/home/all'},
    {path: 'login', component: LoginComponent},
    {path: 'termsAndConditions', component: TermsAndConditionsComponent},
    {path: 'privacy', component: PrivacyComponent},
    {path: 'home', loadChildren: './home/home.module#HomeModule'},
    {path: 'game', loadChildren: './game/game.module#GameModule'},
    {path: '**', redirectTo: '/home/all'}
];
