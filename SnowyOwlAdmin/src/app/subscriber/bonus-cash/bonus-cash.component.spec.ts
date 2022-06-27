import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BonusCashComponent } from './bonus-cash.component';

describe('SubscriberRolesComponent', () => {
  let component: BonusCashComponent;
  let fixture: ComponentFixture<BonusCashComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BonusCashComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BonusCashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
