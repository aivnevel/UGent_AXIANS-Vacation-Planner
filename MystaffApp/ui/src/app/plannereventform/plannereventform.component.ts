import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbDate, NgbDateNativeAdapter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {AppService} from "../app.service";
import {HolidayMessage} from "../models/HolidayMessage";
import {ExactDate} from "../models/ExactDate";
import {AbsenceCounter} from "../models/AbsenceCounter";
import {TranslateService} from "../translate";

@Component({
  selector: 'app-plannereventform',
  templateUrl: './plannereventform.component.html',
  styleUrls: ['./plannereventform.component.css']
})

/*
The function and purpose is the same as the eventform component, the planner is able to plan an absence for another employee,
this can be done so by filling in the form by choosing the name of the employee and the properties of the absence for
the chosen employee.
The data of the form will then be converted in a holidaymessage and sent to the backend to be stored in the database, during
the creation a holiday structure and the id of the employee will also be emitted to the newsfeed to add the newly
created absence to the request list
 */
export class PlannereventformComponent implements OnInit {

  //Adapter used to convert the NgbDate structure to a Date structure
  adapter: NgbDateNativeAdapter = new NgbDateNativeAdapter();

  //Id of the planner creating the absence, this is provided by the parent component newsfeed
  @Input() plannerid: string;

  //Data of the form
  absenceChoices: string[];
  periodChoices: string[];
  employeeid: string;
  selectedOption: string;
  selectedPeriod: string;
  start: any;
  end: any;
  startdate: Date = new Date();
  enddate: Date;
  comments: string;


  //A list of employees for the planner to choose from
  allMembers: string[];

  //A mapping name <-> id, when the planner has chosen an employee and submits the absence then this dictionary will be
  //used to get the correct id of the employee
  membersIdMapping: { [fullName: string]: string };
  membersIdAbsenceMapping: { [id: string]: any };

  amountPossibleDays: string = "";
  amountAbsencesApproved: string = "";
  amountPotentialAbsences: string = "";

  warning = '';

  possibleHolidayCounters: number[];

  daysChanged: boolean = false;

  //The Eventmitters used to emit data from this component to the newsfeed
  @Output() holidayCreated: EventEmitter<any> = new EventEmitter<any>();
  @Output() employeeOfHoliday: EventEmitter<string> = new EventEmitter<string>();

  //Variable that disables the button after the planner has clicked the submit button once, this is to avoid the possibility
  //that the planner can submit multiple instances of the same absence at once.
  isDisabled: boolean = false;

  selectedAmount = "";
  chosenId;

  //The constructor requests all necessary data from the backend and initializes the dictionary.
  constructor(private appService: AppService, public _translate: TranslateService) {
    this._translate.LanguageChange.on(() => {
      this.buildTranslationMaps();
    });

    this.isDisabledEnd = this.isDisabledEnd.bind(this);
    this.allMembers = new Array();
    this.membersIdMapping = {};
    this.membersIdAbsenceMapping = {};
    let membersidarray = [];
    this.appService.getMembers().subscribe(members => {
      for (let member of members) {
        let fullName = member.firstName + " " + member.lastName;
        this.allMembers.push(fullName);
        this.membersIdMapping[fullName] = member.id;
        membersidarray.push(member.id);
      }
      let d = new Date();
      let total;
      let amountPlanned;
      this.appService.getAbsenceCounterOfEmployees(d.getFullYear(), membersidarray).subscribe(absences => {
        for (let absence of absences.counters) {
          let allCounts = [];
          total = absence.maxDaysPossibleThisYear;
          allCounts.push(total);
          amountPlanned = total - absence.daysLeftApprNewInCons;
          allCounts.push(total - absence.daysLeftApprNewInCons);
          allCounts.push(total - absence.daysLeftApproved);
          this.membersIdAbsenceMapping[absence.employeeID] = allCounts;
        }
        this.possibleHolidayCounters = new Array();
      });
    });
  }

  ngOnInit() {
    //Initializing the values for the period choices and absence options
    this.buildTranslationMaps();
  }

  buildTranslationMaps(): void {
    this.absenceChoices = [this._translate.instant('OPTION_SICKNESS'),
      this._translate.instant('OPTION_ANNUAL_LEAVE'),
      this._translate.instant('OPTION_EUROPEAN_LEAVE'),
      this._translate.instant('OPTION_EDUCATIONAL_LEAVE'),
      this._translate.instant('OPTION_OTHER')];

    this.periodChoices = [this._translate.instant('PERIOD_FULL_DAY'),
      this._translate.instant('PERIOD_MORNING'),
      this._translate.instant('PERIOD_AFTERNOON')];
  }

  //Eventhandler when the button is clicked, it will use all the data filled in the form to create a holidaymessage to be
  //sent to the backend and emit data to the newsfeed so a new instance of the request component can be created in the
  //request list and add it.
  buttonClicked() {

    if (this.formcheck()) {
      this.isDisabled = true;

      if (this.membersIdMapping[this.employeeid]) {

        let aantalUren = Math.abs(this.enddate.getTime() - this.startdate.getTime());
        let aantalDagen = Math.ceil(aantalUren / (1000 * 60 * 60 * 24)) + 1;

        if (this.amountPotentialAbsences + aantalDagen < this.amountPossibleDays || this.selectedOption == this._translate.instant('OPTION_OTHER')) {
          let hm: HolidayMessage = new HolidayMessage();
          hm.employeeID = this.membersIdMapping[this.employeeid];
          hm.requestByID = this.plannerid;
          hm.state = "New";
          hm.requestDate = new Date();

          hm.exactDates = new Array();
          this.employeeOfHoliday.emit(this.membersIdMapping[this.employeeid]);

          let tempdate = new Date(this.startdate);
          let tempenddate = new Date(this.enddate);
          this.enddate = new Date(this.enddate.setDate(this.enddate.getDate() + 1));

          let dp: string;

          if (this.selectedPeriod == this._translate.instant('PERIOD_FULL_DAY')) {
            dp = "entire day";
            while (tempdate < this.enddate) {
              hm.exactDates.push(this.createExactDate(tempdate, "AM"));
              hm.exactDates.push(this.createExactDate(tempdate, "PM"));
              tempdate = new Date(tempdate.setDate(tempdate.getDate() + 1));
            }
          } else if (this.selectedPeriod == this._translate.instant('PERIOD_MORNING')) {
            dp = "AM";
            while (tempdate < this.enddate) {
              hm.exactDates.push(this.createExactDate(tempdate, "AM"));

              tempdate = new Date(tempdate.setDate(tempdate.getDate() + 1));
            }

          } else {
            dp = "PM";
            while (tempdate < this.enddate) {
              hm.exactDates.push(this.createExactDate(tempdate, "PM"));
              tempdate = new Date(tempdate.setDate(tempdate.getDate() + 1));
            }
          }

          if (this.selectedOption == this._translate.instant('OPTION_OTHER')) {
            hm.type = "Other";
          } else if (this.selectedOption == this._translate.instant('OPTION_ANNUAL_LEAVE')) {
            hm.type = "Yearly";
          } else if (this.selectedOption == this._translate.instant('OPTION_EUROPEAN_LEAVE')) {
            hm.type = "European";
          } else if (this.selectedOption == this._translate.instant('OPTION_EDUCATIONAL_LEAVE')) {
            hm.type = "Educative";
          } else {
            hm.type = "Sickness";
          }

          hm.comment = this.comments;

          this.appService.postHolidayMessage(hm).subscribe(id => {
            this.holidayCreated.emit({
              startDateHoliday: this.startdate,
              endDateHoliday: tempenddate,
              type: hm.type,
              comment: this.comments,
              dayPart: dp,
              idMessage: id,
              state: "New",
              vacationName: "",
              requestDate: new Date(),
              lastUpdate: new Date().valueOf(),
              person: null,
              shown: true
            });
            this.clearForm();
            this.isDisabled = false;
          });
        } else {
          this.warning = "Totaal aantal afwezigheidsdagen wordt overgeschreden!"
          this.isDisabled = false;
        }

      } else {
        this.warning = "Gekozen werknemer bestaat niet!";
        this.isDisabled = false;
      }


    } else {
      this.warning = "Niet alle verplichte velden zijn ingevuld!";
      this.isDisabled = false;
    }

  }

  //used during the creation of the holidaymessage, each holidaymessage has a collection of ExactDates, this method creates
  //the ExactDate object according to the input date and the input dayPart,
  //returns a single ExactDate object
  createExactDate(start: Date, dp: string): ExactDate {
    let e1 = new ExactDate();
    e1.dayPart = dp;
    e1.date = new Date(start);
    return e1;
  }

  //Method checks if all required fields in the form are filled in
  formcheck(): boolean {
    return (this.selectedPeriod != "" && this.selectedOption != "" && this.startdate != undefined && this.enddate != undefined && this.employeeid != "");
  }

  //Eventhandler, when a Date has been selected in the NgbDatePickerModule then this NgbDate structure will be converted
  //in a Date
  selectstart(start) {
    //Convert ngbstruct to date
    this.startdate = this.adapter.toModel(start);
  }

  //Same as the above but for the end date of the absence
  selectend(end) {
    //Convert ngbstruct to date
    this.enddate = this.adapter.toModel(end);
  }

  //Will disable all dates that are before the current date, this to prevent the planner from choosing a past date
  isDisabledStart(date: NgbDateStruct) {
    const d = new Date(date.year, date.month - 1, date.day + 1);
    const current = new Date();
    return current > d;
  }

  //Will disable all dates before the chosen start date, this to prevent the planner from choosing a date that is before
  //the startdate of the absence
  isDisabledEnd(date: NgbDateStruct) {
    if (this.start) {
      this.startdate = this.adapter.toModel(this.start);
    }
    const d = new Date(date.year, date.month - 1, date.day + 1);
    return this.startdate > d;
  }

  //Clears all the input data of the form after a holidaymessage has been submitted and the data emitted
  clearForm() {
    this.startdate = null;
    this.start = null;
    this.end = null;
    this.enddate = null;
    this.selectedOption = '';
    this.selectedPeriod = '';
    this.comments = '';
    this.employeeid = '';
    this.amountPossibleDays = '';
    this.amountAbsencesApproved = '';
    this.amountPotentialAbsences = '';
    this.warning = '';
  }


  inputNameChanged(event: any) {
    if (this.membersIdMapping[event]) {
      let id = this.membersIdMapping[event];
      this.chosenId = this.membersIdMapping[event];
      this.amountPossibleDays = this.membersIdAbsenceMapping[id][0];
      this.selectedAmount = this.membersIdAbsenceMapping[id][0];
      this.amountPotentialAbsences = this.membersIdAbsenceMapping[id][1];
      this.amountAbsencesApproved = this.membersIdAbsenceMapping[id][2];
      this.possibleHolidayCounters = new Array();
      for (let i = this.membersIdAbsenceMapping[id][1]; i <= 100; i++) {
        this.possibleHolidayCounters.push(i);
      }
    } else {
      this.amountPossibleDays = '';
      this.amountAbsencesApproved = '';
      this.amountPotentialAbsences = '';
    }
  }

  amountPossibleDaysChanged() {
    this.daysChanged = true;
  }

  amountDaysChangedSubmit() {
    this.daysChanged = false;
    this.amountPossibleDays = this.selectedAmount;
    this.membersIdAbsenceMapping[this.chosenId][0];

    let absenceCounter = new AbsenceCounter();
    absenceCounter.employeeID = this.chosenId;
    absenceCounter.maxDaysThisYear = parseInt(this.amountPossibleDays);
    absenceCounter.lastUpdate = new Date();
    absenceCounter.lastComment = "";

    this.appService.postAbsenceCounter(absenceCounter).subscribe();

  }

}
