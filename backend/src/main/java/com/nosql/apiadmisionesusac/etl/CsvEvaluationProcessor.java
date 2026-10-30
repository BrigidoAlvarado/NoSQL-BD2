package com.nosql.apiadmisionesusac.etl;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/** Convierte las doce columnas publicadas a la representación tipada interna. */
@Component
public class CsvEvaluationProcessor {
    // Permite reprocesar archivos anteriores cuando cambian las reglas de calidad.
    static final int RULES_VERSION = 2;
    static final List<String> COLUMNS = List.of(
            "correlativo", "fecha_asignacion", "sexo", "anio_nacimiento", "materia",
            "oportunidad", "anio_ingreso", "aprobacion", "carrera", "tipo_institucion_educativa",
            "departamento_institucion_educativa", "municipio_institucion_educativa");

    private static final Map<String, String> HEADER_ALIASES = Map.of(
            "correlativo_aspirante", "correlativo",
            "numero_de_fecha_de_evaluaci", "oportunidad",
            "numero_de_fecha_de_evaluacion", "oportunidad",
            "anio_de_ingreso", "anio_ingreso",
            "carrera_objetivo", "carrera",
            "departamento_institucion_ed", "departamento_institucion_educativa",
            "municipio_institucion_", "municipio_institucion_educativa");

    public CsvProcessingResult process(byte[] bytes) throws CsvFormatException {
        String text = decodeUtf8(bytes);
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        List<CsvRow> rows = CsvRecordReader.read(text);
        if (rows.isEmpty()) {
            throw new CsvFormatException(1, "El archivo no contiene encabezado ni filas");
        }
        List<String> header = canonicalHeader(rows.getFirst());
        List<Evaluation> valid = new ArrayList<>();
        List<RejectedRow> rejected = new ArrayList<>();
        for (int position = 1; position < rows.size(); position++) {
            CsvRow row = rows.get(position);
            if (row.values().size() != header.size()) {
                rejected.add(new RejectedRow(row.firstLine(), "COLUMNAS_INCORRECTAS",
                        "Se esperaban " + header.size() + " columnas y se recibieron " + row.values().size(), Map.of()));
                continue;
            }
            Map<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < header.size(); i++) {
                values.put(header.get(i), row.values().get(i));
            }
            try {
                valid.add(toEvaluation(values, row.firstLine()));
            } catch (ValidationException exception) {
                rejected.add(new RejectedRow(row.firstLine(), exception.code, exception.getMessage(), Map.copyOf(values)));
            }
        }
        return new CsvProcessingResult(List.copyOf(valid), List.copyOf(rejected));
    }

    private String decodeUtf8(byte[] bytes) throws CsvFormatException {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException exception) {
            throw new CsvFormatException(1, "El archivo debe estar codificado en UTF-8");
        }
    }

    private List<String> canonicalHeader(CsvRow row) throws CsvFormatException {
        List<String> canonical = row.values().stream().map(this::canonicalColumn).toList();
        if (canonical.size() != COLUMNS.size() || !Set.copyOf(canonical).equals(Set.copyOf(COLUMNS))) {
            throw new CsvFormatException(row.firstLine(), "El encabezado debe contener exactamente las 12 columnas esperadas");
        }
        if (Set.copyOf(canonical).size() != canonical.size()) {
            throw new CsvFormatException(row.firstLine(), "El encabezado contiene columnas duplicadas");
        }
        return canonical;
    }

    private String canonicalColumn(String name) {
        String normalized = name.strip().toLowerCase(Locale.ROOT);
        return HEADER_ALIASES.getOrDefault(normalized, normalized);
    }

    private Evaluation toEvaluation(Map<String, String> values, int sourceLine) throws ValidationException {
        for (String column : COLUMNS) {
            if (values.get(column) == null || values.get(column).isBlank()) {
                throw new ValidationException("CAMPO_OBLIGATORIO", "Falta el valor de " + column);
            }
        }
        String aspiranteId = values.get("correlativo");
        LocalDate fecha = parseDate(values.get("fecha_asignacion"));
        String sexo = enumValue(values.get("sexo"), Set.of("M", "F", "SIN REGISTRO"), "SEXO_INVALIDO", "sexo");
        int anioIngreso = parseYear(values.get("anio_ingreso"), "ANIO_INGRESO_INVALIDO", "anio_ingreso");
        String nacimientoRaw = values.get("anio_nacimiento");
        boolean nacimientoConocido = !"SIN REGISTRO".equals(enumToken(nacimientoRaw));
        Integer nacimiento = nacimientoConocido ? parseYear(nacimientoRaw, "ANIO_NACIMIENTO_INVALIDO", "anio_nacimiento") : null;
        Integer edad = nacimiento != null && anioIngreso - nacimiento >= 0 && anioIngreso - nacimiento <= 100
                ? anioIngreso - nacimiento : null;
        String materia = values.get("materia");
        int oportunidad = parsePositiveInt(values.get("oportunidad"), "OPORTUNIDAD_INVALIDA", "oportunidad");
        String aprobacion = enumValue(values.get("aprobacion"), Set.of("APROBADO", "NO APROBADO"),
                "APROBACION_INVALIDA", "aprobacion");
        String carrera = values.get("carrera");
        String tipo = values.get("tipo_institucion_educativa");
        String departamento = values.get("departamento_institucion_educativa");
        String municipio = values.get("municipio_institucion_educativa");
        List<String> canonical = List.of(aspiranteId, fecha.toString(), sexo, nacimientoConocido ? nacimiento.toString() : "SIN REGISTRO",
                materia, String.valueOf(oportunidad), String.valueOf(anioIngreso), aprobacion, carrera, tipo, departamento, municipio);
        return new Evaluation(hash(canonical), aspiranteId, fecha, sexo, nacimiento, edad, nacimientoConocido,
                materia, oportunidad, anioIngreso, aprobacion, carrera, !"NINGUNO".equals(enumToken(carrera)),
                departamento, !"SIN DATOS".equals(enumToken(departamento)), municipio, !"SIN DATOS".equals(enumToken(municipio)),
                tipo, !"SIN DATOS".equals(enumToken(tipo)), sourceLine, canonical);
    }

    private LocalDate parseDate(String value) throws ValidationException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeException exception) {
            throw new ValidationException("FECHA_INVALIDA", "fecha_asignacion no tiene formato ISO YYYY-MM-DD");
        }
    }

    private int parseYear(String value, String code, String column) throws ValidationException {
        try {
            int year = Integer.parseInt(value);
            if (year < 1900 || year > 2100) {
                throw new NumberFormatException();
            }
            return year;
        } catch (NumberFormatException exception) {
            throw new ValidationException(code, column + " debe ser un año entre 1900 y 2100");
        }
    }

    private int parsePositiveInt(String value, String code, String column) throws ValidationException {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ValidationException(code, column + " debe ser un entero positivo");
        }
    }

    private String enumValue(String value, Set<String> valid, String code, String column) throws ValidationException {
        String normalized = enumToken(value);
        if (!valid.contains(normalized)) {
            throw new ValidationException(code, column + " tiene un valor no reconocido: " + value);
        }
        return normalized;
    }

    private String enumToken(String value) {
        return value.strip().toUpperCase(Locale.ROOT);
    }

    private String hash(List<String> values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(String.join("\u001F", values).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }

    private static final class ValidationException extends Exception {
        private final String code;

        private ValidationException(String code, String message) {
            super(message);
            this.code = code;
        }
    }
}
