import { MunicipalityNamePipe } from '../../../shared/municipality-name.pipe';
import { CareerNamePipe } from '../../../shared/career-name.pipe';
import { Component } from '@angular/core';
import { CountByPair } from '../dashboard.models';

@Component({
  imports: [CareerNamePipe, MunicipalityNamePipe],
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
