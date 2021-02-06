import {ExactDate} from 'src/app/models/ExactDate';

export class PersonDate {
  public id: number;
  public employeeID: string;
  public dates: ExactDate[];

  constructor(id, employeeID, dates) {
    this.id = id;
    this.employeeID = employeeID;
    this.dates = dates;
  }
}

