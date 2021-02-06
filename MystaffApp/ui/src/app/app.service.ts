import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';

import {Observable} from 'rxjs/index';

import {Router} from "@angular/router";
import {HolidayMessage} from "./models/HolidayMessage";
import {Favorites} from "./models/Favorites";
import {User} from "./models/User";
import {HolidayPlanningsMessage} from "./models/HolidayPlanningsMessage";
import {Settings} from "./models/Settings";
import {PersonDate} from "./models/PersonDate";
import {AbsenceCounter} from "./models/AbsenceCounter";
import {subscribeToPromise} from "rxjs/internal-compatibility";
import {AppConfig} from "./app.config";
import {TranslateService} from "./translate";
import {SessionStorageService} from "./sessionstorage.service";


@Injectable({
  providedIn: 'root',
})
export class AppService {

  public static token: string;
  public static user: User;
  //docker
  //public static port = 8082;
  //lokaal
  //public static port = 9000;
  public static port;
  public static url;

  //private url = "http://192.168.99.100:";
  //docker
  //public static url = "http://bpvop5.ugent.be:";
  //lokaal
  //public static url = "http://localhost:";

  constructor(private http: HttpClient, private router: Router, private sessionStorage: SessionStorageService) {
    let settings = AppConfig.settings;
    AppService.url = settings.backend.url;
    AppService.port = settings.backend.port;
  }

  /* Makes a request to the backend to validate user credentials and will return a token if the credentials were valid */
  public async login(username: string, password: string): Promise<Object> {
    let fd = new FormData();
    fd.append('username', username);
    fd.append('password', password);

    return this.http.post(AppService.url + AppService.port + "/login", fd).toPromise();
  }

  public logout(): void {
    AppService.token = undefined;
    AppService.user = undefined;
    this.sessionStorage.clear();
    this.router.navigate(['/login']).then();
  }

  public isAuthenticated(): boolean {
    AppService.token = this.sessionStorage.getToken();
    if (AppService.token != "") {
      AppService.user = this.sessionStorage.getUser();
      if (AppService.user != null) {
        return true;
      }
      return false;
    }
    return false;
  }

  // START DATA RETRIEVERS

  public getUser(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/user", {headers: headers});
  }

  public getMembers(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/members", {headers: headers});
  }

  public getProfileSettings(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/profilesettings", {headers: headers});
  }

  public getOrganization(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/organization", {headers: headers});
  }

  public getPlannings(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/plannings", {headers: headers});
  }

  public getPlanning(id: string): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/planning/" + id, {headers: headers});
  }

  public getOrganizationTypes(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/organizationtypes", {headers: headers});
  }

  public getAbsenceTypes(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/absencetypes", {headers: headers});
  }


  // END DATA RETRIEVERS

  public getOwnAbsenceTypes(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getOwnAbsenceTypes", {headers: headers});
  }

  public getHolidayMessageWithDatabaseID(id: number): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getHolidayMessageWithDatabaseID/" + id;
    return this.http.get(url, {headers: headers});
  }

  public getHolidayMessageById(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getHolidayMessageWithEmployeeID/" + AppService.user.userId;
    return this.http.get(url, {headers: headers});
  }

  public getHolidaysNew(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesNew";
    return this.http.get(url, {headers: headers});
  }

  public getHolidaysApproved(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesApproved";
    return this.http.get(url, {headers: headers});
  }

  public getHolidaysInConsideration(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesInConsideration";
    return this.http.get(url, {headers: headers});
  }

  public getHolidaysInConsiderationAndNew(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesNewOrInConsideration";
    return this.http.get(url, {headers: headers});
  }

  public getAllHolidayMessagesUnderPlannerWithId(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesUnderPlannerWithId/" + id;
    //console.log(url);
    return this.http.get(url, {headers: headers});
  }

  public getAllHMIDsPerPlanningUnitUnderPlannerWithID(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHMIDsPerPlanningUnitUnderPlannerWithID/" + id;
    //console.log(url);
    return this.http.get(url, {headers: headers});
  }

  public getAllHolidayMessagesShortOfEveryone(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesShortOfEveryone";
    //console.log(url);
    return this.http.get(url, {headers: headers});
  }


  public getAllHolidayMessagesPerPlanningUnit(ids: String[]): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllHolidayMessagesPerPlanningUnit" + "?";
    ids.forEach(function (id) {
      url += "ids=" + id + "&";
    });
    return this.http.get(url, {headers: headers});
  }

  public getAllCollisionStatesUnderPlannerBetween(id: String, start: String, end: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllCollisionStatesUnderPlannerBetween/" + id + "/" + start + "/" + end;
    return this.http.get(url, {headers: headers});
  }

  public getAllCollisionStatesUnderPlanningUnitBetween(id: String, start: String, end: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllCollisionStatesUnderPlanningUnitBetween/" + id + "/" + start + "/" + end;
    return this.http.get(url, {headers: headers});
  }


  public isPlanningStillPossibleForPlanningUnitsWithoutPeople(start: String, end: String, unitids: String[], people: PersonDate[]): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/isPlanningStillPossibleForPlanningUnitsWithoutPeople" + "/" + start + "/" + end + "?";
    unitids.forEach(function (id) {
      url += "ids=" + id + "&";
    });
    return this.http.post(url, JSON.stringify(people), {headers: headers});
  }

  public getPlanningUnitsOfEmployee(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllPlanningUnitIDsInWhichEmployeeIDIsIn/" + id;
    return this.http.get(url, {headers: headers});
  }

  public getPlanningUnitsOfPlanner(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner/" + id;
    return this.http.get(url, {headers: headers});
  }

  public getAllPlanningUnitIDsInWhichEmployeeIDIsInWithPlannerFlag(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllPlanningUnitIDsInWhichEmployeeIDIsInWithPlannerFlag/" + id;
    return this.http.get(url, {headers: headers});
  }

  public getSettingsOfEmployee(id: String): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getSettingsFromEmployeeWithId/" + id;
    return this.http.get(url, {headers: headers});
  }

  public postSettings(sett: Settings): Observable<any> {
    console.log(JSON.stringify(sett));
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.post(AppService.url + AppService.port + "/postSettings", JSON.stringify(sett), {headers: headers});
  }

  public deleteSettings(id: String): Observable<void> {
    console.log("DELETE http://localhost:9000/deleteSettings/" + id);
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.delete<void>(AppService.url + AppService.port + "/deleteSettings/" + id, {headers: headers}).subscribe();
    return;
  }

  public postAbsenceCounter(absenceCounter: AbsenceCounter): Observable<any> {
    console.log(JSON.stringify(absenceCounter));
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.post(AppService.url + AppService.port + "/postAbsenceCounter", JSON.stringify(absenceCounter), {headers: headers});
  }

  public getAbsenceCounterOfEmployees(year: number, ids: string[]): Observable<any> {
    console.log("GET http://localhost:9000/getAbsenceCounters/" + year);
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAbsenceCounters/" + year + '?';
    ids.forEach(function (id) {
      url += "employeeIDs=" + id + "&";
    });
    return this.http.get(url, {headers: headers});
  }

  public deleteAbsenceCounter(id: String): Observable<void> {
    console.log("DELETE http://localhost:9000/deleteAbsenceCounter/" + id);
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.delete<void>(AppService.url + AppService.port + "/deleteAbsenceCounter/" + id, {headers: headers}).subscribe();
    return;
  }

  // Date: yyyy-mm-dd
  public getNumberOfTimesPersonIsRejectedSince(id: String, date: String){
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getNumberOfTimesPersonIsRejectedSince/" + id + "/" + date;
    return this.http.get(url, {headers: headers});
  }

  public postHolidayMessage(hm: HolidayMessage): Observable<any> {
    console.log(JSON.stringify(hm));
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.post(AppService.url + AppService.port + "/holidayMessage", JSON.stringify(hm), {headers: headers});
  }

  public postHolidayPlanningsMessage(hpm: HolidayPlanningsMessage): Observable<any> {
    console.log(JSON.stringify(hpm));
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.post(AppService.url + AppService.port + "/holidayPlanningsMessage", JSON.stringify(hpm), {headers: headers});
  }

  public postholidayPlanningsMessageForPlanningsUnit(hpm: HolidayPlanningsMessage, unitid: String): Observable<any> {
    console.log("postholidayPlanningsMessageForPlanningsUnit ");
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.post(AppService.url + AppService.port + "/holidayPlanningsMessageForPlanningsUnit/" + unitid, JSON.stringify(hpm), {headers: headers});
  }

  public deleteHolidayMessage(id: number): Observable<any> {
    console.log("Delete holidayMessage" + id);
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.delete(AppService.url + AppService.port + "/holidayMessage/" + id, {headers: headers});
  }

  public getHolidayMessageWithEmployeeID(id: number): Observable<any> {
    console.log("GET all holiday messages from employee " + id);
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getHolidayMessageWithEmployeeID/" + id, {headers: headers});
  }

  public getHolidayMessagesApprovedOfDoctorWithID(id: string): Observable<any> {
    console.log("GET all approved holiday messages from employee " + id);
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getHolidayMessagesApprovedOfDoctorWithID/" + id, {headers: headers});
  }

  public getAllEmployeesOfPlanningUnitsWithIds(ids): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    let url = AppService.url + AppService.port + "/getAllEmployeesOfPlanningUnitsWithIds?";
    ids.forEach(function (id) {
      url += "ids=" + id + "&";
    });
    return this.http.get(url, {headers: headers});
  }

  public getAvailablePeoplePerDayPartForPlanningUnits(start: String, end: String, ids: String[]): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    console.log(ids);
    let url = AppService.url + AppService.port + "/getAvailablePeoplePerDayPartForPlanningUnits/" + start + "/" + end + "?";
    for (let id of ids) {
      url += "ids=" + id + "&";
    }
    console.log(url);
    return this.http.get(url, {headers: headers});
  }

  public postAbsence(absenceData: HolidayMessage): Observable<any> {

    let headers = new HttpHeaders().set("token", AppService.token);

    console.log(absenceData);
    let startDate = new Date(absenceData.exactDates[0].date);
    let endDate = new Date(absenceData.exactDates[absenceData.exactDates.length - 1].date);

    if (absenceData.exactDates[1].dayPart && absenceData.exactDates[1].dayPart == absenceData.exactDates[0].dayPart) {
      if (absenceData.exactDates[0].dayPart == "PM") {
        startDate.setHours(13);
        endDate.setHours(17);
      } else {
        startDate.setHours(9);
        endDate.setHours(13);
      }
    } else {
      startDate.setHours(9);
      endDate.setHours(17);
    }


    let dailyStartTime = this.formatTime(startDate);
    let dailyEndTime = this.formatTime(endDate);

    let begin = this.formatDate(startDate);
    let end = this.formatDate(endDate);

    let type = this.formatType(absenceData.type);
    console.log(startDate);
    console.log(endDate);
    console.log(dailyStartTime);
    console.log(dailyEndTime);
    console.log(begin);
    console.log(end);
    console.log(type);
    let params = new HttpParams();
    params = params.set('type', type)
      .set('begin', begin)
      .set('end', end)
      .set('dailyStartTime', dailyStartTime)
      .set('dailyEndTime', dailyEndTime);

    return this.http.post(AppService.url + AppService.port + "/absences", null, {params: params, headers: headers});

  }

  public deleteAbsence(absenceId: string): Observable<void> {
    console.log("DELETE http://localhost:9000/" + AppService.user.userId + "/absences/" + absenceId);
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.delete<void>(AppService.url + AppService.port + "/" + AppService.user.userId + "/absences/" + absenceId, {headers: headers}).subscribe();
    return;
  }

  private formatDate(date: Date): string {
    var month = '' + (date.getMonth() + 1),
      day = '' + date.getDate(),
      year = date.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  }

  private formatTime(date: Date): string {
    var hours = '' + date.getHours(),
      minutes = '' + date.getMinutes();

    if (hours.length < 2) hours = '0' + hours;
    if (minutes.length < 2) minutes = '0' + minutes;

    return [hours, minutes].join(':');
  }

  private formatType(type: string): string {
    if (type == "Yearly") {
      return "YEARLY_VACATION";
    } else if (type == "European") {
      return "EUROPEAN_VACATION";
    } else if (type == "Educative") {
      return "EDUCATIONAL VACATION";
    } else {
      return "OTHER";
    }
  }

  public getOrganizationHolidays(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/api/holidays/" + AppService.user.tenant, {headers: headers});
  }

  public getHolidayBetweenTwoDates(start: string, end: string): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getHolidayBetweenTwoDates/" + start + "/" + end, {headers: headers});
  }

  public getDatesOfHoliday(vacation: string, date: string): Observable<any>{
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getDatesOfHoliday/" + vacation + "/" + date, {headers: headers});
  }

  public getVacationsOfYear(year: number): Observable<any>{
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/getVacationsOfYear/" + year, {headers: headers});
  }


}
