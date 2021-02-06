import {
  Component,
  ChangeDetectionStrategy,
  ViewChild,
  TemplateRef
} from '@angular/core';
import {
  isSameDay,
  isSameMonth,
} from 'date-fns';
import {Subject} from 'rxjs';
import {NgbDateNativeAdapter, NgbDateStruct, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {
  CalendarEvent,
  CalendarEventAction,
  CalendarEventTimesChangedEvent, CalendarEventTitleFormatter,
  CalendarView
} from 'angular-calendar';
import {CustomEventTitleFormatter} from "./DetailedInfoFormatter";
import {AppService} from "../app.service";
import {Router} from "@angular/router";
import {HolidayPlanningsMessage} from "../models/HolidayPlanningsMessage";

//Defines the component style and html page
@Component({
  selector: 'app-planner',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './planner.component.html',
  styleUrls: ['./planner.component.css'],
  providers: [{
    provide: CalendarEventTitleFormatter,
    useClass: CustomEventTitleFormatter
  }

  ]
})
export class PlannerComponent {
  @ViewChild('modalContent') modalContent: TemplateRef<any>;
  locations = ["Brugge", "Gent", "Antwerpen"]

  //View of the calendar starts with the month view, can be changed to day or week
  view: CalendarView = CalendarView.Month;

  CalendarView = CalendarView;
  updateButtonText: string;

  selectedLocation = 'Alles';
  //Date of the first calendar
  viewDate: Date = new Date();

  //Date of the second calendar
  viewSecondDate: Date = new Date(new Date().setMonth(new Date().getMonth() + 1));

  //Struct used for handling events
  modalData: {
    action: string;
    event: CalendarEvent;

  };

  adapter: NgbDateNativeAdapter = new NgbDateNativeAdapter();
  //Info popups when events are edited or deleted
  actions: CalendarEventAction[] = [
    {
      label: '<i class="fa fa-fw fa-pencil"></i>',
      onClick: ({event}: { event: CalendarEvent }): void => {
        this.handleEvent('Edited', event);
      }
    },
    {
      label: '<i class="fa fa-fw fa-times"></i>',
      onClick: ({event}: { event: CalendarEvent }): void => {
        this.events = this.events.filter(iEvent => iEvent !== event);
        this.handleEvent('Deleted', event);
      }
    }
  ];

  //Refreshes the calendar components if something were to change, called by this.refresh.next
  refresh: Subject<any> = new Subject();

  //List of events to be shown on the calendar, events have an id to group them by submit
  events: CalendarEvent[] = [];


  //Determines whether extra info of a day is shown or not by clicking for the first calendar
  activeDayIsOpen: boolean = false;
  userid: any;
  hm;

  constructor(private modal: NgbModal, private appService: AppService, private router: Router) {
    if (!this.appService.isAuthenticated()) {
      console.log(this.appService.isAuthenticated());
      this.router.navigate(['/login']).then();
    } else {
      this.appService.getHolidaysInConsiderationAndNew().subscribe(data => {
        console.log(data);
        this.hm = data;

        if (this.hm.length > 0) {
          this.updateButtonText = "Updates★";
        } else {
          this.updateButtonText = "Updates";
        }
      });
      this.appService.getUser().subscribe(data => {
        console.log(data);
        this.userid = data.userId;
        console.log(this.userid);
      });

    }

  }

  inputDate: NgbDateStruct = this.adapter.fromModel(new Date());

  //Eventhandler for when a day is clicked on the first calendar, first it determines if it is the same month as the current view
  //Then changes the current date to the clicked day and opens/closes it
  dayClicked({date, events}: { date: Date; events: CalendarEvent[] }): void {
    if (isSameMonth(date, this.viewDate)) {
      this.viewDate = date;
      this.inputDate = this.adapter.fromModel(new Date(this.viewDate));
      let d = new Date();
      d.setHours(0, 0, 0, 0);
      if (
        (isSameDay(this.viewDate, date) && this.activeDayIsOpen === true) ||
        events.length === 0
      ) {
        this.activeDayIsOpen = false;
      } else {
        this.activeDayIsOpen = true;
      }
    }
  }

  //WILL BE USED FOR LATER, DRAGGING EVENTS not in use yet
  eventTimesChanged({
                      event,
                      newStart,
                      newEnd
                    }: CalendarEventTimesChangedEvent): void {
    event.start = newStart;
    event.end = newEnd;
    this.handleEvent('Dropped or resized', event);
    this.refresh.next();
  }

  //FOr now only showing info boxes that a certain action has happened, just for info for now
  handleEvent(action: string, event: CalendarEvent): void {
    this.modalData = {event, action};
    this.modal.open(this.modalContent, {size: 'lg'});
  }

  //Method that executes when it receives emitted events from the eventform as array, adds all events to the array
  //that stores the events to be displayed on the calendars
  addEvent(events) {
    for (let event of events) {
      console.log(event);
      this.events.push(event);
    }

    this.refresh.next();
  }

  //When clicked on the title of the event after clicking on a day, this method will delete all absences with the same
  //id, as long as the absences are submitted at once, those will be deleted
  handleEventClick(event: CalendarEvent): void {
    let id = event.id;
    for (let i = this.events.length - 1; i >= 0; i--) {
      if (this.events[i].id == id) {
        this.events.splice(i, 1);
      }
    }
    if (this.activeDayIsOpen) {
      this.activeDayIsOpen = false;
    }

    if (this.activeDayIsOpen2) {
      this.activeDayIsOpen2 = false;
    }
    this.refresh.next();
  }

  //Method used for changing the second calendar according to the buttons clicked so the second calendar will always
  //show a month later
  MonthClicked(): void {
    this.viewSecondDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1);
    this.refresh.next();
    console.log(this.viewSecondDate);
    console.log(this.viewDate);

  }

  //Event handler for the second calendar when a day is clicked, has the same functionality as the first calendar
  activeDayIsOpen2: boolean = false;

  //EVENTHANDLERS SECOND CALENDAR
  dayClicked2({date, events}: { date: Date; events: CalendarEvent[] }): void {
    if (isSameMonth(date, this.viewSecondDate)) {
      this.viewSecondDate = date;
      let d = new Date();
      d.setHours(0, 0, 0, 0);
      if (events[0] == null && this.viewSecondDate >= d) {
        this.inputDate = this.adapter.fromModel(new Date(this.viewSecondDate));
      }
      if (
        (isSameDay(this.viewSecondDate, date) && this.activeDayIsOpen2 === true) ||
        events.length === 0
      ) {
        this.activeDayIsOpen2 = false;
      } else {
        this.activeDayIsOpen2 = true;
      }

    }
  }

  changeCalendar(location) {
    document.getElementById(location).classList.add('active');
    document.getElementById(this.selectedLocation).classList.remove('active');
    this.selectedLocation = location;
  }

  handleAbsence(event: any, action: string) {
    if (this.hm.length > 0) {
      console.log(action + "!");
      console.log(event.target.name)
      let id = event.target.name;

      let i = this.hm.length - 1;
      let found = false;
      let gevondenindex = 0;
      while (i >= 0 && !found) {
        if (this.hm[i].id == id) {
          found = true;
          gevondenindex = i;
        }
        i -= 1;
      }
      this.hm[gevondenindex].state = action;
      //TODO change to POST /holidayPlanningsMessage
      let holidayPlanningMessage = this.createHolidayPlanningMessage(this.hm[gevondenindex]);

      this.appService.postHolidayPlanningsMessage(holidayPlanningMessage).subscribe(data => {
        console.log(data);

        this.hm.splice(gevondenindex, 1);
        if (this.hm.length == 0) {
          this.updateButtonText = "Updates"
        }
        this.refresh.next();
      });
      /*
      this.appService.postHolidayMessage(this.hm[gevondenindex]).subscribe( (data) =>{
        console.log(data);
        this.hm.splice(gevondenindex, 1);
        if(this.hm.length == 0){
          this.updateButtonText="Updates"
        }
        this.refresh.next();
      });*/
    }

  }

  createHolidayPlanningMessage(hm: any): HolidayPlanningsMessage {
    let hpm = new HolidayPlanningsMessage();
    hpm.id = hm.id;
    hpm.state = hm.state;
    hpm.comment = "Geen commentaar";
    hpm.plannerID = this.userid;
    return hpm;
  }


  openUpdatesPopup() {
    this.modalData = {action: "Click", event: null};
    this.modal.open(this.modalContent, {size: 'lg'});
  }


}
