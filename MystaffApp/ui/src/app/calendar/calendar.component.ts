import {
  Component,
  ChangeDetectionStrategy,
  ViewChild,
  TemplateRef, ViewChildren, QueryList, OnInit, Input
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
  CalendarView,
  DAYS_OF_WEEK
} from 'angular-calendar';
import {CustomEventTitleFormatter} from "./DetailedInfoFormatter";
import {AppService} from "../app.service";
import {Router} from "@angular/router";
import {EventformComponent} from "../eventform/eventform.component";
import {TranslateService} from "../translate";
import {DatePipe} from "@angular/common";

export interface CalendarAbsence extends CalendarEvent {
  symbol?: string;
  state?: string;
  s?: Date;
  e?: Date;
  approvedCount?: string;
  comments?: string[];
  edited?: boolean;
}


//Const colors used in the calendar for now -> Also used as identification of the status of the absence
//See HTML file of this component for more info
const colors: any = {
  red: {
    primary: '#ad2121',
    secondary: '#FAE3E3'
  },
  blue: {
    primary: '#1e90ff',
    secondary: '#D1E8FF'
  },
  yellow: {
    primary: '#e3bc08',
    secondary: '#FDF1BA'
  },
  gray: {
    primary: '#DCDCDC',
    secondary: '#F5F5F5'
  },
  green: {
    primary: '#008000',
    secondary: '#ABCE9E'
  },
  orange: {
    primary: '#FD6A02',
    secondary: '#FDA50F'
  }
};


@Component({
  selector: 'app-calendar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css'],
  providers: [{
    provide: CalendarEventTitleFormatter,
    useClass: CustomEventTitleFormatter
  }

  ]
})
export class CalendarComponent implements OnInit {

  //Popup window template when an absence is clicked
  @ViewChild('modalContent') modalContent: TemplateRef<any>;
  //NOT USED AT THE MOMENT
  @ViewChild('templateApproved') templateApproved: TemplateRef<any>;
  //This variable is used to access the eventform child component in the calendar component
  @ViewChildren(EventformComponent) eventform: QueryList<EventformComponent>;


  //Variable that states whether the sidebar is opened or closed
  _opened: boolean = false;

  //Function that handles the opening and closing of the sidebar
  public _toggleSidebar() {
    this._opened = !this._opened;
  }

  //Extension of the function above, adds the clicked date as startdate
  _toggleSidebarButton() {
    this.inputDate = this.adapter.fromModel(new Date());
    this._toggleSidebar();
  }

  //View of the calendar starts with the month view, can be changed to day or week
  view: CalendarView = CalendarView.Month;

  //Locale, variable that determines the startday of the week and which days are weekends
  locale: string;
  weekStartsOn: number = DAYS_OF_WEEK.MONDAY;
  weekendDays: number[] = [DAYS_OF_WEEK.FRIDAY, DAYS_OF_WEEK.SATURDAY];

  //Has to be set on false to make sure the screen does not scroll all the way to the top when the sidebar opens
  focused: boolean;

  CalendarView = CalendarView;

  //Current selected date of the first calendar that is stored
  viewDate: Date = new Date();

  //Current selected date of the second calendar
  viewSecondDate: Date = new Date(new Date().setMonth(new Date().getMonth() + 1));

  //Struct used for handling events
  modalData: {
    action: string;
    event: CalendarAbsence;

  };

  //Refreshes the calendar components if something were to change, called by this.refresh.next
  refresh: Subject<any> = new Subject();

  //List of events to be shown on the calendar, events have an id to group them by submit
  events: CalendarAbsence[] = [];

  //Determines whether extra info of a day is shown or not by clicking for the first calendar
  activeDayIsOpen: boolean = false;

  //Event handler for the second calendar when a day is clicked, has the same functionality as the first calendar
  activeDayIsOpen2: boolean = false;

  //Adapter used for converting Dates <-> NgbDate(structs)
  adapter: NgbDateNativeAdapter = new NgbDateNativeAdapter();

  inputDate: NgbDateStruct = this.adapter.fromModel(new Date());
  outputDate: NgbDateStruct = this.adapter.fromModel(new Date());

  //Data binding for History component
  rows = [];
  updates = [];
  table_titles = ["state", "history", "requestDate", "start", "end", "type", "comment"];

  //Possible actions of the events
  actions: CalendarEventAction[] = [
    {
      //This action handles the deletion of an absence when the X is clicked
      label: '<b class="fa fa-fw fa-times">❌</b>',
      onClick: ({event}: { event: CalendarEvent }): void => {
        this.handleEventClick(event);
      }
    },
    {
      //This action handles the edit of an absence when the "Edit" text is clicked
      label: '<b class="fa fa-fw fa-times">' + this._translate.instant('EDIT') + '</b>',
      onClick: ({event}: { event: CalendarAbsence }): void => {
        this.editAbsence(event);
      }
    }
  ];

  //The constructor calls the AppService methods to get all the necessary data to be shown on the calendar
  constructor(private modal: NgbModal, private appService: AppService, private router: Router, private datePipe: DatePipe, public _translate: TranslateService) {

    this._translate.LanguageChange.on(() => {
      this.activeDayIsOpen = false;
      this.activeDayIsOpen2 = false;

      this.updateText();

      this.actions = [
        {
          //This action handles the deletion of an absence when the X is clicked
          label: '<b class="fa fa-fw fa-times">❌</b>',
          onClick: ({event}: { event: CalendarEvent }): void => {
            this.handleEventClick(event);
          }
        },
        {
          //This action handles the edit of an absence when the "Edit" text is clicked
          label: '<b class="fa fa-fw fa-times">' + _translate.instant('EDIT') + '</b>',
          onClick: ({event}: { event: CalendarAbsence }): void => {
            this.editAbsence(event);
          }
        }
      ];

      this.events.forEach(event => {
        if(event.s >= new Date(new Date().getDate()+1)){
          event.actions = this.actions;
        }
      });

      this.refresh.next();
    });

    //Redirection if the person is not logged in
    if (!this.appService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    } else {
      //Focused set on false to prevent scrolling to top
      //Todo make the ngsidebar scrollable together with window view
      this.focused = false;

      //Gets all the organisation holidays from the database
      this.appService.getOrganizationHolidays().subscribe((data): any => {
        this.addOrganizationHolidays(data);
      });

      //First it gets the current logged in user, this id is used to get all the absences that are present in the db of
      //that user
      this.appService.getUser().subscribe((data) => {
        this.appService.getHolidayMessageById().subscribe((data): any => {
          //console.log(data);
          this.convertHolidayMessagesToCalendarEvent(data);
        });
      });
      //load data for history component
      this.loadHistoryData();
    }
  }

  ngOnInit(): void {
    this.updateText();
  }

  updateText(): void {
    this.locale = this._translate.currentLang;
    this.refresh.next();
  }


  //Eventhandler for when a day is clicked on the first calendar, first it determines if it is the same month as the current view
  //Then changes the current date to the clicked day and opens/closes it
  dayClicked({date, events}: { date: Date; events: CalendarEvent[] }): void {
    if (isSameMonth(date, this.viewDate)) {
      this.viewDate = date;
      let d = new Date();
      d.setHours(0, 0, 0, 0);
      //Check if the clicked date is equal to or later than the current date
      if (this.viewDate >= d && events.length < 1) {
        //Als wat er al ingevuld is dezelfde date is als de viewdate, dan toggle sidebar
        if (this.viewDate.getDate() == this.adapter.toModel(this.inputDate).getDate()) {
          if (this.activeDayIsOpen || events.length === 0) {
            this._toggleSidebar();
          } else {
            if (!this._opened) {
              this._toggleSidebar();
            }
          }
        } else {
          if (!this._opened) {
            this._toggleSidebar();
          }
          this.inputDate = this.adapter.fromModel(new Date(this.viewDate));
          this.outputDate = this.adapter.fromModel(new Date(this.viewDate));
        }
      }
      if (
        (isSameDay(this.viewDate, date) && this.activeDayIsOpen === true) ||
        events.length === 0
      ) {
        this.activeDayIsOpen = false;
        if(events.length >= 1){
          this._opened = false;
          let e = this.eventform.first;
          e.edit = false;
        }

      } else {
        this.activeDayIsOpen = true;
        if(events.length >= 1){
          this._opened = false;
          let e = this.eventform.first;
          e.edit = false;
        }
      }
    }
  }

  //EVENTHANDLERS SECOND CALENDAR, zelfde principe als hierboven
  dayClicked2({date, events}: { date: Date; events: CalendarEvent[] }): void {
    let e = this.eventform.first;
    if (isSameMonth(date, this.viewSecondDate)) {
      let e = this.eventform.first;
      this.viewSecondDate = date;
      let d = new Date();
      d.setHours(0, 0, 0, 0);
      if (this.viewSecondDate >= d && events.length < 1) {
        if (this.viewSecondDate.getDate() == this.adapter.toModel(this.inputDate).getDate()) {
          if (this.activeDayIsOpen2 || events[0] == null) {
            this._toggleSidebar();
          } else {
            if (!this._opened) {
              this._toggleSidebar();
            }
          }
        } else {
          if (!this._opened) {
            this._toggleSidebar();
          }
          this.inputDate = this.adapter.fromModel(new Date(this.viewSecondDate));
          this.outputDate = this.adapter.fromModel(new Date(this.viewSecondDate));
        }
      }
      if (
        (isSameDay(this.viewSecondDate, date) && this.activeDayIsOpen2 === true) ||
        events.length === 0
      ) {
        this.activeDayIsOpen2 = false;
        if(events.length >= 1){
          this._opened = false;
          let e = this.eventform.first;
          e.edit = false;
        }
      } else {
        this.activeDayIsOpen2 = true;
        if(events.length >= 1){
          this._opened = false;
          let e = this.eventform.first;
          e.edit = false;
        }
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

  //For now only showing info boxes that a certain action has happened, just for info for now
  handleEvent(action: string, event: CalendarAbsence): void {
    this.modalData = {event, action};
    this.modal.open(this.modalContent, {size: 'lg'});
  }

  //Method that executes when it receives emitted events from the eventform as array, adds all events to the array
  //that stores the events to be displayed on the calendars
  //Deletabsence is used to delete all the absences of that id and replace them
  addEvent(events) {
    this._toggleSidebar();
    this.deleteAbsencesFromCalendar(events[0].id);
    if (events[0].edited == true) {
      if (this.activeDayIsOpen) {
        this.activeDayIsOpen = !this.activeDayIsOpen
      }
      if (this.activeDayIsOpen2) {
        this.activeDayIsOpen2 = !this.activeDayIsOpen2;
      }
    }
    for (let event of events) {
      if(event.s >= new Date(new Date().getDate()+1)){
        event.actions = this.actions;
        event.title = this.eventform.first.getAbsenceKey(event.title);
      }

      this.events.push(event);
    }

    this.refresh.next();
  }

  //When clicked on the title of the event after clicking on a day, this method will delete all absences with the same
  //id, as long as the absences are submitted at once, those will be deleted
  handleEventClick(event: CalendarEvent): void {
    let e = this.eventform.first;
    if (e.edit == true) {
      e.edit = false;
    }
    if (this._opened) {
      this._toggleSidebar();
    }
    let id = event.id;
    this.deleteAbsencesFromCalendar(id);
    this.appService.deleteHolidayMessage(Number(id)).subscribe(() => {
      //console.log("DELETED: ", id)
    });
    if (this.activeDayIsOpen) {
      this.activeDayIsOpen = false;
    }

    if (this.activeDayIsOpen2) {
      this.activeDayIsOpen2 = false;
    }
    //console.log("delete", id);
    this.appService.deleteAbsence("" + id);
    this.refresh.next();
  }

  //Method that loops over the array from end to begin and deletes all the absences with the given id
  deleteAbsencesFromCalendar(id: any) {
    for (let i = this.events.length - 1; i >= 0; i--) {
      if (this.events[i].id == id) {
        this.events.splice(i, 1);
      }
    }
  }

  //Handles the functionality of someone wants to edit existing absences
  editAbsence(event: CalendarAbsence): void {
    this._opened = !this._opened;
    //console.log("Wijzig events");
    let e = this.eventform.first;
    if (e.edit == false) {
      e.edit = true;
      this.inputDate = this.adapter.fromModel(event.s);
      this.outputDate = this.adapter.fromModel(event.e);
      e.comments = event.meta;
      e.selectedPeriod = this.bepaalPeriode(e, event);
      e.selectedOption = this.bepaalOption(e, event);
      e.editid = event.id;
    } else {
      e.edit = false;
    }

  }

  //Used to fill in the selectedPeriod option-list incase of edit
  bepaalPeriode(e: EventformComponent, event: CalendarEvent): string {
    if (event.start.getHours() == 9 && event.end.getHours() == 17) {
      return 'Hele dag';
    } else if (event.start.getHours() == 13) {
      return 'Namiddag';
    } else {
      return 'Voormiddag';
    }
  }

  //Fills in the selectedChoice option-list incase of edit
  bepaalOption(e: EventformComponent, event: CalendarEvent): string {
    return event.title;
  }

  //Method used for changing the second calendar according to the buttons clicked so the second calendar will always
  //show a month later
  MonthClicked(): void {
    this.viewSecondDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1);
    this.refresh.next();
    //console.log(this.viewSecondDate);
    //console.log(this.viewDate);
  }

  //Converts a given Date to a calendar event
  convertDatesToCalendarEvent(start: Date, end: Date, h1: number, h2: number, uid: string, title: string): CalendarAbsence {
    if (title == "YEARLY_VACATION" || title == "Yearly") {
      title = 'OPTION_ANNUAL_LEAVE';
    } else if (title == "EUROPEAN_VACATION" || title == "European") {
      title = 'OPTION_EUROPEAN_LEAVE';
    } else if (title == "EDUCATIONAL_VACATION" || title == "Educative") {
      title = 'OPTION_EDUCATIONAL_LEAVE';
    } else if (title == "Other") {
      title = 'OPTION_OTHER';
    } else if (title == "Sickness") {
      title = 'OPTION_SICKNESS';
    }

    start.setHours(h1);
    end.setHours(h2);
    return {
      id: uid,
      start: new Date(start),
      end: new Date(end),
      title: title,
      color: colors.gray,
      symbol: "",
      state: ""
    };
  }

  //Shows the popup when an event is clicked
  showEvent(event: CalendarEvent): void {
    if (event.start.getHours() != 0) {
      this.modalData = {action: "test", event: event};
      this.modal.open(this.modalContent, {size: 'lg'});
    }
  }

  //Converts the backend format of organization holidays to frontend format
  addOrganizationHolidays(data: any): void {
    for (let holiday of data) {
      //console.log(holiday);
      let startDate = new Date(holiday.date);
      let endDate = new Date(holiday.date);
      startDate.setHours(0, 0);
      endDate.setHours(23, 59);
      this.events.push({
        id: holiday.id,
        title: holiday.desc,
        start: startDate,
        end: endDate,
        color: colors.blue,
        state: this._translate.instant('OFFICIAL_HOLIDAY'),
        symbol: ""
      });
    }
    this.refresh.next();
    //console.log(this.events);
  }

  //Converts the backend format holidayMessage to Frontendformat CalendarEvent
  convertHolidayMessagesToCalendarEvent(data: any) {
    for (let hm of data) {
      //console.log(hm);
      let i = 0;
      while (i < hm.exactDates.length) {
        let e = hm.exactDates[i];
        //console.log(e);
        let start = e.date;
        let daypart = e.daypart;
        let event;
        if (hm.exactDates[i + 1]) {
          //console.log("------", e);
          let end = hm.exactDates[i + 1].date;
          if (start[0] == end[0] && start[1] == end[1] && start[2] == end[2]) {
            //console.log("zelfde dag");
            let d1 = new Date(start[0], start[1] - 1, start[2]);
            let d1copy = new Date(start[0], start[1] - 1, start[2]);
            event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 17, hm.id, hm.type);
            i += 2;
          } else {
            //console.log("andere dag");
            let d1 = new Date(start[0], start[1] - 1, start[2]);
            let d1copy = new Date(start[0], start[1] - 1, start[2]);
            if (daypart == "PM") {
              event = this.convertDatesToCalendarEvent(d1, d1copy, 13, 17, hm.id, hm.type);
            } else {
              event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 13, hm.id, hm.type);
            }
            i += 1;
          }
        } else {
          //console.log("andere dag");
          let d1 = new Date(start[0], start[1] - 1, start[2]);
          let d1copy = new Date(start[0], start[1] - 1, start[2]);
          if (daypart == "PM") {
            event = this.convertDatesToCalendarEvent(d1, d1copy, 13, 17, hm.id, hm.type);
          } else {
            event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 13, hm.id, hm.type);
          }
          i += 1;
        }
        if (hm.state == "New") {
          event.color = colors.yellow;
          event.symbol = "⏳"
          event.state = 'EVENT_STATE_NEW';
        } else if (hm.state == "InConsideration") {
          event.color = colors.orange;
          event.symbol = "⏳"
          event.state = 'EVENT_STATE_IN_CONSIDERATION';
        } else if (hm.state == "Approved") {
          event.color = colors.green;
          event.symbol = "✅"
          event.state = 'EVENT_STATE_APPROVED';
        } else {
          event.color = colors.red;
          event.symbol = "❌"
          event.state = 'EVENT_STATE_REJECTED';
        }
        event.s = new Date(hm.exactDates[0].date);
        event.e = new Date(hm.exactDates[hm.exactDates.length - 1].date);
        if(event.s >= new Date(new Date().getDate()+1)){
          event.actions = this.actions;
        }
        event.meta = hm.comment;
        event.approvedCount = this.countApproved(hm.planningUnitStates);

        event.comments = new Array();

        for (let planningunitstate of hm.planningUnitStates) {
          event.comments.push(planningunitstate.comment);
        }
        //console.log(event);
        this.events.push(event);
      }
    }
    this.refresh.next();
  }

  countApproved(planningunitstates: any[]): string {
    let stateCount = 0;
    let approvedCount = 0;
    for (let state of planningunitstates) {
      if (state.state == "Approved") {
        approvedCount = approvedCount + 1;
      }
      stateCount = stateCount + 1;
    }
    return approvedCount + "/" + stateCount;
  }

  checkTimeOfDay(events: CalendarEvent[], hour: any): boolean {
    let colored = false;
    let i = 0;
    if (events.length > 0) {
      let event = events[i];
      while (event && !colored) {
        if (event.start.getHours() == hour) {
          colored = true;
        }
        i++;
        event = events[i];
      }
    }
    return colored;
  }

  checkTimeOfDay2(events: CalendarEvent[], hour: any): boolean {
    let colored = false;
    let i = 0;
    if (events.length > 0) {
      let event = events[i];
      while (event && !colored) {
        if (event.end.getHours() == hour) {
          colored = true;
        }
        i++;
        event = events[i];
      }
    }
    return colored;
  }

  //fetch history of holiday messages from current employee
  loadHistoryData(): void {
    this.appService.getUser().subscribe((data) => {
      this.appService.getHolidayMessageWithEmployeeID(data.userId).subscribe((data) => {
        //console.log(data);
        this.fillUpdates(data);
        this.refresh.next();
      });
    });
  }

  //create the data to pass through history component
  private fillUpdates(data: any): void {
    this.updates = [];
    this.rows = [];
    for (let message of data) {
      let allDates = [];
      let exactDates = message.exactDates;
      for (let dates of exactDates) {
        let obj = {date: dates.date, daypart: dates.daypart};
        allDates.push(obj);
      }
      allDates.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
      let emoji = "";
      let status = "";
      switch (message.state) {
        case "Approved":
          emoji = "check";
          status = "goedgekeurd";
          break;
        case "Rejected":
          emoji = "close";
          status = "afgekeurd";
          break;
        case "InConsideration":
          emoji = "hourglass_empty";
          status = "in beraad";
          break;
        case "New":
          emoji = "hourglass_empty";
          status = "in beraad";
          break;
      }
      for (let hm of message.history) {
        let allDatesHistory = [];
        for (let dates of hm.exactDates) {
          allDatesHistory.push(dates.date);
        }
        allDatesHistory.sort((a, b) => new Date(a).getTime() - new Date(b).getTime());
        hm['start'] = this.datePipe.transform(new Date(allDatesHistory[0]), "dd/MM/yyyy HH:mm");
        hm['end'] = this.datePipe.transform(new Date(allDatesHistory[allDatesHistory.length - 1]), "dd/MM/yyyy HH:mm");
        //console.log(new Date(hm.requestDate));
        hm['requestDate'] = this.datePipe.transform(new Date(hm.requestDate), "dd/MM/yyyy HH:mm");
        //console.log(hm.requestDate);
        let emoji2 = "";
        let status2 = "";
        switch (hm.state) {
          case "Approved":
            emoji2 = "check";
            status2 = "goedgekeurd";
            break;
          case "Rejected":
            emoji2 = "close";
            status2 = "afgekeurd";
            break;
          case "InConsideration":
            emoji2 = "hourglass_empty";
            status2 = "in beraad";
            break;
          case "New":
            emoji2 = "hourglass_empty";
            status2 = "in beraad";
            break;
        }
        hm['emoji'] = emoji2;
        hm['status'] = status2;
      }
      this.updates.push({
        id: message.id,
        symbol: emoji,
        requestDate: this.datePipe.transform(new Date(message.requestDate), 'medium'),
        start: this.datePipe.transform(new Date(allDates[0]['date']), "dd/MM/yyyy") + " " + allDates[0]['daypart'],
        end: this.datePipe.transform(new Date(allDates[allDates.length - 1]['date']), "dd/MM/yyyy") + " " + allDates[allDates.length - 1]['daypart'],
        type: message.type,
        comment: message.comment,
        state: status,
        history: message.history
        //hh:mm dd/MM/yyyy
      });
    }
    this.updates.reverse();
    for (let element of this.updates) {
      if (element.history.length > 0) {
        this.rows.push(element, {detailRow: true, element});
      } else {
        this.rows.push(element);
      }
    }
  }
}
