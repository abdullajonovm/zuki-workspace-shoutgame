import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { PreloadAllModules, Route, RouterModule } from '@angular/router';
import { LoggedInGuard } from 'app/logged-in.guard';
import { SharedModule } from 'app/shared/shared.module';

import { AppComponent } from './app.component';
import { ChangelogComponent } from './changelog/changelog.component';
import { LoginComponent } from './login/login.component';

const routes: Route[] = [
    {path: '', pathMatch: 'full', redirectTo: '/game'},
    {path: 'game', loadChildren: () => import('./game/game.module').then(m => m.GameModule), canActivate: [LoggedInGuard]},
    {path: 'payout-model', loadChildren: () => import('./payout-model/payout-model.module').then(m => m.PayoutModelModule), canActivate: [LoggedInGuard]},
    {path: 'question', loadChildren: () => import('./question/question.module').then(m => m.QuestionModule), canActivate: [LoggedInGuard]},
    {path: 'category', loadChildren: () => import('./category/category.module').then(m => m.CategoryModule), canActivate: [LoggedInGuard]},
    {path: 'coupon', loadChildren: () => import('./coupon/coupon.module').then(m => m.CouponModule), canActivate: [LoggedInGuard]},
    {path: 'subscriber', loadChildren: () => import('./subscriber/subscriber.module').then(m => m.SubscriberModule), canActivate: [LoggedInGuard]},
    {path: 'other', loadChildren: () => import('./other/other.module').then(m => m.OtherModule), canActivate: [LoggedInGuard]},
    {path: 'payouts', loadChildren: () => import('./payouts/payouts.module').then(m => m.PayoutsModule), canActivate: [LoggedInGuard]},
    {path: 'affiliate-plan', loadChildren: () => import('./affiliate-plan/affiliate-plan.module').then(m => m.AffiliatePlanModule), canActivate: [LoggedInGuard]},
    {path: 'ineligible-subs', loadChildren: () => import('./ineligible-subs/ineligible-subs.module').then(m => m.IneligibleSubsModule), canActivate: [LoggedInGuard]},
    {path: 'login', component: LoginComponent},
    {path: '**', redirectTo: '/game'}
];


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        ChangelogComponent
    ],
    imports: [
        BrowserModule,
        SharedModule.forRoot(),
        HttpClientModule,
        RouterModule.forRoot(routes, {preloadingStrategy: PreloadAllModules})
    ],
    entryComponents: [ChangelogComponent],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
