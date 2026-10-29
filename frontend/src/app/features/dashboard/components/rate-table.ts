import { Component } from '@angular/core';
import { RateByLabel } from '../dashboard.models';

@Component({
  standalone: true,
  selector: 'app-rate-table',
  inputs: ['rows'],
  templateUrl: './rate-table.html',
})
export class RateTable {
  rows: RateByLabel[] = [];
}
