import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddIneligibleSubComponent } from './add-ineligible-sub.component';

describe('AddIneligibleSubComponent', () => {
  let component: AddIneligibleSubComponent;
  let fixture: ComponentFixture<AddIneligibleSubComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddIneligibleSubComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddIneligibleSubComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
