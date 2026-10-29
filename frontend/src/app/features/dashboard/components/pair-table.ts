import { Component } from '@angular/core';
import { CountByPair } from '../dashboard.models';

@Component({
  standalone: true,
  selector: 'app-pair-table',
  inputs: ['rows', 'firstLabel', 'secondLabel'],
  templateUrl: './pair-table.html',
})
export class PairTable {
  rows: CountByPair[] = [];
  firstLabel = 'Departamento';
  secondLabel = 'Municipio';
}
