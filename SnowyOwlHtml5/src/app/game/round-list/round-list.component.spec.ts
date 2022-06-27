import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RoundListComponent} from './round-list.component';

describe('RoundListComponent', () => {
    let component: RoundListComponent;
    let fixture: ComponentFixture<RoundListComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [RoundListComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RoundListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should be created', () => {
        expect(component).toBeTruthy();
    });
});
