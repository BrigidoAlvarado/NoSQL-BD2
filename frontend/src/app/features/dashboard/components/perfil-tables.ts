import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';

@Component({
  selector: 'app-perfil-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [],
  templateUrl: './perfil-tables.html',
})
export class ProfileTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
