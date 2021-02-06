import {Inject, Injectable} from "@angular/core";
import {SESSION_STORAGE, StorageService} from "ngx-webstorage-service";
import {User} from "./models/User";

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  TOKEN_KEY = "angular_token";
  USER_KEY = "angular_user";

  constructor(@Inject(SESSION_STORAGE) private storage: StorageService) {
  }

  public storeToken(value: string): void {
    let storedToken = this.storage.get(this.TOKEN_KEY) || "";
    storedToken = value;
    this.storage.set(this.TOKEN_KEY, storedToken);
    console.log(this.storage.get(this.TOKEN_KEY) || 'Local storage has no token');
  }
  public storeUser(value: User): void {
    let storedUser = this.storage.get(this.USER_KEY) || "";
    storedUser = JSON.stringify(value);
    this.storage.set(this.USER_KEY, storedUser);
    console.log(this.storage.get(this.USER_KEY) || 'Local storage has no user');
  }
  public getToken(): string {
    return this.storage.get(this.TOKEN_KEY);
  }
  public getUser(): User {
    let user = this.storage.get(this.USER_KEY);
    console.log(user);
    if (user != undefined) {
      return JSON.parse(user);
    }
    return null;
  }
  public clear(): void {
    this.storage.clear();
  }
}
