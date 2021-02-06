import { Component, OnInit } from '@angular/core';
import {AppService} from "../app.service";
import {TranslateService} from "../translate";

@Component({
  selector: 'app-gridtable',
  templateUrl: './gridtable.component.html',
  styleUrls: ['./gridtable.component.css']
})
export class GridtableComponent implements OnInit {

  monthNames = [];
  weekNames = [];

  private table: HTMLTableElement;
  constructor(private appService: AppService, public _translate: TranslateService){
    this._translate.LanguageChange.on(() => {
      this.determineWeekAndMonthNames();
      this.initTableWithData();
    });
  }

  ngOnInit(): void {
    this.determineWeekAndMonthNames();
    this.initTableWithData();
  }

  determineWeekAndMonthNames(): void {
    this.monthNames = [this._translate.instant('MONTH_JAN'),
      this._translate.instant('MONTH_FEB'),
      this._translate.instant('MONTH_MAR'),
      this._translate.instant('MONTH_APR'),
      this._translate.instant('MONTH_MAY'),
      this._translate.instant('MONTH_JUNE'),
      this._translate.instant('MONTH_JULY'),
      this._translate.instant('MONTH_AUG'),
      this._translate.instant('MONTH_SEP'),
      this._translate.instant('MONTH_OCT'),
      this._translate.instant('MONTH_NOV'),
      this._translate.instant('MONTH_DEC')];

    this.weekNames = [this._translate.instant('WEEKDAY_SUN'),
      this._translate.instant('WEEKDAY_MON'),
      this._translate.instant('WEEKDAY_TUE'),
      this._translate.instant('WEEKDAY_WED'),
      this._translate.instant('WEEKDAY_THU'),
      this._translate.instant('WEEKDAY_FRI'),
      this._translate.instant('WEEKDAY_SAT')];
  }

  //This method initializes the gridtable without the absences, these absences are added with the method below this one
  initTableWithData(){

    //Get all users from the backend
    this.appService.getMembers().subscribe( members =>{
      //Get all holidaymessagesshort from the backend
      this.appService.getAllHolidayMessagesShortOfEveryone().subscribe( data =>  {

        //Initialize new table
        this.table = <HTMLTableElement> document.getElementById("absenceTable");
        if (this.table.rows.length > 0) {
          for (let i = this.table.rows.length - 1; i >= 0; i--) {
            this.table.deleteRow(i);
          }
        }

        //Determine the current month and the next month
        let now = new Date();
        let aantalDagen = new Date(now.getFullYear(), now.getMonth()+1, 0).getDate();
        let nowMonthLater = new Date(now.getFullYear(), now.getMonth()+1, 1);
        let aantalDagen2 = new Date(nowMonthLater.getFullYear(), nowMonthLater.getMonth()+1, 0).getDate();

        let row = this.table.insertRow(-1);

        //Empty row, created to make it easier to add the month names on the same row and set the width and height of the column
        let cell = row.insertCell(-1);
        cell.innerHTML = " ";
        cell.setAttribute("style", "width: auto; height: 30px");

        //First cell is the current month, second cell is the next month
        cell = row.insertCell(-1);
        cell.innerHTML = this.monthNames[now.getMonth()]+ " " + now.getFullYear();
        cell.setAttribute("colspan", String(aantalDagen));
        cell.setAttribute("style", "border-left: solid 1px rgb(221,221,221); font-weight: bold;color: #2160b2");

        cell = row.insertCell(-1);
        cell.innerHTML = this.monthNames[nowMonthLater.getMonth()]+ " " + now.getFullYear();
        cell.setAttribute("colspan", String(aantalDagen2));
        cell.setAttribute("style", "border-left: solid 1px rgb(221,221,221); font-weight: bold;color: #2160b2");


        //Adds the header text name
        row = this.table.insertRow(-1);
        cell = row.insertCell(-1);
        cell.innerHTML = this._translate.instant('NAME');
        cell.setAttribute("style", "font-weight: bold;color: #2160b2 ;border-right: solid 1px rgb(221,221,221)");

        //Adds the month numbers of current month
        for(let i = 1; i < aantalDagen+1; i++){
          let d = new Date(now.getFullYear(), now.getMonth(), i);
          let cell = row.insertCell(-1);
          cell.innerHTML = this.weekNames[d.getDay()];
          if(d.getDay() == 6 || d.getDay() == 0) {
            cell.setAttribute("style", "text-align: center;color: #2160b2; border-bottom: solid 1px rgb(221,221,221);font-weight: bold; background-color: rgb(221,221,221)");
          } else {
            cell.setAttribute("style", "text-align: center;color: #2160b2; border-bottom: solid 1px rgb(221,221,221);font-weight: bold;");
          }
        }

        //Adds the month numbers of the next month
        for(let i = aantalDagen+1; i < aantalDagen+aantalDagen2+1; i++){
          let d = new Date(now.getFullYear(), now.getMonth(), i);
          let cell = row.insertCell(-1);
          cell.innerHTML = this.weekNames[d.getDay()];

          if(d.getDay() == 6 || d.getDay() == 0){
            if(i == aantalDagen+1) {
              cell.setAttribute("style", "text-align: center;color: #2160b2; border-left: solid 1px rgb(221,221,221);font-weight: bold; background-color: rgb(221,221,221)");
            } else {
              cell.setAttribute("style", "text-align: center;color: #2160b2; font-weight: bold; background-color: rgb(221,221,221)");
            }
          } else {
              if(i == aantalDagen+1) {
                cell.setAttribute("style", "text-align: center;color: #2160b2; border-left: solid 1px rgb(221,221,221); font-weight: bold;");
              } else {
                  cell.setAttribute("style", "text-align: center;color: #2160b2; font-weight: bold; ");
              }
          }
        }

        //Adds all the names of the members and initializes every cell for every row
        for(let p of members){
          let row = this.table.insertRow(-1);
          let cell = row.insertCell(-1);
          cell.innerHTML = p.firstName + " " + p.lastName;
          cell.setAttribute("style", "white-space: nowrap;color: #2160b2; border-top: solid 1px rgb(221,221,221);border-right: solid 1px rgb(221,221,221); max-height: 30px; font-weight: bold;");

          let row2 = this.table.insertRow(-1);
          let cell2 = row2.insertCell(-1);
          cell2.innerHTML = " - ";
          cell2.setAttribute("style", "border-right: solid 1px rgb(221,221,221);font-weight: bold; color: white; ");
          let day = 1;
          let month = now.getMonth()+1;
          let tweedemaand = false;
          let dayNumber;
          for(let i = 1; i < aantalDagen+aantalDagen2+1; i++){
            if(i >= aantalDagen+1) {
              dayNumber = i - aantalDagen;
            } else {
              dayNumber = i;
            }
            let cell = row.insertCell(-1);
            let cell2 = row2.insertCell(-1);
            cell.innerHTML = " "+dayNumber;
            cell2.innerHTML = " ";
            cell.setAttribute("id", p.firstName+p.lastName+"-"+day+month);
            cell2.setAttribute("id", p.firstName+p.lastName+"2-"+day+month);
            if(i%2 == 0){
              cell.setAttribute("style", "font-size: 10px; vertical-align:5px; text-align: right; border-right: solid 1px rgb(221,221,221);border-top: solid 1px rgb(221,221,221);border-left: solid 1px grey; min-width: 30px;color: #2160b2; background-color: rgba(220,220,220, 0.7)");
              cell2.setAttribute("style", "font-size: 10px; vertical-align:5px; text-align: right; border-right: solid 1px rgb(221,221,221);border-bottom: solid 1px rgb(221,221,221);border-left: solid 1px grey; background-color: rgba(220,220,220, 0.7)");
            } else {
              cell.setAttribute("style", "font-size: 10px; vertical-align:5px; text-align: right; border-right: solid 1px rgb(221,221,221);border-top: solid 1px rgb(221,221,221);border-left: solid 1px rgb(221,221,221); min-width: 30px;color: #2160b2; background-color: rgba(255,255,255, 0.7)");
              cell2.setAttribute("style", "font-size: 10px; vertical-align:5px; text-align: right; border-right: solid 1px rgb(221,221,221);border-bottom: solid 1px rgb(221,221,221);border-left: solid 1px rgb(221,221,221);color: #2160b2; background-color: rgba(255,255,255, 0.7)");
            }
            if(!tweedemaand && day == aantalDagen){
              day = 0;
              tweedemaand = true;
              month +=1;
              if(month == 13){
                month = 0;
              }
            }
            day++;
          }
        }
        this.addDataToTable(members, data);
      });
    });

  }

  loadNewDataInTable(){
    this.appService.getMembers().subscribe(members => {
      this.appService.getAllHolidayMessagesShortOfEveryone().subscribe(data => {
        this.addDataToTable(members, data);
      });
    });
  }

  //Adds the absences of the members to the existing table
  addDataToTable(members: any, data: any){
    for(let p of members){
      for(let hm of data[p.id].holidayMessages){
        for(let exdate of hm.exactDates){
          let cell;
          if(exdate.daypart == "AM"){
            cell = document.getElementById(p.firstName+p.lastName+"-"+exdate.date[2]+exdate.date[1]);
          } else {
            cell = document.getElementById(p.firstName+p.lastName+"2-"+exdate.date[2]+exdate.date[1]);
          }
          if(hm.state == "Approved"){
            cell.style.backgroundColor = "green";
          } else if(hm.state = "New"){
            cell.style.backgroundColor = "yellow";
          } else {
            cell.style.backgroundColor = "red";
          }
          cell.style.opacity = 0.9;
        }
      }
    }
  }

}
