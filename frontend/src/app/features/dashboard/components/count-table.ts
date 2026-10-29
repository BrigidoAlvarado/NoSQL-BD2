import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { CountByLabel } from '../dashboard.models';

@Component({
  imports: [CareerNamePipe],
  standalone: true,
  selector: 'app-count-table',
  inputs: ['rows'],
  templateUrl: './count-table.html',
})
export class CountTable {
  rows: CountByLabel[] = [];
  width(value: number): number {
    const max = Math.max(0, ...this.rows.map((row) => row.cantidad));
    return max > 0 ? (value / max) * 100 : 0;
  }
}
