import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CloneGameDialogComponent } from './clone-game-dialog.component';

describe('CloneGameDialogComponent', () => {
    let component: CloneGameDialogComponent;
    let fixture: ComponentFixture<CloneGameDialogComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [CloneGameDialogComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(CloneGameDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should be created', () => {
        expect(component).toBeTruthy();
    });
});
