import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { DashboardData } from '../dashboard.models';

@Component({
  selector: 'app-perfil-tables',
  inputs: ['dashboard', 'queryId'],
  imports: [CareerNamePipe, ],
  templateUrl: './perfil-tables.html',
})
export class ProfileTables {
  dashboard!: DashboardData;
  queryId = '';
  percent(value: number): string {
    return `${value.toFixed(2)} %`;
  }
}
