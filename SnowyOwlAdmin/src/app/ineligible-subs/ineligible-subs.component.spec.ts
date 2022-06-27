import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IneligibleSubsComponent } from './ineligible-subs.component';

describe('IneligibleSubsComponent', () => {
  let component: IneligibleSubsComponent;
  let fixture: ComponentFixture<IneligibleSubsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IneligibleSubsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IneligibleSubsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
