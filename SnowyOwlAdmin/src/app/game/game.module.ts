import { NgModule } from '@angular/core';
import { FindGameComponent } from 'app/game/games/games.component';
import { GameComponent } from 'app/game/game/game.component';
import { CloneGameDialogComponent } from 'app/game/clone-game-dialog/clone-game-dialog.component';
import { Route, RouterModule } from '@angular/router';
import { GameService } from 'app/game/game.service';
import { SharedModule } from 'app/shared/shared.module';
import {StartBracketPlayDialogComponent} from 'app/game/start-bracket-play-dialog.component';
import { AddSponsorPlayersDialogComponent } from './add-sponsor-players-dialog/add-sponsor-players-dialog.component';
import { WinnersDialogComponent } from './winners-dialog/winners-dialog.component';
import {CopyToClipboardComponent} from './copy-to-clipboard/copy-to-clipboard.component';
import { GameGuideComponent } from './game-guide/game-guide.component';
import { RoundsFormComponent } from './rounds-form/rounds-form.component';
import { PlayerInfoDialogComponent } from './player-info-dialog/player-info-dialog.component';

const routes: Route[] = [
    {path: '', pathMatch: 'full', component: FindGameComponent},
    {path: 'view/:id', component: GameComponent},
    {path: 'create', component: GameComponent},
    {path: 'clone/:cloneId', component: GameComponent}
];

@NgModule({
    declarations: [FindGameComponent, GameComponent, CloneGameDialogComponent,
        StartBracketPlayDialogComponent, WinnersDialogComponent, CopyToClipboardComponent,
        GameGuideComponent, RoundsFormComponent, PlayerInfoDialogComponent, AddSponsorPlayersDialogComponent],
    imports: [SharedModule, RouterModule.forChild(routes)],
    providers: [GameService],
    entryComponents: [
        CloneGameDialogComponent, StartBracketPlayDialogComponent, WinnersDialogComponent,
        CopyToClipboardComponent, PlayerInfoDialogComponent, AddSponsorPlayersDialogComponent
    ]
})
export class GameModule {
}
