import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamPlanningComponent } from './team-planning.component';

describe('TeamPlanningComponent', () => {
  let component: TeamPlanningComponent;
  let fixture: ComponentFixture<TeamPlanningComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TeamPlanningComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamPlanningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
