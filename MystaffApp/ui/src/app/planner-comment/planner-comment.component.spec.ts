import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlannerCommentComponent } from './planner-comment.component';

describe('PlannerCommentComponent', () => {
  let component: PlannerCommentComponent;
  let fixture: ComponentFixture<PlannerCommentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlannerCommentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlannerCommentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
