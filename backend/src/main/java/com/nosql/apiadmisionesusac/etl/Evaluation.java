package com.nosql.apiadmisionesusac.etl;

import java.time.LocalDate;
import java.util.List;

public record Evaluation(
        String id,
        String aspiranteId,
        LocalDate fechaAsignacion,
        String sexo,
        Integer anioNacimiento,
        Integer edadIngreso,
        boolean anioNacimientoConocido,
        String materia,
        int oportunidad,
        int anioIngreso,
        String aprobacion,
        String carreraObjetivo,
        boolean carreraObjetivoConocida,
        String departamentoInstitucionEducativa,
        boolean departamentoInstitucionEducativaConocido,
        String municipioInstitucionEducativa,
        boolean municipioInstitucionEducativaConocido,
        String tipoInstitucionEducativa,
        boolean tipoInstitucionEducativaConocido,
        int filaOrigen,
        List<String> canonicalValues) {
}
