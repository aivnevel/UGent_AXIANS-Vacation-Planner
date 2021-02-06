import {Component, Input, Output, EventEmitter, ViewChild, ElementRef} from '@angular/core';
import {AppService} from "../app.service";
import {TranslateService} from "../translate";
import { Pipe, PipeTransform} from '@angular/core';
import { DatePipe } from '@angular/common';

declare var $ :any;

@Pipe({
  name: 'dateFormat'
})
export class DateFormatPipe extends DatePipe implements PipeTransform {
  transform(value: any, locale: string, args?: any): any {
    return super.transform(value, "EEE dd MMM", "", locale);
  }
}

@Component({
  selector: 'app-requests',
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.css'],
  providers: [DateFormatPipe]
})
export class RequestsComponent{
  @ViewChild('commentInput') commentInput: ElementRef;
  @Input('fullname') fullname: string;
  @Input('comment') comment: string;
  @Input('state') state: string;
  @Input('InConsideration') InConsideration: boolean;
  @Input('skills') skills: string[];
  @Input('start') start: Date;
  @Input('last') last: Date;
  @Input('requestDate') requestDate: Date;
  @Input('locations') locations: string[];
  @Input('PULocations') PULocations: string[];
  @Input('PUSkills') PUSkills: string[];
  @Input('dayPart') dayPart: string;
  @Input('id') id: number;
  @Input('collisions') collisions: string[];
  @Input('isCollision') isCollision: boolean = false;
  @Input('type') type: string;
  @Input('vacationName') vacationName: string;
  @Input('isPossible') isPossible: boolean;
  isAlwaysCollision: boolean = false;
  isSometimesCollision: boolean = false;
  isAcceptedAndSometimesCollision: boolean = false;

  @Output() messageEvent = new EventEmitter<string>();

  visible_comment: string;
  visible_skills: string;
  visible_locations: string;
  expanded: boolean;
  message: string;
  vacationType: string;
  countClickedDeclined: number;
  countClickedAccepted: number;
  amount: number;

  constructor(private appService: AppService, private _dateFormatPipe: DateFormatPipe, public _translate: TranslateService){
    this.countClickedDeclined = 0;
    this.countClickedAccepted = 0;
    this.setVisibleSkills(this.expanded);
    this.setVisibleComment(this.expanded);
  }

  process(){
    if(this.isPossible==undefined) {
      $("#test"+this.id).prop('value', ' ⏳');
      $("#test"+this.id).prop('style', 'margin-left: 1%;float: left;');
      this.messageEvent.emit(this.id + "");
      this.isPossible = true;
    } else {
      this.messageEvent.emit(this.id + "");
      $("#test"+this.id).prop('value', 'test');
      $("#test"+this.id).prop('style', 'margin-left: 1%;float: left;');
      this.isPossible = undefined;
    }
  }

  clicked(){
    if(this.expanded){
      $("#plus"+this.id).text("add_circle_outline");
    } else {
      $("#plus"+this.id).text("remove_circle_outline");
    }
    this.expanded = !this.expanded;
    this.setVisibleComment(this.expanded);
    this.setVisibleSkills(this.expanded);
  }
  clickedAccept(){
    if(this.countClickedAccepted == 0){
      this.countClickedAccepted++;
    }
    else if(this.countClickedAccepted >= 1) {
      this.message = "Approved " + this.id + " " + this.isSometimesCollision;
      this.messageEvent.emit(this.message);
    }
  }

  clickedWait(){
    this.message = "InConsideration " + this.id + " " + this.isSometimesCollision;
    this.messageEvent.emit(this.message);
  }

  clickedRefuse(){
    if(this.countClickedDeclined == 0){
      this.commentInput.nativeElement.style.visibility = "visible";
      this.appService.getNumberOfTimesPersonIsRejectedSince(this.id.toString(), "01-01-"+(new Date()).getFullYear()).subscribe((data: any)=>{
        this.amount = data;
      });
    }
    else if(this.countClickedDeclined >= 1) {
      this.message = "Rejected " + this.id + " " + this.isSometimesCollision + " " + this.message;
      this.messageEvent.emit(this.message);
    }
    this.countClickedDeclined++;
  }

  setVisibleSkills(expanded){
    var max = 36;
    for(var skill in this.skills){
      max -= 10;
    }
    if(max >= 0){
      if(this.skills != undefined) this.visible_skills = this.skills.toString();
      else this.visible_skills = "";
    } else {
      this.visible_skills = '';
      for(let skill of this.skills) {
        if(this.PUSkills.indexOf(skill)!=-1) {
          if (!expanded) {
            skill = skill.slice(0, 7);
            var temp = ((skill.split("")).reverse()).join("");
            var counter = 0;
            var index = -1;
            for (let char of temp) {
              if (index == -1) {
                if (/[aeiouy]/.test(char)) index = counter;
                else counter++;
              } else break;
            }
            skill = skill.slice(0, 7 - index);
            skill += ". ";
          } else {
            skill += " ";
          }
          if ((this.visible_skills + skill).length <= 36 && !expanded) {
            this.visible_skills += skill;
          } else if (expanded) {
            this.visible_skills += skill;
          }
        }
      }
      this.visible_skills+="..";
    }
  }

  setLocations(locations){
    let res = [];
    for(let location of locations){
      if(this.PULocations.indexOf(location)!=-1){
        res.push(location);
      }
    }
    return res.join(", ");
  }

  setVisibleComment(expanded) {
    if (this.comment != undefined) {
      var commentTemp = this.comment.split(" ");
      var counter = 0;
      this.visible_comment = "";
      for (let comment of commentTemp) {
        if (counter + (comment).length + 1 < 35 && !expanded) {
          counter += comment.length + 1;
          this.visible_comment += comment + " ";
        } else if (expanded) {
          counter += comment.length + 1;
          this.visible_comment += comment + " ";
        } else break;
      }
    }
  }

  formatDate(date) {
    var monthNames = [
      "Januari", "Februari", "Maart",
      "April", "Mei", "Juni", "Juli",
      "Augustus", "September", "Oktober",
      "November", "December"
    ];

    let day = date.getDate();
    let monthIndex = date.getMonth();
    return day + ' ' + monthNames[monthIndex];
  }

  formatRequestDate(date){
    let reqdate = new Date(date);
    let dateDay;
    if(reqdate.getDay()<10){
      dateDay = "0"+reqdate.getDay();
    } else dateDay = reqdate.getDay();

    let dateMonth;
    if(reqdate.getMonth()-1<10){
      dateMonth = "0"+(reqdate.getMonth()-1);
    } else dateMonth = (reqdate.getMonth()-1);

    return " " + date.getHours() + ":" + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes());
  }

  receiveMessage($event){
    this.message=$event;
  }

  formatDay(day){
    return this._dateFormatPipe.transform(day, this._translate.currentLang);
  }

  formatDayPart(dayPart){
    if(dayPart == "AM") return "VM";
    else if(dayPart == "PM") return "NM";
  }

  formatType(type){
    if(type == "Yearly") return "flight_takeoff";
    else if(type == "Sickness") return "healing";
    else if(type == "European") return "account_balance";
    else if(type == "Educative") return "school";
    else if(type == "Other") return "live_help";
  }
}
