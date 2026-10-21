package com.nosql.apiadmisionesusac.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

class CsvEvaluationProcessorTest {
    private final CsvEvaluationProcessor processor = new CsvEvaluationProcessor();

    @Test
    void transformaValoresEspecialesSinEliminarLaFila() throws Exception {
        CsvProcessingResult result = processor.process(csv("""
                001,2024-01-02,m,SIN REGISTRO,MATEMATICA,2,2024,aprobado,NINGUNO,SIN DATOS,SIN DATOS,SIN DATOS
                """).getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.validRows().size());
        Evaluation row = result.validRows().getFirst();
        assertEquals("M", row.sexo());
        assertEquals("APROBADO", row.aprobacion());
        assertNull(row.anioNacimiento());
        assertNull(row.edadIngreso());
        assertFalse(row.anioNacimientoConocido());
        assertFalse(row.carreraObjetivoConocida());
        assertFalse(row.tipoInstitucionEducativaConocido());
    }

    @Test
    void conservaCategoriasConEspaciosYRechazaLosTiposInvalidos() throws Exception {
        CsvProcessingResult result = processor.process(csv("""
                001,2024-01-02,F,2004, FISICA ,0,2024,TAL VEZ,INGENIERIA,PRIVADO,GUATEMALA,MIXCO
                """).getBytes(StandardCharsets.UTF_8));

        assertTrue(result.validRows().isEmpty());
        assertEquals(1, result.rejectedRows().size());
        assertEquals("OPORTUNIDAD_INVALIDA", result.rejectedRows().getFirst().code());
    }

    @Test
    void aceptaEncabezadosTruncadosDeLosArchivosRecientes() throws Exception {
        String input = """
                correlativo,fecha_asignacion,sexo,anio_nacimiento,materia,oportunidad,anio_ingreso,aprobacion,carrera,tipo_institucion_educativa,departamento_institucion_ed,municipio_institucion_
                001,2024-01-02,F,2004,MATEMATICA,1,2024,APROBADO,INGENIERIA,PRIVADO,GUATEMALA,MIXCO
                """;
        CsvProcessingResult result = processor.process(input.getBytes(StandardCharsets.UTF_8));
        assertEquals(1, result.validRows().size());
        assertEquals("MIXCO", result.validRows().getFirst().municipioInstitucionEducativa());
    }

    @Test
    void mismoRegistroProduceMismoHashYFechaDistintaOtroHash() throws Exception {
        Evaluation first = processor.process(csv("001,2024-01-02,F,2004,MATEMATICA,1,2024,APROBADO,INGENIERIA,PRIVADO,GUATEMALA,MIXCO\n")
                .getBytes(StandardCharsets.UTF_8)).validRows().getFirst();
        Evaluation repeat = processor.process(csv("001,2024-01-02,F,2004,MATEMATICA,1,2024,APROBADO,INGENIERIA,PRIVADO,GUATEMALA,MIXCO\n")
                .getBytes(StandardCharsets.UTF_8)).validRows().getFirst();
        Evaluation changed = processor.process(csv("001,2024-01-03,F,2004,MATEMATICA,1,2024,APROBADO,INGENIERIA,PRIVADO,GUATEMALA,MIXCO\n")
                .getBytes(StandardCharsets.UTF_8)).validRows().getFirst();
        assertEquals(first.id(), repeat.id());
        assertFalse(first.id().equals(changed.id()));
    }

    @Test
    void informaErrorDeCsvNoEstructurable() {
        assertThrows(CsvFormatException.class, () -> processor.process((csv("001,2024-01-02,F,2004,\"MATEMATICA,1,2024,APROBADO,INGENIERIA,PRIVADO,GUATEMALA,MIXCO") )
                .getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void procesaLosCsvLocalesAsignadosCuandoEstanDisponibles() throws Exception {
        Path data = Path.of("..", ".data");
        Assumptions.assumeTrue(Files.isDirectory(data), "Los CSV locales son evidencia opcional y están excluidos de Git");
        try (Stream<Path> files = Files.list(data).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            for (Path file : files.toList()) {
                CsvProcessingResult result = processor.process(Files.readAllBytes(file));
                assertFalse(result.validRows().isEmpty(), () -> "No se leyó ninguna fila de " + file);
                assertTrue(result.rejectedRows().isEmpty(), () -> "Se rechazaron filas válidas de " + file + ": " + result.rejectedRows());
            }
        }
    }

    private String csv(String row) {
        return "correlativo,fecha_asignacion,sexo,anio_nacimiento,materia,oportunidad,anio_ingreso,aprobacion,carrera,tipo_institucion_educativa,departamento_institucion_educativa,municipio_institucion_educativa\n" + row;
    }
}
