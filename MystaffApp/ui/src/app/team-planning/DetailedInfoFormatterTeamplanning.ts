import {LOCALE_ID, Inject} from "@angular/core";
import {CalendarEventTitleFormatter} from "angular-calendar";
import {CustomCalendarEvent} from "./CustomCalendarEvent";
import {DatePipe} from "@angular/common";
import {TranslateService} from "../translate";

export class CustomEventTitleFormatter extends CalendarEventTitleFormatter {
  constructor(@Inject(LOCALE_ID) private locale: string, public _translate : TranslateService) {
    super();
  }

  month(event: CustomCalendarEvent): string {
    return `<B>${event.name}: ${this._translate.instant('TEAM_PLANNING_ABSENT_FROM')} ${new DatePipe(this.locale).transform(
      event.start,
      'HH:mm',
      this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)

      }<\B> ${this._translate.instant('TEAM_PLANNING_BECAUSE')} ${this._translate.instant(event.title)}`;
  }

  week(event: CustomCalendarEvent): string {
    return `<B>${new DatePipe(this.locale).transform(
      event.start,
      'HH:mm',
      this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)

      }<\B> ${event.name}`;
  }

  day(event: CustomCalendarEvent): string {
    return `<B>${new DatePipe(this.locale).transform(
      event.start,
      'HH:mm',
      this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)

      }<\B> ${event.name}`;
  }
}
