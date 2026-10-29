import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { CountByTriple } from '../dashboard.models';

@Component({
  imports: [CareerNamePipe],
  standalone: true,
  selector: 'app-triple-table',
  inputs: ['rows'],
  templateUrl: './triple-table.html',
})
export class TripleTable {
  rows: CountByTriple[] = [];
}
