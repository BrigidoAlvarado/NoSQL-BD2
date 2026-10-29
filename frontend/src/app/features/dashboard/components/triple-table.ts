import { Component } from '@angular/core';
import { CountByTriple } from '../dashboard.models';

@Component({
  standalone: true,
  selector: 'app-triple-table',
  inputs: ['rows'],
  templateUrl: './triple-table.html',
})
export class TripleTable {
  rows: CountByTriple[] = [];
}
