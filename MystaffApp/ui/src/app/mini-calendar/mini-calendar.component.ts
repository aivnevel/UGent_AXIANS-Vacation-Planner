import {Component, OnInit, TemplateRef, ViewChild, ViewEncapsulation} from '@angular/core';
import {CalendarEvent, CalendarEventTitleFormatter, CalendarView, DAYS_OF_WEEK,} from 'angular-calendar';
import {Subject} from "rxjs";
import {AppService} from "../app.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DictionaryService} from "../dictionary.service";
import {CustomEventTitleFormatter} from "../calendar/DetailedInfoFormatter";
import {DatePipe} from "@angular/common";
import {TranslateService} from "../translate";
import {SessionStorageService} from "../sessionstorage.service";


//Const of the colors used in minicalendar
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
    primary: '#A9A9A9',
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

export interface miniCalendarEvent extends CalendarEvent {
  employeeID?: string;
  comments?: string;
  employeeName?: string;
  maticon?: string;
}

@Component({
  selector: 'app-mini-calendar',
  encapsulation: ViewEncapsulation.None,
  templateUrl: './mini-calendar.component.html',
  styleUrls: ['./mini-calendar.component.css'],
  providers: [{
    provide: CalendarEventTitleFormatter,
    useClass: CustomEventTitleFormatter
  }, DatePipe, SessionStorageService]
})

/*

The minicalendar component uses the Angular-Calendar library, this mini calendar is the calendar component shown as a child
component in the newsfeed component. This component requests all approved holidaymessages in the backend and transforms those
holidaymessages to the miniCalendarEvent interface which extends from the CalendarEvent interface. These events are then
stored in the events array to be shown in the calendar itself.

The minicalendar component also requests data from the backend concerning how many people are necessary on each working day
and has functions that are exposed to the newsfeed component to be called, these functions receive data like id's from the
newsfeed component to filter the events and numbers to be shown.

 */

export class MiniCalendarComponent implements OnInit {
  //This is the child popupcomponent when the name of an absence is clicked this popup will be shown, the popup
  //properties itself are declared in the ngTemplate 'daypopup' in mini-calendar.component.html
  @ViewChild('daypopup') daypopup: TemplateRef<any>;

  private readonly onReady = new LiteEvent<void>();

  refresh: Subject<any> = new Subject();

  //view is a variable that keeps track of whether the calendar is on month/week/day view
  view: CalendarView = CalendarView.Month;

  CalendarView = CalendarView;

  //Two variables that keep track whether the black bar underneath the days are open or closed
  activeDayIsOpen: boolean = false;
  activeDayIsOpen2: boolean = false;

  //The events array is the array that the calendar reads from to fill it up
  events = [];

  //Used to filter out the events that are not necessary to be shown, these events are not deleted but stored in the
  //hiddenevents array, when the absences are added it will scan through the hiddenevents array to put them back in the
  //events array
  hiddenevents = [];

  //Dictionary that holds the date and is mapped on the the data that is used to determine how many people are needed
  //for that specfic date and the min/max amount of people that can be absent on that day
  dict2: { [puid: string]: any; } = {};

  //False until all the data related to fill up the above dict is loaded
  isLoaded: boolean = false;

  //keeps track of the current userId
  userid: any;

  //Used for the newsfeed component to indicate which shift number data has to be shown in the calendar date cells
  //Viewdate is used by the calendar to know on what date to make its decisions on, it is for example used to show the
  //month of the viewdate
  viewDate: Date = new Date();

  //Same functionality as the viewDate, but for the second calendar
  viewSecondDate: Date = new Date(new Date().setMonth(new Date().getMonth() + 1));

  //Indicates on what day the week starts on
  weekStartsOn: number = DAYS_OF_WEEK.MONDAY;

  membersIdMapping: { [id: string]: string };

  isPlanner: boolean = false;

  dataLoaded: boolean = false;

  allpus: string[];
  currentpus: string = "0";

  amountOfPusOfPlanner: number;

  locale: string;

  //The constructor loads all the necessary data from the appService component, these data include the holidaymessages,
  //the official holidays and the numbers for required staff, min/max reserve
  constructor(private modal: NgbModal, private appService: AppService, public dictService: DictionaryService, private datePipe: DatePipe, private sessionStorage: SessionStorageService, public _translate: TranslateService) {
    this.locale = _translate.currentLang;
    _translate.LanguageChange.on(() => {
      this.locale = _translate.currentLang;
      this.activeDayIsOpen = false;
      this.activeDayIsOpen2 = false;
    });

    if (this.appService.isAuthenticated()) {
      (async () => {
        let makeCalendarEvents = () => {
          this.appService.getHolidaysApproved().subscribe(data => {
            this.convertHolidayMessagesToCalendarEvent(data);
          });
        };
        if (!this.dictService.dataLoaded) {
          this.dictService.Ready.on(makeCalendarEvents);
        } else {
          makeCalendarEvents();
        }
      })();
      this.membersIdMapping = {};
      this.appService.getMembers().subscribe(members => {
        for (let member of members) {
          let fullName = member.firstName + " " + member.lastName;
          this.membersIdMapping[member.id] = fullName;
        }
      });
      this.appService.getOrganizationHolidays().subscribe(holidays => {
        this.addOrganizationHolidays(holidays);
      });

      this.userid = this.sessionStorage.getUser().userId;
      this.appService.getPlanningUnitsOfPlanner(this.userid).subscribe(pus => {
        if (pus.length > 0) {
          this.isPlanner = true;
          this.allpus = pus;
          this.amountOfPusOfPlanner = pus.length;
          this.loadAvailablePeoplePerDagpartForPlanningUnits(pus);
        }
      });

    }
  }

  public get Ready() {
    return this.onReady.expose();
  }

  //Unused
  ngOnInit() {

  }

  //This function loads all the data that is used related to the required amount of staff needed each day, how many
  //can be absent for each day
  //It determines the first and last day range that it has to request data from, in this case it is the first day of the
  //of the month of the upper calendar and the last day of the month of the lower calendar
  //The data is then stored in the dictionary called dict, with the date the key and an array of Morning/Afternoon shifts
  //Returns nothing, sets the isLoaded variable on true
  loadAvailablePeoplePerDagpartForPlanningUnits(pus: string[]) {
    this.isLoaded = false;
    let firstDay = this.datePipe.transform(new Date(this.viewDate.getFullYear(), this.viewDate.getMonth(), 1), "yyyy-MM-dd");
    let lastDate = this.datePipe.transform(new Date(this.viewSecondDate.getFullYear(), this.viewSecondDate.getMonth() + 1, 0), "yyyy-MM-dd");


      this.appService.getAvailablePeoplePerDayPartForPlanningUnits(firstDay, lastDate, pus).subscribe(data => {
        let dict;
          if(this.dict2["0"] && pus.length > 1){
            dict = this.dict2["0"];
          }
          else if(this.dict2[pus[0]]){
            dict = this.dict2[pus[0]];
          }
          else {
            dict = {};
          }
        for (let day of data) {
          if (dict[day.date]) {
            if(dict[day.date].length < 2){
              dict[day.date].push(day);
            }
          } else {
            dict[day.date] = new Array();
            dict[day.date].push(day);
          }
        }
        if(pus.length > 1 || this.amountOfPusOfPlanner == 1){
          this.dict2["0"] = dict;
        }
        else {
          this.dict2[pus[0]] = dict;
        }
        this.isLoaded = true;
      });


  }

  //The MonthClicked function sets the viewSecondDate variable on a date that is one month further than the viewDate
  //Variable so that the lower calendar shows the month after the upper calendar
  //Method then refreshes the calendar component
  //No return value
  MonthClicked(): void {
    this.viewSecondDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1);

    let fViewDate = this.datePipe.transform(this.viewDate, "yyyy-MM-dd");
    let fViewDate2 = this.datePipe.transform(this.viewSecondDate, "yyyy-MM-dd");
    if (!this.dict2[this.currentpus][fViewDate] || !this.dict2[this.currentpus][fViewDate2]) {
      if (this.currentpus != "0") {

        this.loadAvailablePeoplePerDagpartForPlanningUnits([this.currentpus]);
      } else {

        this.loadAvailablePeoplePerDagpartForPlanningUnits(this.allpus);
      }
    }

    this.refresh.next();
  }

  //Converts the holidaymessage structure to the CalendarAbsence class structure
  //Returns nothing, directly stores the CalendarAbsences in the events array
  convertHolidayMessagesToCalendarEvent(data: any) {
    for (let hm of data) {
      let i = 0;
      while (i < hm.exactDates.length) {
        let e = hm.exactDates[i];
        let start = e.date;
        let daypart = e.daypart;
        let event: miniCalendarEvent;

        if (hm.exactDates[i + 1]) {
          let end = hm.exactDates[i + 1].date;
          if (start[0] == end[0] && start[1] == end[1] && start[2] == end[2]) {
            let d1 = new Date(start[0], start[1] - 1, start[2]);
            let d1copy = new Date(start[0], start[1] - 1, start[2]);
            event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 17, hm.id, hm.type);
            i += 2;
          } else {
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
          let d1 = new Date(start[0], start[1] - 1, start[2]);
          let d1copy = new Date(start[0], start[1] - 1, start[2]);
          if (daypart == "PM") {
            event = this.convertDatesToCalendarEvent(d1, d1copy, 13, 17, hm.id, hm.type);
          } else {
            event = this.convertDatesToCalendarEvent(d1, d1copy, 9, 13, hm.id, hm.type);

          }
          i += 1;
        }
        event.comments = hm.comment;
        event.employeeID = hm.employeeID;
        event.employeeName = this.dictService.getCacheEmployeeFirstNameOfId(hm.employeeID) + " " + this.dictService.getCacheEmployeeLastNameOfId(hm.employeeID);
        this.events.push(event);
      }
    }
    this.dataLoaded = true;
    this.onReady.trigger();
    this.refresh.next();
  }

  //This function is used by the convertHolidaymessagesToCalendarEvents
  //The paramaters of this function are: the date of the day used to fill in the start hour, the date of the day used to fill in the end hour,
  //The start and end hours, the id of the absence and the reason of absence
  //Return value is a CalendarEvent with the properties of the input parameters
  convertDatesToCalendarEvent(start: Date, end: Date, h1: number, h2: number, uid: string, title: string): miniCalendarEvent {

    let color, icon;
    if (title == "YEARLY_VACATION" || title == "Yearly") {
      title = "OPTION_ANNUAL_LEAVE";
      color = colors.yellow;
      icon = "flight_takeoff";
    } else if (title == "EUROPEAN_VACATION" || title == "European") {
      title = "OPTION_EUROPEAN_LEAVE";
      color = colors.orange;
      icon = "account_balance";
    } else if (title == "EDUCATIONAL_VACATION" || title == "Educative") {
      title = "OPTION_EDUCATIONAL_LEAVE";
      color = colors.red;
      icon = "school";
    } else if (title == "Other") {
      title = "OPTION_OTHER";
      color = colors.green
      icon = "live_help";
    } else if (title == "Sickness") {
      title = "OPTION_SICKNESS";
      color = colors.gray;
      icon = "healing"
    }

    start.setHours(h1);
    end.setHours(h2);
    return {id: uid, start: new Date(start), end: new Date(end), title: title, color: color, maticon: icon};
  }

  //Converts the structure of the OrganizationHolidays of axians to a CalendarEvent with the same properties
  //Returns nothing, creates a CalendarEvent that is then stored in the events array
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

  day: any;
  //Handles the click event on a day, if a day is clicked then a black info bar underneath the day will be opened
  //If the bar is open on click, then the bar will be closed, same if the bar is initially closed
  //No return value, the clicked day will also be saved in the day variable
  handleEvent(action: string, day: any) {
    console.log(day.badgeTotal);
    this.day = day;
    if (this.viewDate != day.date && this.activeDayIsOpen) {
      this.activeDayIsOpen = false;
    } else {
      this.viewDate = day.date;
      if (day.events.length > 0) {
        if (this.activeDayIsOpen) {
          this.activeDayIsOpen = false;
        } else {
          this.activeDayIsOpen = true;
        }
      }
    }
  }

  //Same functionality as the one above, but for the lower calendar
  handleEvent2(action: string, day: any) {
    this.day = day;
    if (this.viewSecondDate != day.date && this.activeDayIsOpen2) {
      this.activeDayIsOpen2 = false;
    } else {
      this.viewSecondDate = day.date;
      if (day.events.length > 0) {
        if (this.activeDayIsOpen2) {
          this.activeDayIsOpen2 = false;
        } else {
          this.activeDayIsOpen2 = true;
        }
      }
    }
  }

  //This event will show a popup window, this popup window is the ngTemplate daypopup, it utilizes the day variable
  //That is initialized in the handleEvent function
  //The popupwindow contains the required amount of staff for that day, the min and max amount of people that can be absent
  //A list of people having approved absences are also shown on the popup window
  showEvent() {
    this.modal.open(this.daypopup, {size: 'lg'});
  }

  //This function is used by the upper calendar to determine whether an absence is in the morning/afternoon or the whole
  //day, by using this function the calendar component itself can determine whether to show the absence in the morning
  //part of the day, the afternoon part of the day or in both if the absence takes a whole day
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

  //Same functionality as the function above but this function applies for the lower calendar
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

  //Function that is called by the newsfeed, the input parameter is an array of id's of the absences that have to be
  //shown in the calendar, this function calls the moveAbsencesFromHiddenToVisible function that moves all CalendarEvents
  //with the given id from the hiddenevent array to the event array
  //No return value
  addAbsences(ids: any): void {
    for (let id of ids) {
      this.moveAbsencesFromHiddenToVisible(id);
    }
    this.refresh.next();
  }

  //Moves for the given id all calendarevents with that id from the hiddenevents array to the events array
  private moveAbsencesFromHiddenToVisible(id: any) {
    for (let i = this.hiddenevents.length - 1; i >= 0; i--) {
      if (this.hiddenevents[i].id == id) {
        this.events.push(this.hiddenevents[i]);
        this.hiddenevents.splice(i, 1);
      }
    }
  }

  //Function that is called by the newsfeed component, input parameter is an array of id's of absences that have to be
  //removed from sight in the calendar, this function calls the deleteAbsencesFromCalendar function that moves the calendarEvents
  //With the given id to the hiddenevents array
  //No return value
  deleteAbsences(ids: any) {
    for (let id of ids) {
      this.deleteAbsencesFromCalendar(id);
    }
    this.refresh.next();
  }

  //For the given id, it will move all CalendarEvents with the same id from the events array to the hiddenevents array
  //No return value
  private deleteAbsencesFromCalendar(id: any) {
    for (let i = this.events.length - 1; i >= 0; i--) {
      if (this.events[i].id == id) {
        this.hiddenevents.push(this.events[i]);
        this.events.splice(i, 1);
      }
    }
  }


  //Function used by the newsfeed to jump to a certain month and month +1, the input parameter is a number that indicates
  //which pair of months to jump to
  //The function will then load for the pair of months the required data such as the required amount of staff needed for
  //each day of the two months
  filterMonths(maand: number) {
    switch (maand) {
      case 1:
        this.viewDate = new Date(this.viewDate.setMonth(0));
        break;

      case 2:
        this.viewDate = new Date(this.viewDate.setMonth(2));
        break;

      case 3:
        this.viewDate = new Date(this.viewDate.setMonth(4));
        break;

      case 4:
        this.viewDate = new Date(this.viewDate.setMonth(6));
        break;

      case 5:
        this.viewDate = new Date(this.viewDate.setMonth(8));
        break;

      case 6:
        this.viewDate = new Date(this.viewDate.setMonth(10));
        break;
    }
    this.viewSecondDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1);
    if (this.currentpus != "0") {
      this.loadAvailablePeoplePerDagpartForPlanningUnits([this.currentpus]);
    } else {
      this.loadAvailablePeoplePerDagpartForPlanningUnits(this.allpus);
    }
    this.refresh.next();
  }

  //Function called by the newsfeed that passes a number, this number indicates which shift values to show on the
  //calendar days
  filterPu(shiftnummer: string) {
    this.currentpus = shiftnummer;
    if (this.currentpus != "0") {
      this.loadAvailablePeoplePerDagpartForPlanningUnits([this.currentpus]);
    } else {
      this.loadAvailablePeoplePerDagpartForPlanningUnits(this.allpus);
    }
    this.refresh.next();
  }


  //Returns the right value to be displayed in the calendar days, the date is the date of the day, the position
  //determines if it is the data of the morning part of afternoon part, the requested type determnes which data is needed,
  //this can be either the totalNumberNeeded, the min reserve or the max reserve, the shift parameter indicates which shift
  //to display the values of, by default this value is 0 which displays the overall values of that day
  showAvailablePeoplePerDay(date: Date, position: number, requestedtype: number, shift: string): string {
    let formatted = this.datePipe.transform(date, "yyyy-MM-dd");
    let dict;
    if(this.amountOfPusOfPlanner > 1){
      dict = this.dict2[shift];
    }
    else {
      dict = this.dict2["0"];
    }
    if (dict[formatted]) {
      let day = dict[formatted][position];
      if (requestedtype == 0) {
        return day.totalNumberNeeded;
      } else if (requestedtype == 1) {
        return day.totalMinReserve;
      } else {
        return day.totalMaxReserve;
      }
    } else {
      return "";
    }
  }

  giveShiftsOfGivenDay(day: Date, position: number) {
    let formatted = this.datePipe.transform(day, "yyyy-MM-dd");
    let dict;
    if(this.amountOfPusOfPlanner > 1){
      dict = this.dict2[this.currentpus];
    }
    else {
      dict = this.dict2["0"];
    }
    if (dict[formatted]) {
      return dict[formatted][position]['shifts'];
    } else {
      return [];
    }
  }

  showSuggestions(ids: string[]) {
    let newArray = [];
    for (let id of ids) {
      newArray.push(this.membersIdMapping[id]);
    }
    return newArray;
  }
}

interface ILiteEvent<T> {
  on(handler: { (data?: T): void }): void;

  off(handler: { (data?: T): void }): void;
}

class LiteEvent<T> implements ILiteEvent<T> {
  private handlers: { (data?: T): void; }[] = [];

  public on(handler: { (data?: T): void }): void {
    this.handlers.push(handler);
  }

  public off(handler: { (data?: T): void }): void {
    this.handlers = this.handlers.filter(h => h !== handler);
  }

  public trigger(data?: T) {
    this.handlers.slice(0).forEach(h => h(data));
  }

  public expose(): ILiteEvent<T> {
    return this;
  }
}
