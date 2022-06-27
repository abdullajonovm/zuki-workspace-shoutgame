import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AssignCodesComponent } from './assign-codes.component';

describe('AssignCodesComponent', () => {
  let component: AssignCodesComponent;
  let fixture: ComponentFixture<AssignCodesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AssignCodesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignCodesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
