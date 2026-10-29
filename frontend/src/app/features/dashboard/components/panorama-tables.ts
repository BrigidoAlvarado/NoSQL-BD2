import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';
import { CountTable } from './count-table';

@Component({
  selector: 'app-panorama-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [CountTable],
  templateUrl: './panorama-tables.html',
})
export class PanoramaTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
