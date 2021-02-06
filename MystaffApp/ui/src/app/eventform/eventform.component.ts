import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  CalendarEvent
} from 'angular-calendar';
import {NgbDate, NgbDateNativeAdapter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {AppService} from "../app.service";
import {HolidayMessage} from "../models/HolidayMessage";
import {ExactDate} from "../models/ExactDate";
import {CalendarAbsence} from "../calendar/calendar.component";
import {TranslateService} from "../translate";
import {isLineBreak} from "codelyzer/angular/sourceMappingVisitor";


//Const colors used
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
  }
};

@Component({
  selector: 'app-eventform',
  templateUrl: './eventform.component.html',
  /*template: `    `,*/
  styleUrls: ['./eventform.component.css']
})

//Eventform component for submitting new absences
export class EventformComponent implements OnInit {

  //Adapter to convert NgbStruct <-> Date
  adapter: NgbDateNativeAdapter = new NgbDateNativeAdapter();

  //Collection of possible reasons for submitting an absence, these are added in the constructor
  absenceChoiceMapper = new Map<string, string>();

  //Collection of possible periods for absences -> Afternoon/Morning
  periodChoiceMapper = new Map<string, string>();

  //List of CalendarEvents to be submitted to the database and the calendar component
  calenderEvents = [];

  //Start date that appears if it has a value
  @Input() start: NgbDate;

  //End date that appears if it has a value
  @Input() end: NgbDate;

  //The selected option and period
  @Input() selectedOption: string;
  @Input() selectedPeriod: string;

  //Empty as long as user filled all necessary fields, or else it is used to show the warning message
  warning: string;

  //Comments value field
  @Input() comments: string;

  //id that is currently being edited and boolean that determines whether the user is in edit mode or not
  @Input() edit: boolean = false;
  @Input() editid: any;

  @Input() isCalendar = true;

  startdate: Date;
  enddate: Date;
  tempdate: Date;
  event: CalendarEvent;

  amountPossibleDays: number;
  amountAbsencesApproved: number;
  amountPotentialAbsences: number;

  userid: any;
  currentyear: number;

  s: Date;
  e: Date;

  isDisabled: boolean = false;

  //Submits all the information to the calendar when the submit button is clicked
  @Output() absenceSubmitted: EventEmitter<CalendarEvent[]> = new EventEmitter<CalendarEvent[]>();

  //Constructor
  constructor(private appService: AppService, public _translate: TranslateService) {
    //Checks all dates whether it is clickable and editable or not, all dates before the current date will
    //Not be clickable
    this.isDisabledEnd = this.isDisabledEnd.bind(this);

    _translate.LanguageChange.on(() => {
      if (this.warning != '') {
        this.warning = this._translate.instant('FORM_WARNING_FILL_IN_FIELD');
      }

      let previous = '';
      this.absenceChoiceMapper.forEach((value, key) => {
        if (key == this.selectedOption) {
          previous = value;
        }
      });

      this.absenceChoiceMapper.clear();
      this.absenceChoiceMapper.set(this._translate.instant('OPTION_SICKNESS'), 'Ziekte')
        .set(this._translate.instant('OPTION_ANNUAL_LEAVE'), 'Jaarlijks verlof')
        .set(this._translate.instant('OPTION_EUROPEAN_LEAVE'), 'Europees verlof')
        .set(this._translate.instant('OPTION_EDUCATIONAL_LEAVE'), 'Educatief verlof')
        .set(this._translate.instant('OPTION_OTHER'), 'Andere');

      this.absenceChoiceMapper.forEach((value, key) => {
        if (value == previous) {
          this.selectedOption = key;
        }
      });

      this.periodChoiceMapper.forEach((value, key) => {
        if (key == this.selectedPeriod) {
          previous = value;
        }
      });
      this.periodChoiceMapper.clear();
      this.periodChoiceMapper.set(this._translate.instant('PERIOD_FULL_DAY'), 'Hele dag')
        .set(this._translate.instant('PERIOD_MORNING'), 'Voormiddag')
        .set(this._translate.instant('PERIOD_AFTERNOON'), 'Namiddag');

      this.periodChoiceMapper.forEach((value, key) => {
        if (value == previous) {
          this.selectedPeriod = key;
        }
      });

      if (this.selectedOption == undefined) {
        this.selectedOption = this._translate.instant('OPTION_SICKNESS');
      }

      if (this.selectedPeriod == undefined) {
        this.selectedPeriod = this._translate.instant('PERIOD_FULL_DAY');
      }

      console.log(this.selectedOption);
      console.log(this.selectedPeriod);

    });

    this.appService.getUser().subscribe(data => {
      this.currentyear = (new Date()).getFullYear();
      this.userid = data.userId;
      this.appService.getAbsenceCounterOfEmployees(this.currentyear, [data.userId]).subscribe(data => {
        this.amountPossibleDays = data.counters[0].maxDaysPossibleThisYear;
        this.amountPotentialAbsences = this.amountPossibleDays - data.counters[0].daysLeftApprNewInCons;
        this.amountAbsencesApproved = this.amountPossibleDays - data.counters[0].daysLeftApproved;
      });
    });
  }

  ngOnInit() {

    //Initializing the values for the period choices and absence options
    this.absenceChoiceMapper.set(this._translate.instant('OPTION_SICKNESS'), 'Ziekte')
      .set(this._translate.instant('OPTION_ANNUAL_LEAVE'), 'Jaarlijks verlof')
      .set(this._translate.instant('OPTION_EUROPEAN_LEAVE'), 'Europees verlof')
      .set(this._translate.instant('OPTION_EDUCATIONAL_LEAVE'), 'Educatief verlof')
      .set(this._translate.instant('OPTION_OTHER'), 'Andere');

    this.periodChoiceMapper.set(this._translate.instant('PERIOD_FULL_DAY'), 'Hele dag')
      .set(this._translate.instant('PERIOD_MORNING'), 'Voormiddag')
      .set(this._translate.instant('PERIOD_AFTERNOON'), 'Namiddag');

    this.isDisabledEnd.bind(this);
    this.warning = '';

    let ele = document.getElementById("all");
    if (!this.isCalendar) {
      ele.className = "all-newsfeed";
    } else {
      ele.className = "all"
    }
  }

  //Input event handler begin date of absence
  selectstart(start) {
    //Convert ngbstruct to date
    this.startdate = this.adapter.toModel(start);
  }

  //Input event handler end date of absence
  selectend(end) {
    //Convert ngbstruct to date
    this.enddate = this.adapter.toModel(end);
  }

  buttonClicked() {
    this.createOrUpdateAbsence();
  }

  //Creates absences after the submit button has been pressed
  createOrUpdateAbsence() {
    if (this.start) {
      //If the start and end date are filled in by the @input() field
      this.startdate = this.adapter.toModel(this.start);
    }

    if (this.end) {
      this.enddate = this.adapter.toModel(this.end);
    }
    if (this.formCheck()) {

      this.s = new Date(this.startdate);
      this.e = new Date(this.enddate);
      this.tempdate = new Date(this.startdate);
      let aantalUren = Math.abs(this.enddate.getTime() - this.startdate.getTime());
      let aantalDagen = Math.ceil(aantalUren / (1000 * 60 * 60 * 24)) + 1;

      if (aantalDagen + this.amountPotentialAbsences <= this.amountPossibleDays || this.selectedOption == this.absenceChoiceMapper.get(this._translate.instant('OPTION_OTHER'))) {
        this.isDisabled = true;
        if (this.periodChoiceMapper.get(this.selectedPeriod) == "Hele dag") {
          //Pushes a calendarevent that has the properties of a whole day
          for (let b = 0; b < aantalDagen; b++) {
            this.calenderEvents.push(this.convertDatesToCalendarEvent(this.startdate, this.tempdate, 9, 17, ""));
            this.startdate.setDate(this.startdate.getDate() + 1);
            this.tempdate.setDate(this.tempdate.getDate() + 1);
          }
        } else if (this.periodChoiceMapper.get(this.selectedPeriod) == "Voormiddag") {
          for (let b = 0; b < aantalDagen; b++) {
            //Pushes a calendarevent that has the properties of a morning absence
            this.calenderEvents.push(this.convertDatesToCalendarEvent(this.startdate, this.tempdate, 9, 13, ""));
            this.startdate.setDate(this.startdate.getDate() + 1);
            this.tempdate.setDate(this.tempdate.getDate() + 1);
          }
        } else {
          for (let b = 0; b < aantalDagen; b++) {
            //Pushes a calendarevent that has the properties of a afternoon absence
            this.calenderEvents.push(this.convertDatesToCalendarEvent(this.startdate, this.tempdate, 13, 17, ""));
            this.startdate.setDate(this.startdate.getDate() + 1);
            this.tempdate.setDate(this.tempdate.getDate() + 1);
          }
        }
        if (this.edit) {
          for (let i = 0; i < this.calenderEvents.length; i++) {
            //If in edit mode, then we will provide the id of the absence that is being edited, so it will be modified
            this.calenderEvents[i].id = this.editid;
          }
        }

        this.createHolidayMessage(this.calenderEvents, this.selectedPeriod);
        if (this.isCalendar) {
          this.absenceSubmitted.emit(this.calenderEvents);
        }
        //Clear list so a new absence can be submitted
        this.calenderEvents = [];
        this.warning = "";
      } else {
        let a = this.amountPotentialAbsences + aantalDagen;
        this.warning = this._translate.instant('FORM_WARNING_LIMIT_REACHED') + this.amountPossibleDays + " < " + a;
        this.isDisabled = false;
      }


    } else {
      //Warning message if not all required fields are filled in

      this.warning = this._translate.instant('FORM_WARNING_FILL_IN_FIELD');
      this.isDisabled = false;
    }
  }

  clearForm() {
    this.startdate = null;
    this.start = null;
    this.end = null;
    this.enddate = null;
    //this.selectedOption = '';
    //this.selectedPeriod = '';
    this.comments = '';
    this.isDisabled = false;
    this.warning = '';
  }


  //Disables all dates that are earlier than the current date
  isDisabledStart(date: NgbDateStruct) {
    const d = new Date(date.year, date.month - 1, date.day + 1);
    const current = new Date();
    return current > d;
  }

  //Converts a given Date with extra information to a CalenderAbsence object that is pushed in the array where the
  //Calendar component reads from
  convertDatesToCalendarEvent(start: Date, end: Date, h1: number, h2: number, uid: string): CalendarAbsence {
    start.setHours(h1);
    end.setHours(h2);
    return {
      id: uid,
      start: new Date(start),
      end: new Date(end),
      title: this.toOptionName(this.absenceChoiceMapper.get(this.selectedOption)),
      meta: this.comments,
      color: colors.yellow,
      symbol: "⏳",
      state: 'EVENT_STATE_NEW',
      s: this.s,
      e: this.e,
      edited: this.edit
    };
  }

  //Checks if all the required fields are filled in
  formCheck(): boolean {
    console.log(this.selectedOption);
    console.log(this.selectedPeriod);
    return (this.selectedPeriod != undefined && this.selectedOption != undefined && this.startdate != undefined && this.enddate != undefined);
  }

  isDisabledEnd(date: NgbDateStruct) {
    if (this.start) {
      this.startdate = this.adapter.toModel(this.start);
    }
    const d = new Date(date.year, date.month - 1, date.day + 1);
    return this.startdate > d;
  }

  //Creates a holidaymessage from data filled and sends it to the backend/db
  createHolidayMessage(events: CalendarEvent[], periode: string) {

    this.appService.getUser().subscribe((data) => {

      let hm = new HolidayMessage();
      let exactDates = [];
      hm.comment = events[0].meta;
      hm.employeeID = data.userId;
      hm.requestByID = data.userId;
      hm.requestDate = new Date();
      hm.state = "New";
      if (this.absenceChoiceMapper.get(this.selectedOption) == 'Andere') {
        hm.type = "Other";
      } else if (this.absenceChoiceMapper.get(this.selectedOption) == 'Jaarlijks verlof') {
        hm.type = "Yearly";
      } else if (this.absenceChoiceMapper.get(this.selectedOption) == 'Europees verlof') {
        hm.type = "European";
      } else if (this.absenceChoiceMapper.get(this.selectedOption) == 'Educatief verlof') {
        hm.type = "Educative";
      } else {
        hm.type = "Sickness";
      }
      for (let event of events) {
        if (this.periodChoiceMapper.get(periode) === "Hele dag") {
          let d1 = new ExactDate();
          let d2 = new ExactDate();
          d1.date = new Date(event.start);
          d2.date = new Date(event.end);
          d1.dayPart = "AM";
          d2.dayPart = "PM";
          exactDates.push(d1, d2);
        } else if (this.periodChoiceMapper.get(periode) === "Voormiddag") {
          let d1 = new ExactDate();
          d1.date = new Date(event.start);
          d1.dayPart = "AM";
          exactDates.push(d1);
        } else {
          let d1 = new ExactDate();
          d1.date = new Date(event.start);
          d1.dayPart = "PM";
          exactDates.push(d1);
        }
      }
      if (this.edit) {
        for (let event of events) {
          hm.id = this.editid;
          event.id = this.editid;
        }
      }
      hm.exactDates = exactDates;
      this.appService.postHolidayMessage(hm).subscribe((data) => {
        if (!this.edit) {
          for (let event of events) {
            event.id = data;
          }
        }
        this.edit = false;
        this.isDisabled = false;
        this.editid = "";
        this.clearForm();

        this.appService.getAbsenceCounterOfEmployees(this.currentyear, [this.userid]).subscribe(data => {
          this.amountPossibleDays = data.counters[0].maxDaysPossibleThisYear;
          this.amountPotentialAbsences = this.amountPossibleDays - data.counters[0].daysLeftApprNewInCons;
          this.amountAbsencesApproved = this.amountPossibleDays - data.counters[0].daysLeftApproved;
        });
      })
    });
  }

  public toOptionName(s:string): string {
    if (s == 'Ziekte') {
      return 'OPTION_SICKNESS';
    } else if (s == 'Europees verlof') {
      return 'OPTION_EUROPEAN_LEAVE';
    } else if (s == 'Educatief verlof') {
      return 'OPTION_EDUCATIONAL_LEAVE';
    } else if (s == 'Jaarlijks verlof') {
      return 'OPTION_ANNUAL_LEAVE';
    } else {
      return 'OPTION_OTHER';
    }
  }

  public getAbsenceKey(s: string): string {
    let res = s;
    this.absenceChoiceMapper.forEach((value, key) => {
      if (s == value) {
        res = key;
        return;
      }
    });
    return res;
  }
}
