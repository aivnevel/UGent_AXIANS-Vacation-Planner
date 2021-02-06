import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule, DatePipe, registerLocaleData} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {FlatpickrModule} from 'angularx-flatpickr';
import {SidebarModule} from "ng-sidebar";
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CalendarModule, DateAdapter} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {CalendarComponent} from './calendar/calendar.component';
import {EventformComponent} from "./eventform/eventform.component";
import {NewsfeedComponent} from "./newsfeed/newsfeed.component";
import {MiniCalendarComponent} from './mini-calendar/mini-calendar.component';
import {PlannerComponent} from './planner/planner.component';

import {LoginFormComponent} from './login-form/login-form.component';
import localeNL from '@angular/common/locales/nl';
import localeFR from '@angular/common/locales/fr';
import {
  MatButtonModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule,
  MatProgressSpinnerModule, MatMenuModule
} from "@angular/material";
import {HistoryComponent} from './history/history.component';
import {DateFormatPipe, RequestsComponent} from './requests/requests.component';
import {ScrollingModule} from "@angular/cdk/scrolling";
import {AppHttpInterceptorService} from "./http-interceptor.service";
import {DictionaryService} from "./dictionary.service";
import {AppService} from "./app.service";
import {TeamPlanningComponent} from './team-planning/team-planning.component';
import {SettingsComponent} from './settings/settings.component';
import {HelpComponent} from './help/help.component';
import {PlannerCommentComponent} from './planner-comment/planner-comment.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatTabsModule} from '@angular/material/tabs';
import {GridtableComponent} from './gridtable/gridtable.component';
import {AgGridModule} from 'ag-grid-angular';
import {PlannereventformComponent} from './plannereventform/plannereventform.component';
import {MatTooltipModule} from '@angular/material';
import {UpdateComponent} from './update/update.component';
import {TRANSLATION_PROVIDERS, TranslateService, TranslatePipe} from './translate';
import {AppConfig} from "./app.config";
import {SessionStorageService} from "./sessionstorage.service";

const routes: Routes = [
  {
    path: 'calendar',
    component: CalendarComponent,
    data: {technology: 'Angular'}
  },
  {
    path: 'newsfeed',
    component: NewsfeedComponent,
    data: {technology: 'Angular'}
  },
  {
    path: 'login',
    component: LoginFormComponent,
    data: {technology: 'Angular'}
  },
  {
    path: 'teamplanning',
    component: TeamPlanningComponent,
    data: {technology: 'Angular'}
  },
  {
    path: '**',
    redirectTo: '/login',
    pathMatch: 'full'
  }
];

registerLocaleData(localeNL);
registerLocaleData(localeFR);


export function initializeApp(appConfig: AppConfig) {
  return () => appConfig.load();
}


@NgModule({
  declarations: [
    AppComponent,
    CalendarComponent,
    EventformComponent,
    NewsfeedComponent,
    MiniCalendarComponent,
    PlannerComponent,
    SettingsComponent,
    LoginFormComponent,
    HistoryComponent,
    RequestsComponent,
    TeamPlanningComponent,
    SettingsComponent,
    HelpComponent,
    PlannerCommentComponent,
    GridtableComponent,
    PlannereventformComponent,
    UpdateComponent,
    TranslatePipe,
    DateFormatPipe
  ],
  exports: [CalendarComponent, MatTabsModule],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    ReactiveFormsModule,
    DragDropModule,
    HttpClientModule,
    MatTabsModule,
    ScrollingModule,
    MatTooltipModule,
    AgGridModule.withComponents([]),
    HttpClientXsrfModule.withOptions({
      cookieName: 'Csrf-Token',
      headerName: 'Csrf-Token',
    }),

    BrowserAnimationsModule,
    CalendarModule.forRoot({
      provide: DateAdapter,
      useFactory: adapterFactory
    }),
    CommonModule,
    FormsModule,
    NgbModule,
    FlatpickrModule.forRoot(),
    CalendarModule.forRoot({
      provide: DateAdapter,
      useFactory: adapterFactory
    }),
    RouterModule.forRoot(routes),
    SidebarModule.forRoot(),
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatMenuModule
  ],
  providers: [
    TRANSLATION_PROVIDERS, TranslateService, AppService, DictionaryService, DatePipe,
    {
      multi: true,
      provide: HTTP_INTERCEPTORS,
      useClass: AppHttpInterceptorService
    }, SessionStorageService,
    AppConfig,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig], multi: true }
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}
