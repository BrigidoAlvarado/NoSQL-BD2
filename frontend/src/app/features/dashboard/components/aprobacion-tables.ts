import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';
import { CountTable } from './count-table';
import { RateTable } from './rate-table';

@Component({
  selector: 'app-aprobacion-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [CountTable, RateTable],
  templateUrl: './aprobacion-tables.html',
})
export class ApprovalTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
