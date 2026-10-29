import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';
import { CountTable } from './count-table';

@Component({
  selector: 'app-carreras-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [CountTable],
  templateUrl: './carreras-tables.html',
})
export class CareerTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
