import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AppService} from "../app.service";
import {TranslateService} from "../translate";

@Component({
  selector: 'app-planner-comment',
  templateUrl: './planner-comment.component.html',
  styleUrls: ['./planner-comment.component.css']
})
export class PlannerCommentComponent implements OnInit {
  @Input('amount') amount: number;
  @Output() messageEvent = new EventEmitter<string>();

  message: string;
  constructor(public _translate: TranslateService) {}

  ngOnInit() {
  }

  dataChanged(event){
    if(event!=""){
      this.message=event;
    }
    console.log(this.amount);
    this.messageEvent.emit(this.message);
  }
}
