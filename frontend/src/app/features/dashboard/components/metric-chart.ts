import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { ChartDatum, ChartKind } from '../dashboard.models';

@Component({
  imports: [CareerNamePipe],
  standalone: true,
  selector: 'app-metric-chart',
  inputs: ['rows', 'kind', 'title'],
  templateUrl: './metric-chart.html',
  styleUrl: './metric-chart.css',
})
export class MetricChart {
  rows: ChartDatum[] = [];
  kind: ChartKind = 'bar';
  title = '';
  readonly palette = ['#2d72b7', '#28745e', '#a7642d', '#7652a2', '#ad4662', '#3c8095', '#7a6c37', '#4c6381'];
  visibleRows(): ChartDatum[] {
    return this.rows.slice(0, 8);
  }
  total(): number {
    return this.visibleRows().reduce((sum, row) => sum + row.value, 0);
  }
  width(value: number): number {
    const max = Math.max(0, ...this.visibleRows().map((row) => row.value));
    return max ? (value / max) * 100 : 0;
  }
  color(index: number): string {
    return this.palette[index % this.palette.length];
  }
  format(value: number): string {
    return Number.isInteger(value) ? String(value) : value.toFixed(2);
  }
  donutBackground(): string {
    const rows = this.visibleRows();
    const total = this.total();
    if (!total) return '#e7edf4';
    let current = 0;
    return `conic-gradient(${rows
      .map((row, index) => {
        const start = current;
        current += (row.value / total) * 100;
        return `${this.color(index)} ${start}% ${current}%`;
      })
      .join(', ')})`;
  }
}
