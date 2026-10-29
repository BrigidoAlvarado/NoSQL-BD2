package com.nosql.apiadmisionesusac.statistics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.search.queries.M11ApplicantHistory;
import com.nosql.apiadmisionesusac.search.queries.M15ApplicantInformation;
import com.nosql.apiadmisionesusac.statistics.queries.*;
import com.nosql.apiadmisionesusac.statistics.summary.CareerSummaryStore;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;

@SpringBootTest
@EnabledIfSystemProperty(named = "statistics.integration", matches = "true")
class IndividualQueriesIntegrationTest {
    @Autowired MongoTemplate mongo;
    private MongoCollection<Document> evaluations;

    @BeforeEach
    void createFixture() {
        // Colección exclusiva: no agrega datos a las cargas del usuario.
        evaluations = mongo.getCollection("validacion_consultas_" + UUID.randomUUID());
        evaluations.insertMany(QueryFixture.evaluations());
    }

    @AfterEach
    void cleanFixture() {
        if (evaluations != null) evaluations.drop();
    }

    @Test
    void cantidadesDistinguenPersonasDeEvaluaciones() {
        assertEquals(2, M01ApplicantsByInstitution.execute(evaluations).size());
        assertEquals(1, M02ApprovedBySubject.execute(evaluations).getFirst().cantidad());
        assertEquals(2, M03ApprovedByCareerYear.execute(evaluations).size());
        assertEquals(2, M08ApprovedByMunicipality.execute(evaluations).size());
        assertEquals(
                3,
                M09PublicEvaluationsByMonth.execute(evaluations).stream()
                        .mapToLong(DashboardResponse.CountByPair::cantidad)
                        .sum());
        assertEquals(2, M10TopYoungCareers.execute(evaluations).size());
        assertEquals(2, M12ApplicantsBySexInstitution.execute(evaluations).size());
        assertEquals(2, M16FirstAttemptFailures.execute(evaluations).getFirst().cantidad());
        assertEquals(2, M17TopMunicipalities.execute(evaluations).getFirst().cantidad());
        var department = M19CareersByDepartment.execute(evaluations).getFirst();
        assertEquals("A", department.segundaEtiqueta());
        assertEquals(2, department.cantidad());
    }

    @Test
    void porcentajesYPromediosUsanLosDenominadoresEsperados() {
        var subject =
                M04ApprovalBySubject.execute(evaluations).stream()
                        .filter(row -> row.etiqueta().equals("MATEMATICA"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(33.33, subject.porcentajeAprobacion(), 0.001);
        var age = M05AgeByCareer.execute(evaluations).getFirst();
        assertEquals("A", age.etiqueta());
        assertEquals(19, age.promedio());
        assertEquals(2, age.conDato());
        assertEquals(18, M07ApprovedAgeByInstitution.execute(evaluations).getFirst().promedio());
        assertEquals(
                66.67,
                M13ApprovalByAge.execute(evaluations).getFirst().porcentajeAprobacion(),
                0.001);
        var opportunities =
                M14OpportunitiesBySubject.execute(evaluations).stream()
                        .filter(row -> row.materia().equals("MATEMATICA"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(1.5, opportunities.promedio());
        var publicRate =
                M18ApprovalByInstitution.execute(evaluations).stream()
                        .filter(row -> row.etiqueta().equals("PUBLICO"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(66.67, publicRate.porcentajeAprobacion(), 0.001);
        var year = M20ResultsByYear.execute(evaluations).getFirst();
        assertEquals(4, year.evaluaciones());
        assertEquals(50, year.porcentajeAprobacion());
    }

    @Test
    void resumenEHistorialConservanLasEvaluaciones() {
        var store = mock(CareerSummaryStore.class);
        var summary = M06CareerSummary.execute(evaluations, store);
        assertEquals(3, summary.getFirst().evaluaciones());
        assertEquals(2, summary.getFirst().aspirantesDistintos());
        verify(store, times(2)).save(any());
        assertEquals(3, M11ApplicantHistory.execute(evaluations, "a1").size());
        var detail = M15ApplicantInformation.execute(evaluations, "a1");
        assertEquals(3, detail.totalEvaluaciones());
        assertEquals(2, detail.evaluacionesAprobadas());
    }
}
