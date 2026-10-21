package com.nosql.apiadmisionesusac.etl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

@Component
class EtlNeo4jStore {
    private static final String UPSERT = """
            UNWIND $rows AS row
            MERGE (a:Aspirante {id: row.aspiranteId})
            MERGE (e:Evaluacion {id: row.id})
            ON CREATE SET e.fechaAsignacion = date(row.fechaAsignacion), e.sexo = row.sexo,
              e.anioNacimiento = row.anioNacimiento, e.anioNacimientoConocido = row.anioNacimientoConocido,
              e.edadIngreso = row.edadIngreso, e.oportunidad = row.oportunidad, e.anioIngreso = row.anioIngreso,
              e.aprobacion = row.aprobacion, e.materiaClave = row.materia
            MERGE (m:Materia {clave: row.materia})
            MERGE (c:Carrera {clave: row.carrera}) ON CREATE SET c.conocida = row.carreraConocida
            MERGE (r:Resultado {codigo: row.aprobacion})
            MERGE (t:TipoInstitucion {codigo: row.tipo}) ON CREATE SET t.conocida = row.tipoConocido
            MERGE (d:Departamento {clave: row.departamento}) ON CREATE SET d.conocido = row.departamentoConocido
            MERGE (u:Municipio {clave: row.municipioKey}) ON CREATE SET u.nombre = row.municipio,
              u.departamento = row.departamento, u.conocido = row.municipioConocido
            MERGE (p:ProcedenciaEducativa {clave: row.procedenciaKey}) ON CREATE SET p.tipo = row.tipo,
              p.departamento = row.departamento, p.municipio = row.municipio,
              p.conocida = row.tipoConocido AND row.departamentoConocido AND row.municipioConocido
            MERGE (a)-[:REALIZO]->(e)
            MERGE (e)-[:EVALUO_MATERIA]->(m)
            MERGE (e)-[:PARA_CARRERA]->(c)
            MERGE (e)-[:OBTUVO_RESULTADO]->(r)
            MERGE (e)-[:TIENE_PROCEDENCIA]->(p)
            MERGE (p)-[:ES_DE_TIPO]->(t)
            MERGE (p)-[:UBICADA_EN]->(u)
            MERGE (u)-[:PERTENECE_A]->(d)
            """;
    private final Driver driver;

    EtlNeo4jStore(Driver driver) {
        this.driver = driver;
    }

    void saveEvaluations(List<Evaluation> evaluations) {
        try (Session session = driver.session()) {
            for (int from = 0; from < evaluations.size(); from += 500) {
                List<Map<String, Object>> rows = new ArrayList<>();
                for (Evaluation value : evaluations.subList(from, Math.min(from + 500, evaluations.size()))) {
                    rows.add(row(value));
                }
                session.executeWriteWithoutResult(transaction -> transaction.run(UPSERT, Map.of("rows", rows)).consume());
            }
        }
    }

    private Map<String, Object> row(Evaluation value) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", value.id());
        row.put("aspiranteId", value.aspiranteId());
        row.put("fechaAsignacion", value.fechaAsignacion().toString());
        row.put("sexo", value.sexo());
        if (value.anioNacimiento() != null) {
            row.put("anioNacimiento", value.anioNacimiento());
        }
        row.put("anioNacimientoConocido", value.anioNacimientoConocido());
        if (value.edadIngreso() != null) {
            row.put("edadIngreso", value.edadIngreso());
        }
        row.put("materia", value.materia());
        row.put("oportunidad", value.oportunidad());
        row.put("anioIngreso", value.anioIngreso());
        row.put("aprobacion", value.aprobacion());
        row.put("carrera", value.carreraObjetivo());
        row.put("carreraConocida", value.carreraObjetivoConocida());
        row.put("tipo", value.tipoInstitucionEducativa());
        row.put("tipoConocido", value.tipoInstitucionEducativaConocido());
        row.put("departamento", value.departamentoInstitucionEducativa());
        row.put("departamentoConocido", value.departamentoInstitucionEducativaConocido());
        row.put("municipio", value.municipioInstitucionEducativa());
        row.put("municipioConocido", value.municipioInstitucionEducativaConocido());
        row.put("municipioKey", hash(value.departamentoInstitucionEducativa(), value.municipioInstitucionEducativa()));
        row.put("procedenciaKey", hash(value.tipoInstitucionEducativa(), value.departamentoInstitucionEducativa(), value.municipioInstitucionEducativa()));
        return row;
    }

    private String hash(String... values) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\u001F", values).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}
