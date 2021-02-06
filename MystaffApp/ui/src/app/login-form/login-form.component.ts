import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AppService} from "../app.service";
import {Router} from "@angular/router";
import {NgForm} from "@angular/forms";
import {TranslateService} from "../translate";
import {SessionStorageService} from "../sessionstorage.service";

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.css']
})

export class LoginFormComponent {

  @ViewChild('loginComponentForm') loginComponentForm: NgForm;

  username: string;
  password: string;

  constructor(private router: Router, private appservice: AppService, private sessionStorage: SessionStorageService, public _translate: TranslateService) {
    if (this.appservice.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }
  }

  async logIn() {
    await this.appservice.login(btoa(this.username), btoa(this.password)).then((res => {
      AppService.token = res["token"];
      this.sessionStorage.storeToken(res["token"]);
      this.appservice.getUser().subscribe((res) => {
        AppService.user = res;
        this._translate.use(AppService.user.preferredLocale);
        this.router.navigate(['/calendar']).then();
        this.sessionStorage.storeUser(res);
      }, error => {
        this.loginComponentForm.reset();
        this.showError();
      });
    })).catch((reason => {
      this.loginComponentForm.reset();
      this.showError();
    }));
  }

  showError() {
    document.getElementById("login-error").hidden = false;
  }

}
