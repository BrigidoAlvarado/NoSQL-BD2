package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

/** M22: Cuenta una recuperación por aspirante, materia y cohorte cuando alguna aprobación sucede en oportunidad mayor que un fallo; denominador: personas con fallo. */
public final class M22AdditionalAnalysis {
    private M22AdditionalAnalysis() {}
    public static List<Document> execute(MongoCollection<Document> evaluations) {
        var pipeline = List.of(
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$anioIngreso",
      "materia": "$materia",
      "aspirante": "$aspiranteId"
    },
    "primerFallo": {
      "$min": {
        "$cond": [
          {
            "$eq": [
              "$aprobacion",
              "NO APROBADO"
            ]
          },
          "$oportunidad",
          999999
        ]
      }
    },
    "ultimaAprobacion": {
      "$max": {
        "$cond": [
          {
            "$eq": [
              "$aprobacion",
              "APROBADO"
            ]
          },
          "$oportunidad",
          0
        ]
      }
    }
  }
}
"""),
                Document.parse("""
{
  "$match": {
    "primerFallo": {
      "$lt": 999999
    }
  }
}
"""),
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$_id.anio",
      "materia": "$_id.materia"
    },
    "conFallo": {
      "$sum": 1
    },
    "recuperados": {
      "$sum": {
        "$cond": [
          {
            "$gt": [
              "$ultimaAprobacion",
              "$primerFallo"
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
    "porcentaje": {
      "$multiply": [
        100,
        {
          "$divide": [
            "$recuperados",
            "$conFallo"
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
