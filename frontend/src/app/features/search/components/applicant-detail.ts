import { Component } from '@angular/core';
import { ApplicantDetail } from '../search.models';

/** Presenta M11/M15; la página se encarga de solicitar los datos. */
@Component({
  selector: 'app-applicant-detail',
  inputs: ['detail'],
  templateUrl: './applicant-detail.html',
})
export class ApplicantDetailView {
  detail!: ApplicantDetail;
  display(value: number | null): string {
    return value === null ? 'SIN REGISTRO' : String(value);
  }
}
