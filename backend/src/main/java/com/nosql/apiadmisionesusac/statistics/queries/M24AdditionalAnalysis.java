package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

/** M24: Promedia días entre primera y última asignación solo con fechas distintas; informa incluidos y excluidos. */
public final class M24AdditionalAnalysis {
    private M24AdditionalAnalysis() {}
    public static List<Document> execute(MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<Document> execute(MongoCollection<Document> evaluations, Document filter) {
        var pipeline = List.of(
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$anioIngreso",
      "aspirante": "$aspiranteId"
    },
    "primera": {
      "$min": "$fechaAsignacion"
    },
    "ultima": {
      "$max": "$fechaAsignacion"
    }
  }
}
"""),
                Document.parse("""
{
  "$set": {
    "dias": {
      "$cond": [
        {
          "$gt": [
            "$ultima",
            "$primera"
          ]
        },
        {
          "$divide": [
            {
              "$subtract": [
                "$ultima",
                "$primera"
              ]
            },
            86400000
          ]
        },
        null
      ]
    }
  }
}
"""),
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$_id.anio"
    },
    "incluidos": {
      "$sum": {
        "$cond": [
          {
            "$ne": [
              "$dias",
              null
            ]
          },
          1,
          0
        ]
      }
    },
    "excluidos": {
      "$sum": {
        "$cond": [
          {
            "$eq": [
              "$dias",
              null
            ]
          },
          1,
          0
        ]
      }
    },
    "promedioDias": {
      "$avg": "$dias"
    },
    "minimoDias": {
      "$min": "$dias"
    },
    "maximoDias": {
      "$max": "$dias"
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
