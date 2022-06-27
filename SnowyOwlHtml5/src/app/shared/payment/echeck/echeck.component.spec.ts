import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EcheckComponent } from './echeck.component';

describe('EcheckComponent', () => {
  let component: EcheckComponent;
  let fixture: ComponentFixture<EcheckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EcheckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EcheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
