package com.nosql.apiadmisionesusac.etl;

import java.util.Map;

public record RejectedRow(int line, String code, String detail, Map<String, String> values) {
}
