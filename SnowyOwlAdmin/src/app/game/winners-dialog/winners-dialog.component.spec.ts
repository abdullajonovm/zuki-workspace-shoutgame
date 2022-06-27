import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WinnersDialogComponent } from './winners-dialog.component';

describe('WinnersDialogComponent', () => {
  let component: WinnersDialogComponent;
  let fixture: ComponentFixture<WinnersDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WinnersDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WinnersDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
