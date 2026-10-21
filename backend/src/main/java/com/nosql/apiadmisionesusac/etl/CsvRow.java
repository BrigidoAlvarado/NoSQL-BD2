package com.nosql.apiadmisionesusac.etl;

import java.util.List;

record CsvRow(int firstLine, List<String> values) {
}
