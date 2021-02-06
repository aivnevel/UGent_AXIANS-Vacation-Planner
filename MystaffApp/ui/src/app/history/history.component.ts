import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {animate, state, style, transition, trigger} from "@angular/animations";
import {TranslateService} from "../translate";
import {MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0', visibility: 'hidden' })),
      state('expanded', style({ height: '*', visibility: 'visible' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class HistoryComponent implements OnInit, OnChanges{

  //data as input because history component is used by different components (with different data types to display)

  @Input()
  rows: any[];

  //same goes for table_titles

  @Input()
  table_titles;

  dataSource: MatTableDataSource<any>;

  //only add clickable detail row when there is more history to see
  isExpansionDetailRow = (i: number, row: Object) => row.hasOwnProperty('detailRow');
  expandedElement: any;

  constructor(public _translate : TranslateService){
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<any>(this.rows);
  }

  //update table on changes
  ngOnChanges(changes: SimpleChanges): void {
    this.dataSource = new MatTableDataSource<any>(changes.rows.currentValue);
  }

  typeToString(type: string) : string {
    if (type === 'Sickness')
      return this._translate.instant('OPTION_SICKNESS');
    else if (type === 'Yearly')
      return this._translate.instant('OPTION_ANNUAL_LEAVE');
    else if (type === 'Educative')
      return this._translate.instant('OPTION_EDUCATIONAL_LEAVE');
    else if (type === 'European')
      return this._translate.instant('OPTION_EUROPEAN_LEAVE');
    return this._translate.instant('OPTION_OTHER');
  }
}


