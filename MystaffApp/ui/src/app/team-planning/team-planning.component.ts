import {
  Component,
  ViewChild,
  TemplateRef, ViewChildren, QueryList, OnInit
} from '@angular/core';
import {
  isSameDay,
  isSameMonth,
} from 'date-fns';
import {Observable, Subject, forkJoin} from 'rxjs';
import {NgbDateNativeAdapter, NgbDateStruct, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {
  CalendarEvent,
  CalendarEventTimesChangedEvent, CalendarEventTitleFormatter,
  CalendarView,
  DAYS_OF_WEEK
} from 'angular-calendar';
import {
  CustomCalendarEvent
} from "./CustomCalendarEvent";
import {AppService} from "../app.service";
import {Router} from "@angular/router";
import {EventformComponent} from "../eventform/eventform.component";
import {CustomEventTitleFormatter} from "./DetailedInfoFormatterTeamplanning";
import {DictionaryService} from "../dictionary.service";
import {DatePipe} from "@angular/common";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {TranslateService} from "../translate";


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
  selector: 'app-team-planning',
  templateUrl: './team-planning.component.html',
  styleUrls: ['./team-planning.component.css'],
  providers: [{
    provide: CalendarEventTitleFormatter,
    useClass: CustomEventTitleFormatter
  }
  ],
  //animations used for the detailview
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', visibility: 'hidden'})),
      state('expanded', style({height: '*', visibility: 'visible'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class TeamPlanningComponent implements OnInit {

//Popup window template when an absence is clicked
  @ViewChild('modalContent') modalContent: TemplateRef<any>;
  //This variable is used to access the eventform child component in the calendar component
  @ViewChildren(EventformComponent) eventform: QueryList<EventformComponent>;

  //the teams the user is a part of
  teamList = [];
  //teams to display in the calendar
  teamsToView: any;

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
    event: CustomCalendarEvent;

  };

  //Refreshes the calendar components if something were to change, called by this.refresh.next
  refresh: Subject<any> = new Subject();


  //List of events to be shown on the calendar, events have an id to group them by submit
  events: CustomCalendarEvent[] = [];

  //Determines whether extra info of a day is shown or not by clicking for the first calendar
  activeDayIsOpen: boolean = false;

  //Event handler for the second calendar when a day is clicked, has the same functionality as the first calendar
  activeDayIsOpen2: boolean = false;

  //Adapter used for converting Dates <-> NgbDate(structs)
  adapter: NgbDateNativeAdapter = new NgbDateNativeAdapter();

  //history component variables
  table_titles = ["name", "state", "history", "requestDate", "start", "end", "type", "comment"];
  //the data to display without the detail row
  updates = [];
  //the data with an attribute for the detail row
  rows = [];

  constructor(private modal: NgbModal, private appService: AppService, private router: Router,
              private dictionaryService: DictionaryService, private datePipe: DatePipe, public _translate: TranslateService) {

    this._translate.LanguageChange.on(() => {
      this.updateText();
      this.activeDayIsOpen = false;
      this.activeDayIsOpen2 = false;
    });


    //Redirection if the person is not logged in
    if (!this.appService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    } else {
      //Focused set on false to prevent scrolling to top
      this.focused = false;

      //Gets all the organisation holidays from the database
      /*this.appService.getOrganizationHolidays().subscribe((data): any => {
        this.addOrganizationHolidays(data);
      });*/
      this.teamsToView = new Map();
      (async () => {
        let handler = () => {
          this.appService.getPlanningUnitsOfEmployee(AppService.user.userId).subscribe((data2) => {
            this.appService.getAllEmployeesOfPlanningUnitsWithIds(data2).subscribe((data) => {
              let membersToAdd = [];
              for (let val of data) {
                let load = true;
                for (let obj of membersToAdd) {
                  if (val.member == obj.member) {
                    load = false;
                  }
                }
                //do not load duplicate members
                if (load == true) {
                  membersToAdd.push(val);
                }
              }
              //put all async methods in Observable array
              let observables: Observable<any>[] = [];
              for (let member of membersToAdd) {
                observables.push(this.appService.getHolidayMessagesApprovedOfDoctorWithID(member.member));
              }
              let allData = [];
              //if all async methods are finished, load the name associated with the id
              forkJoin(observables).subscribe(dataArray => {
                for (let element of dataArray) {
                  for (let element2 of element) {
                    element2['name'] = this.dictionaryService.getCacheEmployeeFullNameOfId(element2.employeeID);
                    allData.push(element2);
                  }
                }
                console.log(allData);
                //fill the update array with data
                this.fillUpdates(allData);
              });


              //this.fillUpdates(membersToAdd);
            });
            //teams to display in select box
            for (let val of data2) {
              this.teamsToView[val] = false;
              let name = this.dictionaryService.getPlanningUnitNameOfId(val);
              let obj = {id: val, name: name};
              this.teamList.push(obj);
              console.log(val + ", " + name);
            }
          });
        };
        //when dictionaryService data is loaded
        if (!this.dictionaryService.dataLoaded) {
          this.dictionaryService.Ready.on(handler);
        } else {
          handler();
        }
      })();
    }
  }

  ngOnInit(): void {
    this.updateText();
  }

  updateText(): void {
    this.locale = this._translate.currentLang;
  }

  inputDate: NgbDateStruct = this.adapter.fromModel(new Date());

  //Eventhandler for when a day is clicked on the first calendar, first it determines if it is the same month as the current view
  //Then changes the current date to the clicked day and opens/closes it
  dayClicked({date, events}: { date: Date; events: CustomCalendarEvent[] }): void {
    if (isSameMonth(date, this.viewDate)) {
      this.viewDate = date;
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

  //EVENTHANDLERS SECOND CALENDAR, zelfde principe als hierboven
  dayClicked2({date, events}: { date: Date; events: CustomCalendarEvent[] }): void {
    let e = this.eventform.first;
    if (isSameMonth(date, this.viewSecondDate)) {
      let e = this.eventform.first;
      this.viewSecondDate = date;
      let d = new Date();
      d.setHours(0, 0, 0, 0);
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
  handleEvent(action: string, event: CustomCalendarEvent): void {
    this.modalData = {event, action};
    this.modal.open(this.modalContent, {size: 'lg'});
  }

  //Method used for changing the second calendar according to the buttons clicked so the second calendar will always
  //show a month later
  MonthClicked(): void {
    this.viewSecondDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1);
    this.refresh.next();
  }

  //Converts a given Date to a calendar event
  convertDatesToCalendarEvent(start: Date, end: Date, h1: number, h2: number, uid: string, title: string, name: string, state: string, comment: string): CustomCalendarEvent {
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
      name: name,
      state: state,
      comment: comment
    };
  }

  //Shows the popup when an event is clicked
  showEvent(event: CustomCalendarEvent): void {
    this.modalData = {action: "test", event: event};
    this.modal.open(this.modalContent, {size: 'lg'});
  }

  //Converts the backend format of organization holidays to frontend format
  addOrganizationHolidays(data: any): void {
    for (let holiday of data) {
      let startDate = new Date(holiday.date);
      let endDate = new Date(holiday.date);
      startDate.setHours(0, 0);
      endDate.setHours(23, 59);
      this.events.push({
        id: holiday.id,
        title: holiday.desc,
        start: startDate,
        end: endDate,
        color: colors.blue
      });
    }
    this.refresh.next();
  }

  //Converts the backend format holidayMessage to Frontendformat CalendarEvent
  convertHolidayMessagesToCalendarEvent(data: any) {
    for (let hm of data) {
      let load = true;
      for (let event of this.events) {
        if (event.id == hm.id) {
          load = false;
        }
      }
      if (load) {
        let i = 0;
        while (i < hm.exactDates.length) {
          let e = hm.exactDates[i];
          let start = e.date;
          let daypart = e.daypart;
          let name = this.dictionaryService.getCacheEmployeeFullNameOfId(hm.employeeID);
          let event;
          if (hm.exactDates[i + 1]) {
            console.log("------", e);
            let end = hm.exactDates[i + 1].date;
            if (start[0] == end[0] && start[1] == end[1] && start[2] == end[2]) {
              let d1 = new Date(start[0], start[1] - 1, start[2]);
              let d1copy = new Date(start[0], start[1] - 1, start[2]);
              event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 17, hm.id, hm.type, name, hm.state, hm.comment);
              i += 2;
            } else {
              let d1 = new Date(start[0], start[1] - 1, start[2]);
              let d1copy = new Date(start[0], start[1] - 1, start[2]);
              if (daypart == "PM") {
                event = this.convertDatesToCalendarEvent(d1, d1copy, 13, 17, hm.id, hm.type, name, hm.state, hm.comment);
              } else {
                event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 13, hm.id, hm.type, name, hm.state, hm.comment);

              }
              i += 1;
            }


          } else {
            let d1 = new Date(start[0], start[1] - 1, start[2]);
            let d1copy = new Date(start[0], start[1] - 1, start[2]);
            if (daypart == "PM") {
              event = this.convertDatesToCalendarEvent(d1, d1copy, 13, 17, hm.id, hm.type, name, hm.state, hm.comment);
            } else {
              event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 13, hm.id, hm.type, name, hm.state, hm.comment);

            }
            i += 1;
          }
          if (hm.state == "New") {
            event.color = colors.yellow;
          } else if (hm.state == "InConsideration") {
            event.color = colors.orange;
          } else if (hm.state == "Approved") {
            event.color = colors.green;
          } else {
            event.color = colors.red;
          }
          event.meta = hm.comment;
          this.events.push(event);
        }
      }
    }
    this.refresh.next();
  }

  //if a team is selected, display the absences in the calendar
  addTeamToCalendar(event) {
    this.teamsToView[event.source.value] = event.source.selected;
    let idsToAdd = [];
    this.events = [];
    //add teams to view to list
    for (let key in this.teamsToView) {
      if (this.teamsToView[key] == true) {
        idsToAdd.push(key);
      }
    }
    //get all approved holiday messages for the selected teams
    this.appService.getAllEmployeesOfPlanningUnitsWithIds(idsToAdd).subscribe((data) => {
      console.log(data);
      for (let employee of data) {
        this.appService.getHolidayMessagesApprovedOfDoctorWithID(employee.member).subscribe((data) => {
          this.convertHolidayMessagesToCalendarEvent(data);
        });
      }
    });
  }


  //convert holiday messages to data that can be displayed in the history component table
  private fillUpdates(data: any): void {
    this.updates = [];
    this.rows = [];
    for (let message of data) {
      let allDates = [];
      for (let dates of message.exactDates) {
        let obj = {date: dates.date, daypart: dates.daypart};
        allDates.push(obj);
      }
      //sort by requestdate
      allDates.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
      let emoji = "";
      let status = "";
      switch (message.state) {
        //link material icons with state
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
      //same procedure for history of the holiday message
      for (let hm of message.history) {
        let allDatesHistory = [];
        for (let dates of hm.exactDates) {
          allDatesHistory.push(dates.date);
        }
        allDatesHistory.sort((a, b) => new Date(a).getTime() - new Date(b).getTime());
        //transform dates to appropriate format
        hm['start'] = this.datePipe.transform(new Date(allDatesHistory[0]), "dd/MM/yyyy HH:mm");
        hm['end'] = this.datePipe.transform(new Date(allDatesHistory[allDatesHistory.length - 1]), "dd/MM/yyyy HH:mm");
        console.log(new Date(hm.requestDate));
        hm['requestDate'] = this.datePipe.transform(new Date(hm.requestDate), "dd/MM/yyyy HH:mm");
        console.log(hm.requestDate);
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
        requestDate: this.datePipe.transform(new Date(message.requestDate), "dd/MM/yyyy HH:mm"),
        start: this.datePipe.transform(new Date(allDates[0]['date']), "dd/MM/yyyy") + " " + allDates[0]['daypart'],
        end: this.datePipe.transform(new Date(allDates[allDates.length - 1]['date']), "dd/MM/yyyy") + " " + allDates[allDates.length - 1]['daypart'],
        type: message.type,
        comment: message.comment,
        state: status,
        history: message.history,
        name: message.name
        //hh:mm dd/MM/yyyy
      });
    }
    //by descending date
    this.updates.reverse();
    //if holiday message has history, add detail row
    for (let element of this.updates) {
      if (element.history.length > 0) {
        this.rows.push(element, {detailRow: true, element});
      } else {
        this.rows.push(element);
      }
    }
  }
}
