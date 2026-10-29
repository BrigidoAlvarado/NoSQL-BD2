import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';
import { CountTable } from './count-table';

@Component({
  selector: 'app-carreras-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [CareerNamePipe, CountTable],
  templateUrl: './carreras-tables.html',
})
export class CareerTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
