package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

/** M21: Cuenta evaluaciones por cohorte, materia y oportunidad; porcentaje sobre el total del grupo. */
public final class M21AdditionalAnalysis {
    private M21AdditionalAnalysis() {}
    public static List<Document> execute(MongoCollection<Document> evaluations) {
        var pipeline = List.of(
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$anioIngreso",
      "materia": "$materia",
      "oportunidad": "$oportunidad"
    },
    "total": {
      "$sum": 1
    },
    "aprobadas": {
      "$sum": {
        "$cond": [
          {
            "$eq": [
              "$aprobacion",
              "APROBADO"
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
    "noAprobadas": {
      "$subtract": [
        "$total",
        "$aprobadas"
      ]
    },
    "porcentaje": {
      "$multiply": [
        100,
        {
          "$divide": [
            "$aprobadas",
            "$total"
          ]
        }
      ]
    }
  }
}
""")
        );
        var rows = evaluations.aggregate(pipeline).into(new ArrayList<>());
        rows.sort(java.util.Comparator.comparing(row -> row.get("_id").toString()));
        return rows;
    }
}
