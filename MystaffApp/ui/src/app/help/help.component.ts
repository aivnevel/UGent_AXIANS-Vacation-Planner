import { Component, OnInit } from '@angular/core';
import {TranslateService} from "../translate";

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit {

  constructor(public _translate: TranslateService) { }

  ngOnInit() {
  }

}
