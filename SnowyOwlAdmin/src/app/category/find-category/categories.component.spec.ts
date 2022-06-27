import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FindCategoryComponent } from './categories.component';

describe('FindCategoryComponent', () => {
  let component: FindCategoryComponent;
  let fixture: ComponentFixture<FindCategoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FindCategoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FindCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
