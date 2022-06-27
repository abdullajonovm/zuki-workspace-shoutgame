import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameGuideComponent } from './game-guide.component';

describe('GameGuideComponent', () => {
  let component: GameGuideComponent;
  let fixture: ComponentFixture<GameGuideComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameGuideComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameGuideComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
