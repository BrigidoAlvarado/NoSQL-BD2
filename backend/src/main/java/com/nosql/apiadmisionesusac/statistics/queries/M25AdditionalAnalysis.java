package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

/** M25: Mide condiciones de calidad superpuestas sobre evaluaciones de cada cohorte; no sumar porcentajes. */
public final class M25AdditionalAnalysis {
    private M25AdditionalAnalysis() {}
    public static List<Document> execute(MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<Document> execute(MongoCollection<Document> evaluations, Document filter) {
        var pipeline = List.of(
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$anioIngreso"
    },
    "total": {
      "$sum": 1
    },
    "sinEdad": {
      "$sum": {
        "$cond": [
          {
            "$eq": [
              {
                "$ifNull": [
                  "$edadIngreso",
                  null
                ]
              },
              null
            ]
          },
          1,
          0
        ]
      }
    },
    "carreraDesconocida": {
      "$sum": {
        "$cond": [
          {
            "$ne": [
              "$carreraObjetivoConocida",
              true
            ]
          },
          1,
          0
        ]
      }
    },
    "procedenciaDesconocida": {
      "$sum": {
        "$cond": [
          {
            "$or": [
              {
                "$ne": [
                  "$procedencia.departamentoConocido",
                  true
                ]
              },
              {
                "$ne": [
                  "$procedencia.municipioConocido",
                  true
                ]
              },
              {
                "$ne": [
                  "$procedencia.tipoConocido",
                  true
                ]
              }
            ]
          },
          1,
          0
        ]
      }
    }
  }
}
"""),
                Document.parse("""
{
  "$set": {
    "porcentajeSinEdad": {
      "$multiply": [
        100,
        {
          "$divide": [
            "$sinEdad",
            "$total"
          ]
        }
      ]
    },
    "porcentajeCarreraDesconocida": {
      "$multiply": [
        100,
        {
          "$divide": [
            "$carreraDesconocida",
            "$total"
          ]
        }
      ]
    },
    "porcentajeProcedenciaDesconocida": {
      "$multiply": [
        100,
        {
          "$divide": [
            "$procedenciaDesconocida",
            "$total"
          ]
        }
      ]
    }
  }
}
""")
        );
        var rows = QueryValues.aggregate(evaluations, filter, pipeline);
        rows.sort(java.util.Comparator.comparing(row -> row.get("_id").toString()));
        return rows;
    }
}
