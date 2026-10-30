import { Component, input } from '@angular/core';
import { MetricChart } from '../../dashboard/components/metric-chart';
import { QueryData } from '../graph.models';
import { GRAPH_CHARTS, graphChartRows, graphMetricLabel } from '../graph.charts';

/** Presenta barras desde la misma respuesta que alimenta la tabla Cypher. */
@Component({
  selector: 'app-graph-query-chart',
  imports: [MetricChart],
  templateUrl: './graph-query-chart.html',
  styleUrl: './graph-query-chart.css',
})
export class GraphQueryChart {
  readonly result = input.required<QueryData>();
  readonly definitions = GRAPH_CHARTS;
  readonly rows = graphChartRows;
  readonly metricLabel = graphMetricLabel;
}
