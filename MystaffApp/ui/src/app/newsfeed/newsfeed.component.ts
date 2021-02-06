import {
  Component,
  OnInit,
  QueryList,
  TemplateRef,
  ViewChild,
  ViewChildren,
  Renderer2, ComponentFactoryResolver, ViewContainerRef, ElementRef
} from '@angular/core';
import {AppService} from "../app.service";
import {DictionaryService} from "../dictionary.service";
import {Router, NavigationStart} from "@angular/router";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Subject} from "rxjs";
import {MiniCalendarComponent} from "../mini-calendar/mini-calendar.component";
import {Person} from "src/app/models/Person";
import {DatePipe} from "@angular/common";
import {HolidayPlanningsMessage} from "../models/HolidayPlanningsMessage";
import {TooltipPosition} from "@angular/material";
import {Settings} from "../models/Settings";
import {PersonDate} from "../models/PersonDate";
import {RequestsComponent} from "../requests/requests.component";
import {TranslateService} from "../translate";

declare var $: any;

@Component({
  selector: 'app-newsfeed',
  templateUrl: './newsfeed.component.html',
  styleUrls: ['./newsfeed.component.css'],
  providers: [DatePipe]
})
export class NewsfeedComponent implements OnInit {
  @ViewChildren(MiniCalendarComponent) minicalendars: QueryList<MiniCalendarComponent>;
  @ViewChildren(RequestsComponent) requests: QueryList<RequestsComponent>;
  @ViewChild('modalContent') modalContent: TemplateRef<any>;
  @ViewChild('helpWindow') helpWindow: TemplateRef<any>;
  @ViewChild('mincal') mincal: MiniCalendarComponent;
  firstName: string;
  lastName: string;
  type: string;
  date: string;
  dayPart: string;
  inputname: string;
  visible_skills: string[];
  visible_locations: string[];
  visible_vacationTypes: string[];
  visible_sorts: string[];
  visible_dayparts: string[];
  sortType: string;
  message: string;
  Requestfilters: boolean;
  idemp: string;
  AllHolidayMessagesCompact: any[];
  requestFilters: boolean;
  currentPlanningsUnitName: string;

  toggle = false;
  persons = [];
  personsTest = [];
  favorites = [];
  testRequests = [];
  sorts = ["Datum ↓", "Datum ↑", "Achternaam ↓", "Achternaam ↑", "Voornaam ↓", "Voornaam ↑", "skill-naam ↓", "skill-naam ↑"];
  vacationTypes = ["Krokusvakantie", "Zomervakantie", "Kerstvakantie", "Herfstvakantie", "Paasvakantie"];
  skills = [];
  locations = [];
  AMPM = ["Voormiddag", "Namiddag", "Werkdag"];
  AMPM_alt = {
    "Voormiddag": false,
    "Namiddag": false,
    "Werkdag": false,
  };
  numberSkills = 5;
  numberLocations = 5;
  numbervacationTypes = 5;
  numberSorts = 5;
  searchAll = ["voormiddag", "namiddag", "vm", "nm", "werkdag"];
  filteredRequests = [];
  planningsUnit = [];
  planningsUnitIDs = [];
  allPlanningsUnits = [];
  knownPlanningsUnitIDs = new Array();
  knownPlanningsUnitIDNames = new Array();
  holidayInConsideration = new Array();
  holidayNew = new Array();
  planningsUnitSkills = new Array();
  planningsUnitLocations = new Array();

  allPUList = [];
  selectedPU = {};
  visiblePU = [];
  numberPU = 5;

  //standard values checkbox
  selectedSkills = {};
  selectedLocation = {};
  selectedMonths = {
    "January": false,
    "February": false,
    "March": false,
    "April": false,
    "May": false,
    "June": false,
    "July": false,
    "August": false,
    "September": false,
    "October": false,
    "November": false,
    "December": false,
  };
  selectedHolidays = {};
  holidayTypes = ["Jaarlijks", "Europees", "Educatief", "Ziekte", "Andere"];

  commentTypes = [];
  selectedComments = {
    "Ja": false,
    "Neen": false
  };

  selectedPeriods = {
    //???
  };
  selectedAMPM = {
    "AM": false,
    "PM": false,
    "entire day": false,
  };

  filteredDoctors = this.persons;
  filteredAbsences = [];
  hm = [];
  mode;
  events = [];

  plannerMode: boolean = false;
  isPlanner: boolean = false;

  collisionstart: Date = new Date();
  collisionend: Date = new Date();

  collisions: { [id: string]: string[] };
  settings: Settings;

  position: TooltipPosition = "left";

  routeSub: any;

  isSometimesRequestsDisabled = false;

  // Maps
  sortByMap = new Map<string, string>();
  daypartMap = new Map<string, string>();
  absenceTypeMap = new Map<string, string>();
  holidayTypeMap = new Map<string, string>();

  approvedCollisions = {};

  constructor(private appService: AppService, private router: Router, private modal: NgbModal,
              private dictionaryService: DictionaryService, private datePipe: DatePipe, private rd: Renderer2,
              private resolver: ComponentFactoryResolver, public _translate: TranslateService) {

    this._translate.LanguageChange.on(() => {
      this.buildTranslationsMaps();
    });

    this.collisions = {};
    if (!this.appService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    } else {
      this.AllHolidayMessagesCompact = new Array();
      this.Requestfilters = false;
      let knownSkillIds = new Array();//save all the known id's to avoid too much calls from backend
      let knownLocationIds = new Array();
      let knownPersonIds = new Array();
      let planningsUnit = [];
      this.allPlanningsUnits["Planner"] = [];
      this.allPlanningsUnits["Employee"] = [];
      this.planningsUnitIDs["Planner"] = [];
      this.planningsUnitIDs["Employee"] = [];
      this.holidayInConsideration["Alles"] = [];
      this.holidayNew["Alles"] = [];
      this.planningsUnitLocations["Alles"] = [];
      this.planningsUnitSkills["Alles"] = [];

      let skills, locations = [];
      let lastName, firstName, id, person;//creating of a person
      let type, comment, idMessage, requestDate, startDateHoliday, endDateHoliday, date, state, vacationName,
        lastUpdate;//creation holidayMessage
      let day, month, year;//creation of a date
      let dayPart, dayPartPrevious, dayPartTotal; //the sum of all the dayParts of a holiday: AM + PM == entire day

      let handler = () => {
        this.appService.getUser().subscribe((data) => {
          this.idemp = data.userId;
          /*
          load persons for planner mode
           */
          this.appService.getPlanningUnitsOfPlanner(data.userId).subscribe((data: any) => {
            let plannerUnitIds = data;

            this.appService.getAllHolidayMessagesPerPlanningUnit(plannerUnitIds).subscribe((data: any) => {
              planningsUnit["Alles"] = [];
              this.planningsUnitIDs["Planner"].push("Alles");

              for (let j = 0; j < plannerUnitIds.length; j++) {
                this.isPlanner = true;
                this.mode = "Planner";
                this.plannerMode = true; // moet geladen worden vanuit de settings
                if (!this.knownPlanningsUnitIDs[plannerUnitIds[j]]) {
                  this.knownPlanningsUnitIDs[plannerUnitIds[j]] = this.dictionaryService.getPlanningUnitNameOfId(plannerUnitIds[j]);
                }
                let planningsUnitName = this.knownPlanningsUnitIDs[plannerUnitIds[j]];
                this.planningsUnitSkills[planningsUnitName] = [];
                this.planningsUnitLocations[planningsUnitName] = [];
                this.knownPlanningsUnitIDNames[planningsUnitName] = plannerUnitIds[j];
                planningsUnit[planningsUnitName] = [];
                for (let i = 0; i < data[plannerUnitIds[j]].length; i++) {
                  skills = [];
                  locations = [];
                  id = data[plannerUnitIds[j]][i].id;// id of a person
                  if (!knownPersonIds[id]) {
                    /*

                    name of a person
                     */
                    lastName = this.dictionaryService.getCacheEmployeeLastNameOfId(data[plannerUnitIds[j]][i].id);
                    firstName = this.dictionaryService.getCacheEmployeeFirstNameOfId(data[plannerUnitIds[j]][i].id);
                    /*

                    Skills of a person
                     */
                    for (let idSkill of data[plannerUnitIds[j]][i].skills) {
                      if (!knownSkillIds[idSkill]) {
                        knownSkillIds[idSkill] = this.dictionaryService.getSkillNameOfId(idSkill);
                      }
                      skills.push(knownSkillIds[idSkill]);
                    }
                    /*

                    Locations of a person
                     */
                    for (let idLocation of data[plannerUnitIds[j]][i].locations) {
                      if (!knownLocationIds[idLocation]) {
                        knownLocationIds[idLocation] = this.dictionaryService.getLocationNameOfId(idLocation);
                      }
                      locations.push(knownLocationIds[idLocation]);
                    }
                    //create an person object
                    person = new Person(firstName, lastName, locations, skills, id);
                    /*

                    Create the holidayMessages
                     */
                    if (data[plannerUnitIds[j]][i].holidayMessages) {
                      if (data[plannerUnitIds[j]][i].holidayMessages.length > 0) {
                        for (let k = 0; k < data[plannerUnitIds[j]][i].holidayMessages.length; k++) {
                          this.hm.push(data[plannerUnitIds[j]][i].holidayMessages[k]);
                          state = data[plannerUnitIds[j]][i].holidayMessages[k].state;
                          if (state != "Rejected") {
                            type = data[plannerUnitIds[j]][i].holidayMessages[k].type;
                            comment = data[plannerUnitIds[j]][i].holidayMessages[k].comment;
                            if (comment != null)
                              if (comment.trim() == "")
                                comment = undefined;
                            idMessage = data[plannerUnitIds[j]][i].holidayMessages[k].id;
                            lastUpdate = data[plannerUnitIds[j]][i].holidayMessages[k].lastUpdate;
                            requestDate = new Date(data[plannerUnitIds[j]][i].holidayMessages[k].requestDate);
                            dayPartPrevious = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[0].daypart;//initial dayPart
                            dayPartTotal = '';
                            for (let x = 0; x < data[plannerUnitIds[j]][i].holidayMessages[k].exactDates.length; x++) {
                              if (dayPartTotal == '') {
                                dayPart = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].daypart;
                                if (dayPart != dayPartPrevious) dayPartTotal = "entire day";
                              }
                              year = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[0];
                              month = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[1];
                              day = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[2];
                              date = new Date().setFullYear(year, month, day);
                              if (x == 0) startDateHoliday = date;//first day
                              if (x == data[plannerUnitIds[j]][i].holidayMessages[k].exactDates.length - 1) {
                                endDateHoliday = date;//last day

                              }
                            }
                            vacationName = "";
                            /*this.appService.getHolidayBetweenTwoDates(this.datePipe.transform(new Date(startDateHoliday), "dd-MM-yyyy"), this.datePipe.transform(new Date(endDateHoliday), "dd-MM-yyyy")).subscribe((data: any) => {
                              vacationName = data.vacation;
                            });*/
                            if (dayPartTotal == "entire day") person.addHoliday(new Date(startDateHoliday), new Date(endDateHoliday), type, comment, dayPartTotal, idMessage, state, vacationName, requestDate, lastUpdate, person, true);
                            else person.addHoliday(new Date(startDateHoliday), new Date(endDateHoliday), type, comment, dayPart, idMessage, state, vacationName, requestDate, lastUpdate, person, true);
                          }
                        }
                      }
                    }
                    //end creation person
                    knownPersonIds[id] = person;
                    this.persons.push(person);
                    this.searchAll.push(person.toString());
                  } else {
                    person = knownPersonIds[id];
                    for (let idSkill of data[plannerUnitIds[j]][i].skills) {
                      if (!knownSkillIds[idSkill]) {
                        knownSkillIds[idSkill] = this.dictionaryService.getSkillNameOfId(idSkill);
                      }
                      person.addSkill(knownSkillIds[idSkill]);
                    }
                    for (let idLocation of data[plannerUnitIds[j]][i].locations) {
                      if (!knownLocationIds[idLocation]) {
                        knownLocationIds[idLocation] = this.dictionaryService.getLocationNameOfId(idLocation);
                      }
                      person.addLocation(knownLocationIds[idLocation]);
                    }
                  }//get the current processing person

                  this.planningsUnitLocations[planningsUnitName] = [];
                  this.planningsUnitSkills[planningsUnitName] = [];
                  for (let loc of data[plannerUnitIds[j]][i].PlanningUnits[plannerUnitIds[j]].locations) {
                    this.planningsUnitLocations[planningsUnitName].push(this.dictionaryService.getLocationNameOfId(loc));
                    if (this.planningsUnitLocations["Alles"].indexOf(this.dictionaryService.getLocationNameOfId(loc)) == -1) {
                      this.planningsUnitLocations["Alles"].push(this.dictionaryService.getLocationNameOfId(loc));
                    }
                  }
                  for (let skill of data[plannerUnitIds[j]][i].PlanningUnits[plannerUnitIds[j]].skills) {
                    this.planningsUnitSkills[planningsUnitName].push(this.dictionaryService.getSkillNameOfId(skill));
                    if (this.planningsUnitSkills["Alles"].indexOf(this.dictionaryService.getSkillNameOfId(skill)) == -1) {
                      this.planningsUnitSkills["Alles"].push(this.dictionaryService.getSkillNameOfId(skill));
                    }
                  }
                  if (!planningsUnit["Alles"].some((doctor) => doctor.id == person.id)) planningsUnit["Alles"].push(person);
                  planningsUnit[planningsUnitName].push(person);
                  if (this.planningsUnitIDs["Planner"].indexOf(planningsUnitName) == -1) this.planningsUnitIDs["Planner"].push(planningsUnitName);
                  person = knownPersonIds[id];//get the current processing person
                }

              }
              this.appService.getAllHMIDsPerPlanningUnitUnderPlannerWithID(this.idemp).subscribe((data) => {
                if (data) {
                  for (let i = 0; i < data.length; i++) {
                    let planningsUnitName = this.dictionaryService.getPlanningUnitNameOfId(data[i].unitID);
                    this.holidayInConsideration[planningsUnitName] = [];
                    this.holidayNew[planningsUnitName] = [];
                    if (data[i].holidayMessages) {
                      for (let holidayId of data[i].holidayMessages) {
                        if (holidayId.state == "InConsideration") {
                          this.holidayInConsideration[planningsUnitName].push(holidayId.id);
                          this.holidayInConsideration["Alles"].push(holidayId.id);
                        } else if (holidayId.state == "New") {
                          this.holidayNew[planningsUnitName].push(holidayId.id);
                          this.holidayNew["Alles"].push(holidayId.id);
                        }
                      }
                    }
                  }
                }
              });
              for (let i = 0; i < this.persons.length; i++) {
                if (this.persons[i].Holidays.length != 0) {
                  for (let h of this.persons[i].Holidays) {
                    h.endDateHoliday.setMonth(h.endDateHoliday.getMonth() - 1);
                    h.startDateHoliday.setMonth(h.startDateHoliday.getMonth() - 1);
                  }
                }
                this.AllHolidayMessagesCompact.push({
                  fullName: this.persons[i].toString(), skills: this.persons[i].skills,
                  locations: this.persons[i].locations, holidays: this.persons[i].Holidays
                });
                for (let skill of this.persons[i].skills) {
                  if (this.searchAll.indexOf(skill) == -1) {
                    this.searchAll.push(skill);
                    this.skills.push(skill);
                    this.selectedSkills[skill] = false;
                  }
                }
                for (let location of this.persons[i].locations) {
                  if (this.locations.indexOf(location) == -1) {
                    this.searchAll.push(location);
                    this.locations.push(location);
                    this.selectedLocation[location] = false;
                  }
                }
                if (i == this.persons.length - 1) {
                  this.visible_locations = this.locations.slice(0, this.numberLocations);
                  this.visible_skills = this.skills.slice(0, this.numberSkills);
                  this.visible_dayparts = this.AMPM;
                  this.visible_vacationTypes = this.vacationTypes.slice(0, this.numbervacationTypes);
                  this.visible_sorts = this.sorts.slice(0, this.numberSorts);
                }
              }
              this.planningsUnit = planningsUnit["Alles"];
              this.getAndSetCollisionStates();
            });
          });
          this.allPlanningsUnits["Planner"] = planningsUnit;
          /*
          load persons for employee mode
           */
          this.appService.getPlanningUnitsOfEmployee(data.userId).subscribe((data: any) => {
            let plannerUnitIds = data;
            this.appService.getAllHolidayMessagesPerPlanningUnit(plannerUnitIds).subscribe((data: any) => {
              planningsUnit["Alles"] = [];
              this.planningsUnitIDs["Employee"].push("Alles");
              for (let j = 0; j < plannerUnitIds.length; j++) {
                if (!this.knownPlanningsUnitIDs[plannerUnitIds[j]]) {
                  this.knownPlanningsUnitIDs[plannerUnitIds[j]] = this.dictionaryService.getPlanningUnitNameOfId(plannerUnitIds[j]);
                }
                let planningsUnitName = this.knownPlanningsUnitIDs[plannerUnitIds[j]];
                this.knownPlanningsUnitIDNames[planningsUnitName] = plannerUnitIds[j];
                planningsUnit[planningsUnitName] = [];
                for (let i = 0; i < data[plannerUnitIds[j]].length; i++) {
                  skills = [];
                  locations = [];
                  id = data[plannerUnitIds[j]][i].id;// id of a person
                  if (!knownPersonIds[id]) {
                    /*

                    name of a person
                     */
                    lastName = this.dictionaryService.getCacheEmployeeLastNameOfId(data[plannerUnitIds[j]][i].id);
                    firstName = this.dictionaryService.getCacheEmployeeFirstNameOfId(data[plannerUnitIds[j]][i].id);
                    /*

                    Skills of a person
                     */
                    for (let idSkill of data[plannerUnitIds[j]][i].skills) {
                      if (!knownSkillIds[idSkill]) {
                        knownSkillIds[idSkill] = this.dictionaryService.getSkillNameOfId(idSkill);
                      }
                      skills.push(knownSkillIds[idSkill]);
                    }
                    /*

                    Locations of a person
                     */
                    for (let idLocation of data[plannerUnitIds[j]][i].locations) {
                      if (!knownLocationIds[idLocation]) {
                        knownLocationIds[idLocation] = this.dictionaryService.getLocationNameOfId(idLocation);
                      }
                      locations.push(knownLocationIds[idLocation]);
                    }
                    //create an person object
                    person = new Person(firstName, lastName, locations, skills, id);
                    /*

                    Create the holidayMessages
                     */
                    if (data[plannerUnitIds[j]][i].holidayMessages) {


                      if (data[plannerUnitIds[j]][i].holidayMessages.length > 0) {
                        for (let k = 0; k < data[plannerUnitIds[j]][i].holidayMessages.length; k++) {
                          this.hm.push(data[plannerUnitIds[j]][i].holidayMessages[k]);
                          state = data[plannerUnitIds[j]][i].holidayMessages[k].state;
                          if (state != "Rejected") {
                            type = data[plannerUnitIds[j]][i].holidayMessages[k].type;
                            comment = data[plannerUnitIds[j]][i].holidayMessages[k].comment;
                            if (comment != null)
                              if (comment.trim() == "")
                                comment = undefined;
                            idMessage = data[plannerUnitIds[j]][i].holidayMessages[k].id;
                            lastUpdate = data[plannerUnitIds[j]][i].holidayMessages[k].lastUpdate;
                            requestDate = new Date(data[plannerUnitIds[j]][i].holidayMessages[k].requestDate);
                            dayPartPrevious = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[0].daypart;//initial dayPart
                            dayPartTotal = '';
                            for (let x = 0; x < data[plannerUnitIds[j]][i].holidayMessages[k].exactDates.length; x++) {
                              if (dayPartTotal == '') {
                                dayPart = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].daypart;
                                if (dayPart != dayPartPrevious) dayPartTotal = "entire day";
                              }
                              year = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[0];
                              month = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[1];
                              day = data[plannerUnitIds[j]][i].holidayMessages[k].exactDates[x].date[2];
                              date = new Date().setFullYear(year, month, day);
                              if (x == 0) startDateHoliday = date;//first day
                              if (x == data[plannerUnitIds[j]][i].holidayMessages[k].exactDates.length - 1) endDateHoliday = date;//last day
                            }
                            let vacationName = "";
                            /*this.appService.getHolidayBetweenTwoDates(this.datePipe.transform(new Date(startDateHoliday), "dd-MM-yyyy"), this.datePipe.transform(new Date(endDateHoliday), "dd-MM-yyyy")).subscribe((data: any) => {
                              vacationName = data.vacation;
                            });*/
                            if (dayPartTotal == "entire day") person.addHoliday(new Date(startDateHoliday), new Date(endDateHoliday), type, comment, dayPartTotal, idMessage, state, vacationName, requestDate, lastUpdate, person, true);
                            else person.addHoliday(new Date(startDateHoliday), new Date(endDateHoliday), type, comment, dayPart, idMessage, state, vacationName, requestDate, lastUpdate, person, true);
                          }
                        }
                      }
                    }
                    //end creation person

                    knownPersonIds[id] = person;
                    this.persons.push(person);
                    this.searchAll.push(person.toString());
                  } else {
                    person = knownPersonIds[id];

                    for (let idSkill of data[plannerUnitIds[j]][i].skills) {
                      if (!knownSkillIds[idSkill]) {
                        knownSkillIds[idSkill] = this.dictionaryService.getSkillNameOfId(idSkill);
                      }
                      person.addSkill(knownSkillIds[idSkill]);
                    }

                    for (let idLocation of data[plannerUnitIds[j]][i].locations) {
                      if (!knownLocationIds[idLocation]) {
                        knownLocationIds[idLocation] = this.dictionaryService.getLocationNameOfId(idLocation);
                      }
                      person.addLocation(knownLocationIds[idLocation]);
                    }

                  }
                  if (!planningsUnit["Alles"].some((doctor) => doctor.id == person.id)) planningsUnit["Alles"].push(person);
                  planningsUnit[planningsUnitName].push(person);
                  if (this.planningsUnitIDs["Employee"].indexOf(planningsUnitName) == -1) this.planningsUnitIDs["Employee"].push(planningsUnitName);
                  person = knownPersonIds[id];//get the current processing person
                }

                //}
              }
              for (let i = 0; i < this.persons.length; i++) {
                for (let skill of this.persons[i].skills) {
                  if (this.searchAll.indexOf(skill) == -1) {
                    this.searchAll.push(skill);
                    this.skills.push(skill);
                    this.selectedSkills[skill] = false;
                  }
                }
                for (let location of this.persons[i].locations) {
                  if (this.locations.indexOf(location) == -1) {
                    this.searchAll.push(location);
                    this.locations.push(location);
                    this.selectedLocation[location] = false;
                  }
                }
                if (i == this.persons.length - 1) {
                  this.visible_locations = this.locations.slice(0, this.numberLocations);
                  this.visible_skills = this.skills.slice(0, this.numberSkills);
                  this.visible_dayparts = this.AMPM;
                  this.visible_vacationTypes = this.vacationTypes.slice(0, this.numbervacationTypes);
                  this.visible_sorts = this.sorts.slice(0, this.numberSorts);
                }
              }
              this.appService.getSettingsOfEmployee(this.idemp).subscribe(settings => {
                if (settings) {
                  this.settings = settings;
                  this.loadSettings(settings);
                }

              }, error => {
                this.settings = new Settings();
                this.settings.employeeID = this.idemp;
              });
              this.loadAllPUFromDict();
            });
            this.allPlanningsUnits["Employee"] = planningsUnit;
            if (!this.isPlanner) this.planningsUnit = planningsUnit["Alles"];
            this.currentPlanningsUnitName = "Alles";
          });
        });
      };
      if (this.dictionaryService.dataLoaded) {
        handler();
        if (this.isPlanner) {
          this.mode = "Planner";
        } else {
          this.mode = "Employee";
        }
      } else {
        this.dictionaryService.Ready.on(handler);
      }
    }
  }

  ngOnInit() {
    this.buildTranslationsMaps();
    this.routeSub = this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.postSettings();
      }
    });
  }

  buildTranslationsMaps(): void {
    this.sortByMap.clear();
    this.sortByMap.set('granted', 'granted')
      .set(this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↓', 'Datum ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↑', 'Datum ↑')
      .set(this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↓', 'Achternaam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↑', 'Achternaam ↑')
      .set(this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↓', 'skill-naam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↑', 'skill-naam ↑')
      .set(this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↓', 'Voornaam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↑', 'Voornaam ↑');
    this.sorts = Array.from(this.sortByMap.keys()).slice(1);
    this.visible_sorts = this.sorts.slice(0, this.numberSorts);

    this.daypartMap.clear();
    this.daypartMap.set(this._translate.instant('FILTERS_DAYPART_MORNING'), 'Voormiddag')
      .set(this._translate.instant('FILTERS_DAYPART_AFTERNOON'), 'Namiddag')
      .set(this._translate.instant('FILTERS_DAYPART_FULL_DAY'), 'Werkdag');
    this.AMPM = Array.from(this.daypartMap.keys());
    this.visible_dayparts = this.AMPM;

    this.absenceTypeMap.clear();
    this.absenceTypeMap.set(this._translate.instant('FILTERS_ABSENCE_TYPE_ANNUAL'), 'Jaarlijks')
      .set(this._translate.instant('FILTERS_ABSENCE_TYPE_EUROPEAN'), 'Europees')
      .set(this._translate.instant('FILTERS_ABSENCE_TYPE_EDUCATIONAL'), 'Educatief')
      .set(this._translate.instant('FILTERS_ABSENCE_TYPE_SICKNESS'), 'Ziekte')
      .set(this._translate.instant('FILTERS_ABSENCE_TYPE_OTHER'), 'Andere')
    this.holidayTypes = Array.from(this.absenceTypeMap.keys());

    this.commentTypes = [];
    this.commentTypes.push(this._translate.instant('FILTERS_COMMENT_YES'), this._translate.instant('FILTERS_COMMENT_NO'));

    this.selectedHolidays = {};
    this.selectedHolidays[this._translate.instant('FILTERS_ABSENCE_TYPE_ANNUAL')] = false;
    this.selectedHolidays[this._translate.instant('FILTERS_ABSENCE_TYPE_EUROPEAN')] = false;
    this.selectedHolidays[this._translate.instant('FILTERS_ABSENCE_TYPE_EDUCATIONAL')] = false;
    this.selectedHolidays[this._translate.instant('FILTERS_ABSENCE_TYPE_SICKNESS')] = false;
    this.selectedHolidays[this._translate.instant('FILTERS_ABSENCE_TYPE_OTHER')] = false;

    this.holidayTypeMap.clear();
    this.holidayTypeMap.set("Yearly", this._translate.instant('FILTERS_ABSENCE_TYPE_ANNUAL'))
      .set("European", this._translate.instant('FILTERS_ABSENCE_TYPE_EUROPEAN'))
      .set("Educative", this._translate.instant('FILTERS_ABSENCE_TYPE_EDUCATIONAL'))
      .set("Sickness", this._translate.instant('FILTERS_ABSENCE_TYPE_SICKNESS'))
      .set("Other", this._translate.instant('FILTERS_ABSENCE_TYPE_OTHER'));
  }

  /*
  Een favoriete persoon toevoegen
   */

  addPerson() {
    if (!this.favorites.some((doctor) => doctor.toString() == this.inputname)
      && this.persons.some((doctor) => doctor.toString() == this.inputname)) {
      this.favorites.push(this.persons.find(x => x.toString() === this.inputname));
    }
    this.recalculate();
  }

  /*
  Een favoriete persoon verwijderen
   */

  deletePerson(id) {
    let element = document.getElementById(id);
    element.parentNode.removeChild(element);
    for (let i = 0; i < this.favorites.length; i++) {
      if (this.favorites[i].id == id) {
        this.favorites.splice(i, 1);
      }
    }
    this.recalculate();
  }

  /*
  Een filter menu openen
   */
  openCloseLeftMenu(id) {
    if (document.getElementById(id).style.display == "block") {
      document.getElementById(id).style.display = "none";
      $("#" + "br" + id).show();
    } else {
      document.getElementById(id).style.display = "block";
      $("#" + "br" + id).hide();
    }
  }

  /*

  datum omzetten naar leesbare datum
   */

  formatDate(date) {
    var monthNames = [
      "Januari", "Februari", "Maart",
      "April", "Mei", "Juni", "Juli",
      "Augustus", "September", "Oktober",
      "November", "December"
    ];

    var day = date.getDate();
    var monthIndex = date.getMonth();
    var year = date.getFullYear();
    var hour = date.getHours();
    var minutes = date.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return day + ' ' + monthNames[monthIndex] + ' ' + year + ' ' + hour + ':' + minutes;
  }

  /*

  "lees meer/minder" knop

  vuile code => wordt binnenkort snel aangepast!
   */

  show(numberType, visible_type, Type) {
    numberType = Type.length;
    visible_type = Type.slice(0, numberType);
    if (Type == this.skills) {
      this.visible_skills = visible_type;
      this.numberSkills = Type.length;
    } else if (Type == this.locations) {
      this.visible_locations = visible_type;
      this.numberLocations = Type.length;
    } else if (Type == this.vacationTypes) {
      this.visible_vacationTypes = visible_type;
      this.numbervacationTypes = Type.length;
    } else if (Type == this.sorts) {
      this.visible_sorts = visible_type;
      this.numberSorts = Type.length;
    } else if (Type == this.allPUList) {
      this.visiblePU = visible_type;
      this.numberPU = Type.length;
    }
  }

  hide(numberType, visible_type, Type) {
    numberType = 5;
    visible_type = Type.slice(0, numberType);
    if (Type == this.skills) {
      this.visible_skills = visible_type;
      this.numberSkills = 5;
    } else if (Type == this.locations) {
      this.visible_locations = visible_type;
      this.numberLocations = 5;
    } else if (Type == this.vacationTypes) {
      this.visible_vacationTypes = visible_type;
      this.numbervacationTypes = 5;
    } else if (Type == this.sorts) {
      this.visible_sorts = Type.slice(0, 5);
      this.numberSorts = 5;
    } else if (Type == this.allPUList) {
      this.visiblePU = Type.slice(0, 5);
      this.numberPU = 5;
    }
  }

  /*
        .set(this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↓', 'Datum ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↑', 'Datum ↑')
      .set(this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↓', 'Achternaam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↑', 'Achternaam ↑')
      .set(this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↓', 'Voornaam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↑', 'Voornaam ↑');
            .set(this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↓', 'skill-naam ↓')
      .set(this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↑', 'skill-naam ↑')

  De sorteerfuncties
   */
  sort(type) {
    let temp = this.filteredRequests;
    this.filteredRequests = [];
    if (type == this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↓') this.sortFirstNameAsc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_FIRST_NAME') + ' ↑') this.sortFirstNameDesc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↑') this.sortLastNameDesc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_LAST_NAME') + ' ↓') this.sortLastNameAsc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↓') this.sortSkillNameAsc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_SKILL_NAME') + ' ↑') this.sortSkillNameDesc();
    for (let doctor of this.filteredDoctors) {
      for (let holiday of doctor.Holidays) {
        if (temp.some((vacation) => vacation.idMessage == holiday.idMessage)) {
          this.filteredRequests.push(holiday);
        }
      }
    }
    if (type == "granted") this.sortGranted();
    else if (type == this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↑') this.sortDateDesc();
    else if (type == this._translate.instant('FILTERS_SORT_BY_DATE') + ' ↓') this.sortDateAsc();
  }

  sortFirstNameAsc() {
    this.filteredDoctors.sort((a, b) => a.firstname.localeCompare(b.firstname));
  }

  sortFirstNameDesc() {
    this.filteredDoctors.sort((a, b) => b.firstname.localeCompare(a.firstname));
  }

  sortLastNameAsc() {
    this.filteredDoctors.sort((a, b) => a.lastname.localeCompare(b.lastname));
  }

  sortLastNameDesc() {
    this.filteredDoctors.sort((a, b) => b.lastname.localeCompare(a.lastname));
  }

  sortSkillNameAsc() {
    this.filteredDoctors.sort((a, b) => a.skills.toString().localeCompare(b.skills.toString()));
  }

  sortSkillNameDesc() {
    this.filteredDoctors.sort((a, b) => b.skills.toString().localeCompare(a.skills.toString()));
  }

  sortGranted() {
    this.filteredRequests.sort((a, b) => a.idMessage - b.idMessage);
  }

  sortDateDesc() {
    this.filteredRequests.sort((a, b) => new Date(b.startDateHoliday).getTime() - new Date(a.startDateHoliday).getTime());
  }

  sortDateAsc() {
    this.filteredRequests.sort((a, b) => new Date(a.startDateHoliday).getTime() - new Date(b.startDateHoliday).getTime());
  }

  /*

  Zoek-functionaliteit
   */

  doctorsWithContent(content, temp) {
    for (let person of this.filteredDoctors) {
      if (person.toString().match(content)
        && !temp.some((doctor) => doctor.id == person.id)) {
        if (this.favorites.some((doctor) => doctor.id == person.id)) temp.unshift(person);
        else temp.push(person);
      }
      for (let Alocation of person.locations)
        if (Alocation.toLowerCase().match(content.toLowerCase())
          && !temp.some((doctor) => doctor.id == person.id)) {
          if (this.favorites.some((doctor) => doctor.id == person.id)) temp.unshift(person);
          else temp.push(person);
        }
      for (let Askill of person.skills) {
        if (Askill.toLowerCase().match(content.toLowerCase())
          && !temp.some((doctor) => doctor.id == person.id)) {
          if (this.favorites.some((doctor) => doctor.id == person.id)) temp.unshift(person);
          else temp.push(person);
        }
      }
    }
  }

  doctorsWithDaypart(dayparts, temp) {
    for (let person of this.filteredDoctors) {
      for (let holiday of person.Holidays) {
        if (holiday.dayPart == dayparts) {
          if (!this.filteredRequests.some((request) => request.idMessage == holiday.idMessage)) {
            if (this.favorites.some((doctor) => doctor.id == person.id)) this.filteredRequests.unshift(holiday);
            else this.filteredRequests.push(holiday)
          }
          if (!temp.some((doctor) => doctor.id == person.id)) {
            if (this.favorites.some((doctor) => doctor.id == person.id)) temp.unshift(person);
            else temp.push(person);
          }
        }
      }
    }
  }

  dataChanged(event) {
    this.recalculate();
    if (event != "") {
      let temp = [];
      let tempVacation = this.filteredRequests;
      this.filteredRequests = [];
      for (let value of this.searchAll) {
        if (value.toLowerCase().includes(event.toLowerCase())) {
          this.doctorsWithContent(value, temp);
          for (let doctor of temp) {
            for (let holiday of doctor.Holidays) {
              if (tempVacation.some((vacation) => vacation.idMessage == holiday.idMessage)) {
                this.filteredRequests.push(holiday);
              }
            }
          }
          if (value == "vm" || value == "voormiddag") {
            value = "AM";
          } else if (value == "nm" || value == "namiddag") {
            value = "PM";
          } else if (value == "werkdag") {
            value = "entire day";
          }
          this.doctorsWithDaypart(value, temp);
        }
      }
      this.filteredDoctors = temp;
    }
  }

  /*

  filter functionaliteit
   */

  checkboxSkillChanged(skillType) {
    this.selectedSkills[skillType] = !this.selectedSkills[skillType];
    this.recalculate();
  }

  checkboxLocationChanged(location) {
    this.selectedLocation[location] = !this.selectedLocation[location];
    this.recalculate();
  }

  checkboxHolidayChanged(holiday) {
    this.selectedHolidays[this.absenceTypeMap.get(holiday)] = !this.selectedHolidays[this.absenceTypeMap.get(holiday)];
    this.recalculate();
  }

  checkComment(comment) {
    if (comment != null || comment != undefined) {
      return this.selectedComments["Ja"];
    } else {
      return this.selectedComments["Neen"];
    }
  }

  checkboxCommentChanged(comment) {
    if (comment == this._translate.instant('FILTERS_COMMENT_YES')) {
      this.selectedComments["Ja"] = !this.selectedComments["Ja"];
    } else {

      this.selectedComments["Neen"] = !this.selectedComments["Neen"];
    }
    this.recalculate();
  }

  checkboxPUChanged(pu) {
    this.selectedPU[pu] = !this.selectedPU[pu];
    this.recalculate();
  }

  checkboxAMPMChanged(daypart) {
    this.AMPM_alt[daypart] = !this.AMPM_alt[daypart];
    if (daypart == this._translate.instant('FILTERS_DAYPART_MORNING')) {
      this.selectedAMPM["AM"] = !this.selectedAMPM["AM"];
    } else if (daypart == this._translate.instant('FILTERS_DAYPART_AFTERNOON')) {
      this.selectedAMPM["PM"] = !this.selectedAMPM["PM"];
    } else if (daypart == this._translate.instant('FILTERS_DAYPART_FULL_DAY')) {
      this.selectedAMPM["entire day"] = !this.selectedAMPM["entire day"];
    }
    this.recalculate();
  }

  changeMonth(m: number) {
    this.minicalendars.forEach(c => {
      c.filterMonths(m);
    });
  }

  selectedItemChanged(index) {
    this.currentPlanningsUnitName = index;
    if (this.isPlanner && this.plannerMode) this.planningsUnit = this.allPlanningsUnits["Planner"][index];
    else this.planningsUnit = this.allPlanningsUnits["Employee"][index];
    if (index != "Alles") {
      this.mincal.filterPu(this.knownPlanningsUnitIDNames[index]);
    } else {
      this.mincal.filterPu("0");
    }
    for (let request of this.testRequests) {
      $("#test" + request.id).prop('value', 'test');
      $("#test" + request.id).prop('style', 'margin-left: 1%;float: left;');
    }
    this.testRequests = [];
    this.personsTest = [];
    this.recalculate();
  }

  recalculate() {
    this.filteredDoctors = [];
    this.filteredRequests = [];
    for (let person of this.planningsUnit) {
      for (let skill of person.skills) {
        if (this.selectedSkills[skill] || this.allFalse(this.selectedSkills)) {
          for (let location of person.locations) {
            if ((this.selectedLocation[location] || this.allFalse(this.selectedLocation))) {
              for (let holiday of person.Holidays) {
                if (this.selectedAMPM[holiday.dayPart] || this.allFalse(this.selectedAMPM)) {
                  if (this.selectedHolidays[this.holidayTypeMap.get(holiday.type)] || this.allFalse(this.selectedHolidays)) {
                    if (this.checkComment(holiday.comment) || this.allFalse(this.selectedComments)) {
                      if (holiday.shown == true) {
                        if (this.filteredDoctors.indexOf(person) == -1) {
                          if (this.favorites.some((doctor) => doctor.id == person.id)) this.filteredDoctors.unshift(person);
                          else this.filteredDoctors.push(person);
                        }
                        if (this.filteredRequests.indexOf(holiday) == -1) {
                          if (this.favorites.some((doctor) => doctor.id == person.id)) this.filteredRequests.unshift(holiday);
                          else this.filteredRequests.push(holiday);
                        }
                      }
                    }
                  }
                }
              }
              break;
            }
          }
          break;
        }
      }
    }
    this.getAndSetCollisionStates();
    this.sort(this.sortType);
    let id = [];
    for (let message of this.AllHolidayMessagesCompact) {
      for (let absence of message.holidays) {
        if (absence.state == "Approved") {
          id.push(absence.idMessage);
        }
      }
    }
    this.minicalendars.forEach(c => c.deleteAbsences(id));
    id = [];
    for (let message of this.filteredRequests) {
      if (message.state == "Approved") {
        id.push(message.idMessage);
      }
    }
    this.minicalendars.forEach(c => c.addAbsences(id));
  }

  allFalse(array) {
    for (let element in array)
      if (array[element]) return false;
    return true;
  }

  exists(absence) {
    if (this.filteredRequests.some((vacation) => vacation.idMessage == absence.idMessage)) {
      return true
    } else return false;
  }

  /*

  processing requests
   */

  //Refreshes the calendar components if something were to change, called by this.refresh.next
  refresh: Subject<any> = new Subject();

  /*

  maand opvragen van een date voor filterfuncties
   */

  getMonth(date) {
    var monthNames = [
      "January", "February", "March",
      "April", "May", "June", "July",
      "August", "September", "October",
      "November", "December"
    ];
    var monthIndex = date.getMonth();
    return monthNames[monthIndex];
  }

  /*

  specifieke filter verwijderen
   */

  deleteFilter(filterArray) {
    for (let element in filterArray) {
      filterArray[element] = false;
      $("#" + element).prop("checked", false);
    }
    this.recalculate();
  }

  /*
  alle filters verwijderen
   */

  deleteAllFilters() {
    this.deleteFilter(this.selectedSkills);
    this.deleteFilter(this.selectedLocation);
    this.deleteFilter(this.selectedMonths);
    this.deleteFilter(this.selectedAMPM);
    this.deleteFilter(this.selectedComments);
    this.deleteFilter(this.selectedHolidays);
    this.deleteFilter(this.AMPM_alt);
  }

  /*

  afhandeling afwezigheid
   */

  modalData: {
    action: string;
    event: any;

  };

  openSettingsWindow() {
    this.modalData = {action: "Click", event: null};
    this.modal.open(this.modalContent, {size: 'lg'});
    this.planninghidden = false;
  }

  openHelpWindow() {
    this.modalData = {action: "Click", event: null};
    this.modal.open(this.helpWindow, {size: 'lg'});
  }

  receiveMessage($event) {
    if (Number($event) >= 0) {
      let startDate = new Date();
      let endDate = new Date();
      let number = Number($event);
      let alreadyExists = false;
      for (let holiday of this.hm) {
        if (holiday.id == number) {
          if (this.testRequests.length != 0) {
            for (let request = 0; request < this.testRequests.length; request++) {
              if (this.testRequests[request].id == holiday.id && !alreadyExists) {
                this.testRequests.splice(request, 1);
                for (let person = 0; person < this.personsTest.length; person++) {
                  if (this.personsTest[person].id == holiday.id) {
                    this.personsTest.splice(person, 1);
                  }
                }
                alreadyExists = true;
                break;
              }
            }
            if (!alreadyExists) {
              this.testRequests.push(holiday);
              this.personsTest.push(new PersonDate(holiday.id, holiday.employeeID, holiday.exactDates));
            }
          } else {
            this.testRequests.push(holiday);
            this.personsTest.push(new PersonDate(holiday.id, holiday.employeeID, holiday.exactDates));
          }
          for (let request of this.testRequests) {
            if (startDate.getTime() - new Date(request.exactDates[0].date[0], request.exactDates[0].date[1] - 1, request.exactDates[0].date[2]).getTime() < 0) {
              startDate = new Date(request.exactDates[0].date[0], request.exactDates[0].date[1] - 1, request.exactDates[0].date[2]);
            }
            if (endDate.getTime() - new Date(request.exactDates[request.exactDates.length - 1].date[0], request.exactDates[request.exactDates.length - 1].date[1] - 1, request.exactDates[request.exactDates.length - 1].date[2]).getTime() < 0) {
              endDate = new Date(request.exactDates[request.exactDates.length - 1].date[0], request.exactDates[request.exactDates.length - 1].date[1] - 1, request.exactDates[request.exactDates.length - 1].date[2]);
            }
          }
        }
      }
      let PUnames = [];
      if (this.currentPlanningsUnitName == "Alles") {
        for (let puname of this.planningsUnitIDs[this.mode]) {
          if (puname != "Alles" && puname != undefined) {
            PUnames.push(this.knownPlanningsUnitIDNames[puname]);
          }
        }
      } else {
        PUnames.push(this.knownPlanningsUnitIDNames[this.currentPlanningsUnitName]);
      }
      for (let request of this.testRequests) {
        $("#test" + request.id).prop('value', ' ⏳');
        $("#test" + request.id).prop('style', 'margin-left: 1%;float: left;');
      }
      this.appService.isPlanningStillPossibleForPlanningUnitsWithoutPeople(this.datePipe.transform(startDate, "yyyy-MM-dd"), this.datePipe.transform(endDate, "yyyy-MM-dd"), PUnames, this.personsTest).subscribe((data) => {
        if (data.isPossible == true) {
          for (let request of this.testRequests) {
            $("#test" + request.id).prop('value', ' ✅');
            $("#test" + request.id).prop('style', 'margin-left: 1%;float: left;background: rgb(0,187,0);');
          }
        }
        if (data.isPossible == false) {
          for (let request of this.testRequests) {
            $("#test" + request.id).prop('value', ' ❌');
            $("#test" + request.id).prop('style', 'margin-left: 1%;float: left;background: rgb(187,0,0);');
          }
        }
      });
    } else {
      this.message = $event;
      let p = null;
      let a = this.message.split(" ");
      let status = a.splice(0, 1)[0];
      let idstatus = a.splice(0, 1)[0];
      let sometimesCollision = a.splice(0, 1)[0];
      let c = "";
      for (let text of a) {
        c = c + text + " ";
      }
      c = c.substr(0, c.length - 1);
      for (let person of this.AllHolidayMessagesCompact) {
        for (let absence of person.holidays) {
          if (absence.idMessage == idstatus) {
            if (p == null) {
              p = absence;
            }
            absence.shown = false;
            if (status == "InConsideration") {
              absence.state = "InConsideration";
            } else if (status == "Approved") {
              absence.state = "Approved";
            } else {
              absence.state = "Rejected";
            }

          }
        }
      }

      if (sometimesCollision == "true" || status == "Rejected") {
        this.recalculate();
      }
      let hpm = this.createHolidayPlanningMessage(p, status, c);
      if (this.currentPlanningsUnitName == "Alles") {
        this.appService.postHolidayPlanningsMessage(hpm).subscribe(id => {

          if (sometimesCollision == "true") {
            this.getAndSetCollisionStates();
          }

          this.appService.getHolidayMessageWithDatabaseID(id).subscribe(hm => {
            if (hm.state == "Approved") {
              this.mincal.convertHolidayMessagesToCalendarEvent([hm]);
            }
          });
        });
      } else {
        this.appService.postholidayPlanningsMessageForPlanningsUnit(hpm, this.knownPlanningsUnitIDNames[this.currentPlanningsUnitName]).subscribe(id => {
          if (sometimesCollision == "true") {
            this.getAndSetCollisionStates();

          }

          this.appService.getHolidayMessageWithDatabaseID(id).subscribe(hm => {
            if (hm.state == "Approved") {
              this.mincal.convertHolidayMessagesToCalendarEvent([hm]);
            }
          });
        });
      }

      if (sometimesCollision == "true") {
        this.isSometimesRequestsDisabled = true;
        this.requests.forEach(r => {
          if (r.id == parseInt(idstatus) && status == "Approved") {
            r.isAcceptedAndSometimesCollision = true;
          }
          if (r.isSometimesCollision) {
            r.isAlwaysCollision = true;
          }
        });
      }
    }
  }


  /*
  ResetSettings
   */
  changeSettings($event) {

    if ($event == "View")
      this.toggle = !this.toggle;
    else if ($event == "requestFilters") {
      this.requestFilters = !this.requestFilters;
    }
    //else if($event == "Reset")
    else if ($event == "Mode") {
      if (this.mode == "Planner") {
        this.mode = "Employee";
      } else {
        this.mode = "Planner";
      }
      this.plannerMode = !this.plannerMode;
    } else if ($event == "Reset") {
      this.resetSettings();
    }
    this.recalculate();
  }

  createHolidayPlanningMessage(p: any, state: string, comment: string): HolidayPlanningsMessage {
    let hpm = new HolidayPlanningsMessage();
    hpm.id = p.idMessage;
    hpm.state = state;
    hpm.comment = comment;
    hpm.plannerID = this.idemp;
    return hpm;
  }


  collisionsLoaded;

  getAndSetCollisionStates() {
    this.collisionsLoaded = false;
    let collisionstart: Date = new Date();
    let collisionend: Date = new Date();
    if (this.filteredRequests.length >= 1) {
      for (let request of this.filteredRequests) {
        if (collisionend < request.endDateHoliday) {
          collisionend = new Date(request.endDateHoliday);

        }
        if (collisionstart > request.startDateHoliday) {
          collisionstart = new Date(request.startDateHoliday);

        }
      }
    } else {
      for (let doctor of this.planningsUnit) {
        for (let holiday of doctor.Holidays) {
          if (collisionend < holiday.endDateHoliday) {
            collisionend = new Date(holiday.endDateHoliday);

          }
          if (collisionstart > holiday.startDateHoliday) {
            collisionstart = new Date(holiday.startDateHoliday);

          }
        }
      }
    }

    let start = this.datePipe.transform(collisionstart, 'yyyy-MM-dd');
    let end = this.datePipe.transform(collisionend, 'yyyy-MM-dd');
    if (this.currentPlanningsUnitName == "Alles") {
      this.appService.getAllCollisionStatesUnderPlannerBetween(this.idemp, start, end).subscribe(collisions => {
        this.applyCollisionsToRequests(collisions);
        this.collisionsLoaded = true;
      });
    } else {
      let idpu = this.knownPlanningsUnitIDNames[this.currentPlanningsUnitName];
      this.appService.getAllCollisionStatesUnderPlanningUnitBetween(idpu, start, end).subscribe(collisions => {
        this.applyCollisionsToRequests(collisions);
        this.collisionsLoaded = true;
      });
    }

  }

  applyCollisionsToRequests(collisions: any) {
    for (let hm of collisions.holidayMessages) {
      let ele = document.getElementById("hm" + hm.id);
      if (ele) {
        if (hm.CollisionState == "UNKNOWN") {
          ele.className = "a-request-unknown";
        } else if (hm.CollisionState == "NEVER") {
          ele.className = "a-request-never";
          this.requests.forEach(r => {
            if (r.id == hm.id) {
              r.isSometimesCollision = false;
              r.isAlwaysCollision = false;
            }
          });
        } else if (hm.CollisionState == "SOMETIMES") {
          this.requests.forEach(r => {
            if (r.id == hm.id) {
              r.isSometimesCollision = true;
            }
          });

          ele.className = "a-request-sometimes";
        } else {
          this.requests.forEach(r => {
            if (r.id == hm.id) {
              r.isAlwaysCollision = true;
            }
          });
          ele.className = "a-request-notallowed";
        }
      } else {
      }
    }

    if (this.isSometimesRequestsDisabled) {
      this.isSometimesRequestsDisabled = false;
      this.requests.forEach(r => {
        if (r.isSometimesCollision) {
          r.isAlwaysCollision = false;
        }
      });
    }
    for (let colls of collisions.collisionGroups) {
      for (let id of colls) {
        this.collisions[id] = new Array();
        for (let c of colls) {
          if (c != id) {
            this.collisions[id].push(c);
            if (this.collisions[c]) {
              let gevonden = false;
              let i = 0;
              while (!gevonden && i < this.collisions[c].length) {
                if (this.collisions[c][i] == id) {
                  gevonden = true;
                }
                i++;
              }
              if (!gevonden) {
                this.collisions[c].push(id);
              }
            }
          }

        }
      }
    }
  }

  @ViewChild("absenceContainer", {read: ViewContainerRef}) absenceContainer: ViewContainerRef;

  addHolidayToPerson(holiday: any) {
    let found: boolean = false;
    let counter = 0;
    while (!found && this.planningsUnit[counter]) {
      let d = this.planningsUnit[counter];
      if (this.employeeid == d.id) {
        holiday.person = d;
        if (holiday.comment == undefined) {
          holiday.comment = null;
        }
        d.addHoliday(holiday.startDateHoliday, holiday.endDateHoliday, holiday.type, holiday.comment, holiday.dayPart, holiday.idMessage, holiday.state, holiday.vacationName,
          holiday.requestDate, holiday.lastUpdate, holiday.person, holiday.shown);
        found = true;
      }
      counter++;
    }
    if (this.currentPlanningsUnitName != "Alles") {
      this.holidayNew[this.currentPlanningsUnitName].push(holiday.idMessage);
    }
    this.holidayNew["Alles"].push(holiday.idMessage);


    this.recalculate();
  }

  employeeid: string;

  getPersonOfHoliday(id: string) {
    this.employeeid = id;
  }

  isCollision(collisions: string[]) {
    if (collisions) {
      return collisions.length > 0 ? true : false;
    } else {
      return false;
    }

  }

  loadSettings(settings: Settings) {
    if (settings.dayPart == null || settings.dayPart == "") {
      settings.dayPart = "";
    } else {
      this.AMPM_alt[settings.dayPart.valueOf()] = true;
      if (settings.dayPart.valueOf() == "Voormiddag") {
        this.selectedAMPM["AM"] = true;
      } else if (settings.dayPart.valueOf() == "Namiddag") {
        this.selectedAMPM["PM"] = true;
      } else if (settings.dayPart.valueOf() == "Werkdag") {
        this.selectedAMPM["entire day"] = true;
      }

      let e = document.getElementById(settings.dayPart.valueOf());
      if (e != null) {
        e.setAttribute('checked', '');
      }
    }

    for (let loc of settings.location) {
      let s: string = loc.valueOf();
      document.getElementById(s).setAttribute('checked', '');
      this.selectedLocation[s] = true;
    }
    for (let skill of settings.function) {
      let s: string = skill.valueOf();
      document.getElementById(s).setAttribute('checked', '');
      this.selectedSkills[s] = true;
    }

    for (let period of settings.period) {
      let s: string = period.valueOf();
      document.getElementById(s).setAttribute('checked', '');
      this.selectedPeriods[s] = true;
    }

    for (let fav of settings.favorites) {
      let s: string = fav.valueOf();
      this.favorites.push(s);
    }

    for (let type of settings.absenceType) {
      let s: string = type.valueOf();
      document.getElementById(s).setAttribute('checked', '');
      this.selectedHolidays[s] = true;
    }

    for (let comm of settings.comments) {
      let s: string = comm.valueOf();
      document.getElementById(s).setAttribute('checked', '');
      this.selectedComments[s] = true;
    }

    /*
    (async () => {
      await this.delay(2500);
      this.recalculate();
    })();*/

    (async () => {
      let recalc = () => {
        this.recalculate();
      };
      if (!this.mincal.isLoaded) {
        this.mincal.Ready.on(recalc);
      } else {
        recalc();
      }
    })();


  }

  postSettings() {

    /*
        this.settings.favorites = [];
        for(let fav of this.favorites){
          this.settings.favorites.push(fav);
        }
    */
    this.settings.period = [];
    for (let s of Object.keys(this.selectedPeriods)) {
      if (this.selectedPeriods[s] == true) {
        this.settings.period.push(s);
      }
    }

    this.settings.function = [];
    for (let s of Object.keys(this.selectedSkills)) {
      if (this.selectedSkills[s] == true) {
        this.settings.function.push(s);
      }
    }

    this.settings.location = [];
    for (let s of Object.keys(this.selectedLocation)) {
      if (this.selectedLocation[s] == true) {
        this.settings.location.push(s);
      }
    }

    let countfalse = 0;
    for (let s of Object.keys(this.AMPM_alt)) {
      if (this.AMPM_alt[s] == true) {
        this.settings.dayPart = s;
      } else {
        countfalse++;
      }
    }

    if (countfalse == 3) {
      this.settings.dayPart = "";
    }

    this.settings.comments = [];
    for (let s of Object.keys(this.selectedComments)) {
      if (this.selectedComments[s] == true) {
        this.settings.comments.push(s);
      }
    }

    this.settings.absenceType = []
    for (let s of Object.keys(this.selectedHolidays)) {
      if (this.selectedHolidays[s] == true) {
        this.settings.absenceType.push(s);
      }
    }


    this.appService.postSettings(this.settings).subscribe();
  }

  resetSettings() {
    this.settings.favorites = [];
    this.settings.dayPart = "";
    this.settings.location = [];
    this.settings.function = [];
    this.settings.requestFilters = false;
    this.settings.period = [];
    this.settings.absenceType = [];
    this.settings.comments = [];

    this.favorites = [];
    this.deleteAllFilters();
    this.appService.postSettings(this.settings).subscribe();

  }

  delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  planninghidden = false;

  togglePlanning() {
    this.planninghidden = !this.planninghidden;
  }


  loadAllPUFromDict() {
    let allpus = this.dictionaryService.getAllPlanningUnits();
    for (let key in allpus) {
      this.allPUList.push(allpus[key]);
      this.selectedPU[allpus[key]] = false;
    }

    this.visiblePU = this.allPUList.slice(0, this.numberPU);
  }
}
