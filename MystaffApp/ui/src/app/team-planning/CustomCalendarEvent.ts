import {
  CalendarEvent

} from 'angular-calendar'
import {EventAction, EventColor} from 'calendar-utils';

export class CustomCalendarEvent<MetaType = any> implements CalendarEvent{
  name?: string;
  employeeID?: string;
  id?: string | number;
  start: Date;
  end?: Date;
  title: string;
  color?: EventColor;
  state?: string;
  comment?: string;
  actions?: EventAction[];
  allDay?: boolean;
  cssClass?: string;
  resizable?: {
    beforeStart?: boolean;
    afterEnd?: boolean;
  };
  draggable?: boolean;
  meta?: MetaType;
}
