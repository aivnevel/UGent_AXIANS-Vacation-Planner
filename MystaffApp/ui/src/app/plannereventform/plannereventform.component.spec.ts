import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlannereventformComponent } from './plannereventform.component';

describe('PlannereventformComponent', () => {
  let component: PlannereventformComponent;
  let fixture: ComponentFixture<PlannereventformComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlannereventformComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlannereventformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
