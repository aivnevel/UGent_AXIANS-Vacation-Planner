import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TranslateService} from "../translate";

declare var jquery: any;
declare var $: any;

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})


export class SettingsComponent implements OnInit {
  @Input('requestFilters') requestFilters: boolean;
  @Input('View') View: boolean;
  @Input('isPlanner') isPlanner: boolean;
  @Input('plannerMode') Mode: boolean;
  @Output() messageEvent = new EventEmitter<string>();

  constructor(public _translate: TranslateService) {}

  ngOnInit() {
      if (this.requestFilters == true) {
        $('#togglerequestFilters').text("toggle_on");
        $('#togglerequestFilters').css("color", "green");
      } else {
        $('#togglerequestFilters').text("toggle_off");
        $('#togglerequestFilters').css("color", "red");
      }
      if (this.View == false) {
        $('#toggleView').text("toggle_on");
        $('#toggleView').css("color", "green");
      } else {
        $('#toggleView').text("toggle_off");
        $('#toggleView').css("color", "red");
      }
      if (this.Mode == true) {
        $('#toggleMode').text("toggle_on");
        $('#toggleMode').css("color", "green");
      } else {
        $('#toggleMode').text("toggle_off");
        $('#toggleMode').css("color", "red");
      }
      if(!this.isPlanner){
        $("#planner1").hide();
        $("#planner2").hide();
      }
  }

  toggle(type){
    if($('#toggle'+type).text() == "toggle_off"){
      $('#toggle'+type).text("toggle_on");
      $('#toggle'+type).css("color","green");
    }
    else {
      $('#toggle'+type).text("toggle_off");
      $('#toggle'+type).css("color", "red");
    }
    this.messageEvent.emit(type);
  }

  wait(ms){
    var start = new Date().getTime();
    var end = start;
    while(end < start + ms) {
      end = new Date().getTime();
    }
  }

}
