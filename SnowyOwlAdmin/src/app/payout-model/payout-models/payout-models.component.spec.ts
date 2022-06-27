import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PayoutModelsComponent } from './payout-models.component';

describe('PayoutModelsComponent', () => {
  let component: PayoutModelsComponent;
  let fixture: ComponentFixture<PayoutModelsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PayoutModelsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PayoutModelsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
