import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RoundsFormComponent } from './rounds-form.component';

describe('RoundsFormComponent', () => {
  let component: RoundsFormComponent;
  let fixture: ComponentFixture<RoundsFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RoundsFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RoundsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
