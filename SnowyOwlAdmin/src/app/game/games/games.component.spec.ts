import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FindGameComponent } from './games.component';

describe('FindGameComponent', () => {
    let component: FindGameComponent;
    let fixture: ComponentFixture<FindGameComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [FindGameComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(FindGameComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should be created', () => {
        expect(component).toBeTruthy();
    });
});
