import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GeneratePayoutsDialogComponent } from './generate-payouts-dialog.component';

describe('GeneratePayoutsDialogComponent', () => {
  let component: GeneratePayoutsDialogComponent;
  let fixture: ComponentFixture<GeneratePayoutsDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GeneratePayoutsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GeneratePayoutsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
