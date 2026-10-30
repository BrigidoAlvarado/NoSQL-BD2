package com.nosql.apiadmisionesusac.etl;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoCollection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Se ejecuta explícitamente contra compose: -Detl.integration=true. */
@SpringBootTest
@EnabledIfSystemProperty(named = "etl.integration", matches = "true")
class CsvImportIntegrationTest {
    private static final String ASPIRANTE = "C04_VALIDACION_20261021";
    private static final String FILE = "c04-validacion.csv";

    @Autowired private CsvImportService importer;
    @Autowired private MongoTemplate mongoTemplate;
    @Autowired private Driver driver;

    @AfterEach
    void cleanup() {
        mongoTemplate.getCollection("evaluaciones").deleteMany(eq("aspiranteId", ASPIRANTE));
        mongoTemplate.getCollection("cargas").deleteMany(eq("nombreArchivo", FILE));
        mongoTemplate.getCollection("rechazos_carga").deleteMany(eq("sha256Archivo", EtlMongoStore.sha256(csv())));
        try (var session = driver.session()) {
            session.executeWriteWithoutResult(tx -> tx.run("MATCH (e:Evaluacion {id: $id}) DETACH DELETE e", Map.of("id", id())).consume());
            session.executeWriteWithoutResult(tx -> tx.run("MATCH (a:Aspirante {id: $id}) DETACH DELETE a", Map.of("id", ASPIRANTE)).consume());
            session.executeWriteWithoutResult(tx -> tx.run("""
                    MATCH (p:ProcedenciaEducativa {tipo: 'C04_VALIDACION_TIPO'}) DETACH DELETE p
                    """).consume());
            session.executeWriteWithoutResult(tx -> tx.run("""
                    MATCH (u:Municipio {nombre: 'C04_VALIDACION_MUNI', departamento: 'C04_VALIDACION_DEPTO'}) DETACH DELETE u
                    """).consume());
            session.executeWriteWithoutResult(tx -> tx.run("""
                    MATCH (n) WHERE (n:Materia OR n:Carrera OR n:TipoInstitucion OR n:Departamento)
                      AND (n.clave = 'C04_VALIDACION_MATERIA' OR n.clave = 'C04_VALIDACION_CARRERA'
                           OR n.clave = 'C04_VALIDACION_DEPTO' OR n.codigo = 'C04_VALIDACION_TIPO')
                    DETACH DELETE n
                    """).consume());
        }
    }

    @Test
    void sincronizaAmbosMotoresYLaRepeticionNoDuplica() {
        ImportResult first = importer.importFile(FILE, csv());
        ImportResult repeated = importer.importFile(FILE, csv());

        assertEquals("COMPLETA", first.estado());
        assertEquals(1, first.filasValidas());
        assertEquals("COMPLETA", repeated.estado());
        assertTrue(repeated.reutilizada());
        MongoCollection<Document> evaluations = mongoTemplate.getCollection("evaluaciones");
        assertEquals(1, evaluations.countDocuments(eq("aspiranteId", ASPIRANTE)));
        assertEquals("SIN REGISTRO", evaluations.find(eq("aspiranteId", ASPIRANTE)).first().getString("sexo"));
        // Una carga con reglas anteriores debe poder recuperar los rechazos corregidos.
        mongoTemplate.getCollection("cargas").updateOne(eq("sha256Archivo", first.sha256Archivo()),
                new Document("$unset", new Document("versionReglas", "")));
        ImportResult updated = importer.importFile(FILE, csv());
        assertTrue(!updated.reutilizada());
        assertEquals("COMPLETA", updated.estado());
        assertEquals(1, evaluations.countDocuments(eq("aspiranteId", ASPIRANTE)));
        try (var session = driver.session()) {
            long graphRows = session.executeRead(tx -> tx.run("MATCH (e:Evaluacion {id: $id}) RETURN count(e) AS total", Map.of("id", id()))
                    .single().get("total").asLong());
            assertEquals(1, graphRows);
            String sex = session.executeRead(tx -> tx.run("MATCH (e:Evaluacion {id: $id}) RETURN e.sexo AS sexo", Map.of("id", id()))
                    .single().get("sexo").asString());
            assertEquals("SIN REGISTRO", sex);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private static byte[] csv() {
        return ("fecha_asignacion,correlativo_aspirante,sexo,anio_nacimiento,materia,numero_de_fecha_de_evaluacion,anio_de_ingreso,aprobacion,carrera_objetivo,departamento_institucion_educativa,municipio_institucion_educativa,tipo_institucion_educativa\n"
                + "2024-01-02," + ASPIRANTE + ",SIN REGISTRO,SIN REGISTRO,C04_VALIDACION_MATERIA,1,2024,APROBADO,C04_VALIDACION_CARRERA,C04_VALIDACION_DEPTO,C04_VALIDACION_MUNI,C04_VALIDACION_TIPO\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String id() {
        try {
            return new CsvEvaluationProcessor().process(csv()).validRows().getFirst().id();
        } catch (CsvFormatException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
