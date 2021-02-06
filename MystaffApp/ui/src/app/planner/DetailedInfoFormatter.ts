import {LOCALE_ID, Inject} from "@angular/core";
import {CalendarEventTitleFormatter, CalendarEvent} from "angular-calendar";
import {DatePipe} from "@angular/common";

export class CustomEventTitleFormatter extends CalendarEventTitleFormatter{
  constructor(@Inject(LOCALE_ID) private locale: string){
    super();
  }

  month(event: CalendarEvent): string{
    return `<B>${new DatePipe(this.locale).transform(
            event.start,
            'HH:mm',
            this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)
    
            }<\B>   Reden: ${event.title}`;


  }
  week(event: CalendarEvent): string{
    return `<B>${new DatePipe(this.locale).transform(
      event.start,
      'HH:mm',
      this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)

      }<\B> ${event.title}`;


  }
  day(event: CalendarEvent): string{
    return `<B>${new DatePipe(this.locale).transform(
      event.start,
      'HH:mm',
      this.locale)} -
            ${new DatePipe(this.locale).transform(event.end, 'HH:mm', this.locale)

      }<\B> ${event.title}`;


  }
}
