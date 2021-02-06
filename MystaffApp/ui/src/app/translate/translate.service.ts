import {Injectable, Inject} from '@angular/core';
import {TRANSLATIONS} from './translations'; // import our opaque token

@Injectable()
export class TranslateService {

  private readonly onLanguageChange = new LiteEvent<void>();
  private _currentLang: string;

  public get LanguageChange() {
    return this.onLanguageChange.expose();
  }

  public get currentLang() {
    return this._currentLang;
  }

  // inject our translations
  constructor(@Inject(TRANSLATIONS) private _translations: any) {
  }

  public use(lang: string): void {
    // set current language

    if (this.currentLang != lang) {
      this._currentLang = lang;
      this.onLanguageChange.trigger();
    }
  }

  public translate(key: string): string {
    // private perform translation
    let translation = key;

    if (this._translations[this.currentLang] && this._translations[this.currentLang][key]) {
      return this._translations[this.currentLang][key];
    }

    return translation;
  }

  public instant(key: string) {
    // call translation
    return this.translate(key);
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

