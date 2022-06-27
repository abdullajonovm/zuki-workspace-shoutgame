import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {GameComponent} from './game.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from 'app/shared/shared.module';
import {RoundComponent} from './round/round.component';
import {RoundListComponent} from './round-list/round-list.component';
import {RoundItemComponent} from 'app/game/round-list/round-item.component';
import {WaitDialogComponent} from './wait-dialog/wait-dialog.component';
import {StatusBarComponent} from './status-bar/status-bar.component';
import {GameGuideDialogComponent} from './game-guide-dialog.component';
import {GameResultsComponent} from './game-results/game-results.component';

const routes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: '/home/all'},
    {path: ':id', component: GameComponent}
];

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        SharedModule
    ],
    declarations: [GameComponent, RoundComponent, RoundListComponent, RoundItemComponent,
        WaitDialogComponent, StatusBarComponent, GameGuideDialogComponent, GameResultsComponent],
    providers: [],
    entryComponents: [RoundComponent, WaitDialogComponent, GameGuideDialogComponent, GameResultsComponent]
})
export class GameModule {
}
