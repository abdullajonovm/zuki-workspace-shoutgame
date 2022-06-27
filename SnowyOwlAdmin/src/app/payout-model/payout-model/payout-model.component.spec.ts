import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PayoutModelComponent } from './payout-model.component';

describe('PayoutModelComponent', () => {
  let component: PayoutModelComponent;
  let fixture: ComponentFixture<PayoutModelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PayoutModelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PayoutModelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
