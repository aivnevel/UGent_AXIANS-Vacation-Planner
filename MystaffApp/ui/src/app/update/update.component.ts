import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-update',
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.css']
})
export class UpdateComponent implements OnInit {
  @Input('Update') lastUpdate: string;
  @Input('state') state: string;
  @Input('start') start: string;
  @Input('end') end: string;
  @Input('type') type: string;

  constructor() { }

  ngOnInit() {
  }

  formatState(state){
    if(state == "New"){
      return "hourglass_empty";
    } else if(state == "Approved"){
      return "done";
    } else return "clear";
  }

  formatType(type){
    if(type == "Yearly") return "flight_takeoff";
    else if(type == "Sickness") return "healing";
    else if(type == "European") return "account_balance";
    else if(type == "Educative") return "school";
    else if(type == "Other") return "live_help";
  }

  formatDate(date) {
    date = new Date(date);
    var monthNames = [
      "Januari", "Februari", "Maart",
      "April", "Mei", "Juni", "Juli",
      "Augustus", "September", "Oktober",
      "November", "December"
    ];

    let day = date.getDate();
    let monthIndex = date.getMonth();
    return day + ' ' + monthNames[monthIndex] + ' ' + date.getFullYear();
  }
}
