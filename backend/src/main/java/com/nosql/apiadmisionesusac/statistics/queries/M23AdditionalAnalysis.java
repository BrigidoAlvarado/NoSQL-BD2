package com.nosql.apiadmisionesusac.statistics.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

/** M23: Cuenta materias distintas por persona y cohorte; porcentajes sobre aspirantes de esa cohorte. */
public final class M23AdditionalAnalysis {
    private M23AdditionalAnalysis() {}
    public static List<Document> execute(MongoCollection<Document> evaluations) {
        var pipeline = List.of(
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$anioIngreso",
      "aspirante": "$aspiranteId"
    },
    "materias": {
      "$addToSet": "$materia"
    }
  }
}
"""),
                Document.parse("""
{
  "$group": {
    "_id": {
      "anio": "$_id.anio",
      "materias": {
        "$size": "$materias"
      }
    },
    "aspirantes": {
      "$sum": 1
    }
  }
}
"""),
                Document.parse("""
{
  "$setWindowFields": {
    "partitionBy": "$_id.anio",
    "output": {
      "totalCohorte": {
        "$sum": "$aspirantes",
        "window": {
          "documents": [
            "unbounded",
            "unbounded"
          ]
        }
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
            "$aspirantes",
            "$totalCohorte"
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
