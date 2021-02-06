import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {AppService} from './app.service';
import {TranslateService} from "./translate";

declare var jquery:any;
declare var $ :any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})

export class AppComponent implements OnInit {
  user: string;
  plannings: string;
  organizationtypes: string;

  organization: string;
  members: string;
  membersUZGent: string;
  membersWithSkills: string;

  allSkils: string[];
  allLocations: string[];
  firstName: string;
  lastName: string;
  type: string;
  date: string;
  dayPart: string;

  supportedLanguages: any[];

  constructor(private appService: AppService, public _translate: TranslateService) {
  }

  ngOnInit(): void {
    this.supportedLanguages = [
      {display: 'English', value: 'en'},
      {display: 'Nederlands', value: 'nl'}
    ];

    this.selectLang('nl');
  }

  isCurrentLang(lang: string) {
    // check if the selected lang is current lang
    return lang === this._translate.currentLang;
  }

  selectLang(lang: string) {
    // set current lang;
    this._translate.use(lang);
  }

  setLoginStatus() {
      this.appService.logout();
  }

  openCloseLeftMenu(id) {
    if (document.getElementById(id).style.display == "block") {
      document.getElementById(id).style.display = "none";
    } else {
      document.getElementById(id).style.display = "block";
    }
  }
}
