export class Person {
  firstname: string;
  lastname: string;
  locations: string[];
  skills: string[];
  dayparts: string[];
  type: string;
  dayPart: string;
  aantal: number;
  absences: any[];
  id: string;
  Holidays: any[];
  NewPlanningsUnits: any[];
  InConsiderationPlanningsUnits: any[];

  constructor(firstname, lastname, locations, skills, id) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.locations = locations;
    this.skills = skills;
    this.skills.sort((a, b) => a.localeCompare(b));
    this.aantal = 0;
    this.id = id;
    this.absences = new Array();
    this.dayparts = new Array();
    this.Holidays = new Array();
  }

  addHoliday(startDateHoliday, endDateHoliday, type, comment, dayPart, idMessage, state, vacationName, requestDate, lastUpdate, person, shown){
    this.Holidays.push({startDateHoliday: startDateHoliday, endDateHoliday: endDateHoliday,
      type: type, comment: comment, dayPart: dayPart, idMessage: idMessage, state: state, vacationName: vacationName, requestDate: requestDate, lastUpdate: lastUpdate, person: person, shown: shown});
  }

  addSkill(skill){
    if(this.skills.indexOf(skill)==-1){
      this.skills.push(skill);
    }
  }

  addLocation(location){
    if(this.locations.indexOf(location)==-1){
      this.locations.push(location);
    }
  }

  toString() {
    return this.firstname + ' ' + this.lastname;
  }

  addPlannerUnitState(PlanningsUnit, state) {
    if(state == "InConsideration"){
      this.NewPlanningsUnits.push(PlanningsUnit);
    } else if(state == "New") {
      this.InConsiderationPlanningsUnits.push(PlanningsUnit);
    }
  }
}
