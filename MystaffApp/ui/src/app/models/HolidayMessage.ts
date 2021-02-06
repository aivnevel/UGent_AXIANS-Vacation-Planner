import {ExactDate} from 'src/app/models/ExactDate';
import {PlanningsUnitState} from 'src/app/models/PlanningsUnitState';

export class HolidayMessage {
  public id: number;
  public employeeID: string;
  public exactDates: ExactDate[];
  //Type of absence: Normal,ShortSickness, LongSickness, Rejected, PartTime, Educative, Other
  public type: string;
  //    Approved, Rejected, InConsideration, New
  public state: string;
  public requestDate: Date;
  public requestByID: string;
  public comment: string;
  public lastUpdate: Date;
  public plannerOfLastUpdate: string;
  public planningsUtilStates: PlanningsUnitState[];

}
