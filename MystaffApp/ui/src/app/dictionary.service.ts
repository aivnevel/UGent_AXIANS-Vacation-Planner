import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {AppService} from "./app.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ShortMemberData} from "./models/ShortMemberData";

@Injectable({
  providedIn: 'root',
})
export class DictionaryService {

  private readonly onReady = new LiteEvent<void>();

  private loadSkillsURL = '/getAllSkills';
  private loadLocationsURL = '/getAllLocations';
  private loadShortMemberDataURL = '/getAllShortMemberData';
  private loadPlanningUnitsURL = '/getAllPlanningUnits';
  private loadMemberNamesURL = '/getAllMemberNamesData';
  private skills: { [id: string]: string } = {};
  private locations: { [id: string]: string } = {};
  private planningUnits: { [id: string]: string } = {};
  private employees: { [id: string]: ShortMemberData } = {};

  public dataLoaded: boolean = false;

  private memberNamesLoaded: boolean = false;
  private skillsLoaded: boolean = false;
  private locationsLoaded: boolean = false;
  private planningUnitsLoaded: boolean = false;

  public get Ready() {
    return this.onReady.expose();
  }

  constructor(private http: HttpClient, private appService: AppService) {
    if (this.appService.isAuthenticated()) {
      this.refreshData().subscribe((data) => {
          //console.log("dict started");
          //this.loadShortMemberData();
          this.loadMemberNames();
          this.loadSkills();
          this.loadLocations();
          this.loadPlanningUnits();
        }
      );
    }
  }

  private triggerIfReady(): void {
    if (this.memberNamesLoaded && this.skillsLoaded && this.locationsLoaded && this.planningUnitsLoaded) {
      this.dataLoaded = true;
      this.onReady.trigger();
    }
  }

  private refreshData(): Observable<any> {
    let headers = new HttpHeaders().set("token", AppService.token);
    return this.http.get(AppService.url + AppService.port + "/refreshData", {headers: headers});
  }

  private loadSkills(): void {
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.get(AppService.url + AppService.port + this.loadSkillsURL, {headers: headers}).subscribe((data: any) => {
      for (let obj of data) {
        this.skills[obj.id] = obj.value;
      }
      this.skillsLoaded = true;
      this.triggerIfReady();
    });
  }

  private loadLocations(): void {
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.get(AppService.url + AppService.port + this.loadLocationsURL, {headers: headers}).subscribe((data: any) => {
      for (let obj of data) {
        this.locations[obj.id] = obj.value;
      }
      this.locationsLoaded = true;
      this.triggerIfReady();
    });
  }

  private loadPlanningUnits(): void {
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.get(AppService.url + AppService.port + this.loadPlanningUnitsURL, {headers: headers}).subscribe((data: any) => {
      for (let obj of data) {
        this.planningUnits[obj.id] = obj.value;
      }
      this.planningUnitsLoaded = true;
      this.triggerIfReady();
    });
  }

  private loadMemberNames(): void {
    console.log("start loading memberNames");
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.get(AppService.url + AppService.port + this.loadMemberNamesURL, {headers: headers}).subscribe((data: any) => {
      //console.log("loadMemberNames()", data);
      console.log(data);
      for (let obj of data) {
        let smd = new ShortMemberData();
        smd.id = obj.id;
        smd.firstName = obj.firstName;
        smd.lastName = obj.lastName;
        this.employees[obj.id] = smd;

        //console.log(obj.id);
      }
      console.log("memberNames loaded");
      this.memberNamesLoaded = true;
      this.triggerIfReady()
    });
  }

  private loadShortMemberData(): void {
    let headers = new HttpHeaders().set("token", AppService.token);
    this.http.get(AppService.url + AppService.port + this.loadShortMemberDataURL, {headers: headers}).subscribe((data: any) => {
      for (let obj of data) {
        let smd = new ShortMemberData();
        smd.id = obj.id;
        smd.firstName = obj.firstName;
        smd.lastName = obj.lastName;
        this.employees[obj.id] = smd;
      }/*
      console.log(this.getEmployeeFirstNameOfId("6d5131bc-2d96-4aac-97c0-ab0e72df598c")); //Alexander
      console.log(this.getEmployeeLastNameOfId("6d5131bc-2d96-4aac-97c0-ab0e72df598c")); //Van Nevel
      console.log(this.getEmployeeFullNameOfId("6d5131bc-2d96-4aac-97c0-ab0e72df598c")); //Alexander Van Nevel
      console.log("loaded FR");*/
    });

  }


  /* ngOnInit(){

   }*/

  public getAllSkills() {
    return this.skills;
  }

  public getAllLocations() {
    return this.locations;
  }

  public getAllPlanningUnits() {
    return this.planningUnits;
  }

  public getAllEmployees() {
    return this.employees;
  }

  public getSkillNameOfId(id: string) {
    return this.skills[id];
  }

  public getLocationNameOfId(id: string) {
    return this.locations[id];
  }

  public getPlanningUnitNameOfId(id: string) {
    return this.planningUnits[id];
  }

  public getCacheEmployeeFullNameOfId(id: string) {
    return this.employees[id].firstName + " " + this.employees[id].lastName;
  }

  public getCacheEmployeeFirstNameOfId(id: string) {
    //console.log(this.employees[id]);
    //console.log(this.employees[id].firstName);
    return this.employees[id].firstName;
  }

  public getCacheEmployeeLastNameOfId(id: string) {
    return this.employees[id].lastName;
  }

  public getEmployeeFullNameOfId(id: string) {
    return this.employees[id].firstName + " " + this.employees[id].lastName;
  }

  public getEmployeeFirstNameOfId(id: string) {
    return this.employees[id].firstName;
  }

  public getEmployeeLastNameOfId(id: string) {
    return this.employees[id].firstName;
  }
}

interface ILiteEvent<T> {
  on(handler: { (data?: T): void }): void;

  off(handler: { (data?: T): void }): void;
}

class LiteEvent<T> implements ILiteEvent<T> {
  private handlers: { (data?: T): void; }[] = [];

  public on(handler: { (data?: T): void }): void {
    this.handlers.push(handler);
  }

  public off(handler: { (data?: T): void }): void {
    this.handlers = this.handlers.filter(h => h !== handler);
  }

  public trigger(data?: T) {
    this.handlers.slice(0).forEach(h => h(data));
  }

  public expose(): ILiteEvent<T> {
    return this;
  }
}
