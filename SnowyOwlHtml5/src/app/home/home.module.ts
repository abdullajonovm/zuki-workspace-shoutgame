import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HomeComponent} from './home.component';
import {RouterModule, Routes} from '@angular/router';
import {AllGamesComponent} from './all-games/all-games.component';
import {MyGamesComponent} from './my-games/my-games.component';
import {MeComponent} from './me/me.component';
import {SharedModule} from 'app/shared/shared.module';
import {EditProfileComponent} from './edit-profile/edit-profile.component';
import {MediaService} from 'app/home/media.service';
import {ChangelogComponent} from './changelog/changelog.component';
import { FilterComponent } from './filter/filter.component';
import {VxDropdownModule} from 'vx-components';

const routes: Routes = [
    {
        path: '', component: HomeComponent, children: [
            {path: '', redirectTo: 'all'},
            {path: 'all'},
            {path: 'my'},
            {path: 'me'}
        ]
    },
    {path: 'me/edit', component: EditProfileComponent}
];

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        SharedModule,
        VxDropdownModule
    ],
    providers: [MediaService],
    entryComponents: [
        ChangelogComponent
    ],
    declarations: [HomeComponent, AllGamesComponent, MyGamesComponent, MeComponent,
        EditProfileComponent, ChangelogComponent, FilterComponent]
})
export class HomeModule {
}
