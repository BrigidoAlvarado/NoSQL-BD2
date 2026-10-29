import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';
import { PairTable } from './pair-table';
import { TripleTable } from './triple-table';

@Component({
  selector: 'app-procedencia-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [PairTable, TripleTable],
  templateUrl: './procedencia-tables.html',
})
export class OriginTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
