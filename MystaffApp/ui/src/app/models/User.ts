import {Permissions} from "./Permissions";

export class User {
  public userId: string
  public tenant: string
  public username: string
  public firstName: string
  public lastName: string
  public preferredLocale: string
  public permissions: Permissions
}
