package com.nosql.apiadmisionesusac.etl;

import java.util.List;

public record CsvProcessingResult(List<Evaluation> validRows, List<RejectedRow> rejectedRows) {
}
