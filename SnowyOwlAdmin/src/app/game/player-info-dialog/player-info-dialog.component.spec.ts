import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerInfoDialogComponent } from './player-info-dialog.component';

describe('PlayerInfoDialogComponent', () => {
  let component: PlayerInfoDialogComponent;
  let fixture: ComponentFixture<PlayerInfoDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlayerInfoDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayerInfoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
