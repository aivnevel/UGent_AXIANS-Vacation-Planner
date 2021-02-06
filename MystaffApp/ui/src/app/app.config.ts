import { HttpClient } from '@angular/common/http';
import {Injectable} from "@angular/core";
import {IAppConfig} from "./app-config.model";
@Injectable()
export class AppConfig {
  public static settings: IAppConfig;
  constructor(private http: HttpClient) {}
  load() {
    const jsonFile = `assets/config/config.dev.json`;
    return new Promise<void>((resolve, reject) => {
      this.http.get(jsonFile).toPromise().then((response : IAppConfig) => {
        AppConfig.settings = <IAppConfig>response;
        resolve();
      }).catch((response: any) => {
        reject(`Could not load file '${jsonFile}': ${JSON.stringify(response)}`);
      });
    });
  }
}
