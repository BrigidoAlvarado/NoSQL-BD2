package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Conversiones y operaciones pequeñas compartidas; no define consultas de negocio. */
public final class QueryValues {
    private QueryValues() {}

    public static Document approvalExpression() {
        return new Document(
                "$cond", List.of(new Document("$eq", List.of("$aprobacion", "APROBADO")), 1, 0));
    }

    public static Document presentExpression(String field) {
        return new Document(
                "$cond", List.of(new Document("$ne", java.util.Arrays.asList(field, null)), 1, 0));
    }

    public static Document absentExpression(String field) {
        return new Document(
                "$cond", List.of(new Document("$eq", java.util.Arrays.asList(field, null)), 1, 0));
    }

    public static List<Document> aggregate(
            MongoCollection<Document> evaluations, List<Document> pipeline) {
        return evaluations.aggregate(pipeline).into(new ArrayList<>());
    }

    public static Comparator<DashboardResponse.CountByLabel> byCount() {
        return Comparator.comparing(DashboardResponse.CountByLabel::cantidad)
                .reversed()
                .thenComparing(DashboardResponse.CountByLabel::etiqueta);
    }

    public static long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    public static double decimal(Object value) {
        return value instanceof Number number
                ? Math.round(number.doubleValue() * 100.0) / 100.0
                : 0;
    }

    public static int size(Object value) {
        return value instanceof List<?> list ? list.size() : 0;
    }

    public static String label(Object value) {
        return value == null ? "SIN DATOS" : value.toString();
    }

    public static double percentage(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round((10000.0 * numerator) / denominator) / 100.0;
    }

    public static List<DashboardResponse.CountByLabel> countRows(
            MongoCollection<Document> collection, List<Document> pipeline) {
        return countRows(collection, pipeline, "cantidad");
    }

    public static List<DashboardResponse.CountByLabel> countRows(
            MongoCollection<Document> collection, List<Document> pipeline, String field) {
        List<DashboardResponse.CountByLabel> rows = new ArrayList<>();
        for (Document row : aggregate(collection, pipeline)) {
            long count = field.equals("aspirantes") ? size(row.get(field)) : number(row.get(field));
            rows.add(new DashboardResponse.CountByLabel(label(row.get("_id")), count));
        }
        rows.sort(byCount());
        return rows;
    }
}
