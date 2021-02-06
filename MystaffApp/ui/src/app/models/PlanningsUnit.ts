import {Person} from "src/app/models/Person";

export class PlanningsUnit{
  persons: Person[];
  id: string;

  constructor(id){
    this.id = id;
    this.persons = new Array();
  }

  addPerson(person){
    this.persons.push(person);
  }
}
