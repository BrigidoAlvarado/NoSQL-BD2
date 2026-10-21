// Ejecutar con mongosh sobre la base admisiones_usac. Es idempotente.
// El ETL insertará una evaluación con _id = SHA-256 canónico de sus 12 campos.

const evaluaciones = db.getCollection('evaluaciones');

evaluaciones.createIndex(
  { aspiranteId: 1, anioIngreso: 1, fechaAsignacion: 1, _id: 1 },
  { name: 'historial_aspirante' },
);
evaluaciones.createIndex(
  { aprobacion: 1, materia: 1 },
  { name: 'aprobacion_por_materia' },
);
evaluaciones.createIndex(
  { anioIngreso: 1, carreraObjetivo: 1, aspiranteId: 1 },
  { name: 'carrera_anio_aspirante' },
);
evaluaciones.createIndex(
  { tipoInstitucionEducativa: 1, sexo: 1, aspiranteId: 1 },
  { name: 'institucion_sexo_aspirante' },
);
evaluaciones.createIndex(
  { departamentoInstitucionEducativa: 1, municipioInstitucionEducativa: 1, aspiranteId: 1 },
  { name: 'procedencia_aspirante' },
);
evaluaciones.createIndex(
  { fechaAsignacion: 1, tipoInstitucionEducativa: 1, materia: 1 },
  { name: 'fecha_institucion_materia' },
);
evaluaciones.createIndex(
  { anioIngreso: 1, oportunidad: 1, aprobacion: 1, carreraObjetivo: 1, aspiranteId: 1 },
  { name: 'primer_intento_por_carrera' },
);

db.getCollection('cargas').createIndex(
  { sha256Archivo: 1 },
  { name: 'archivo_unico', unique: true },
);
db.getCollection('cargas').createIndex(
  { estado: 1, iniciadaEn: -1 },
  { name: 'estado_reciente' },
);

db.getCollection('resumen_carrera').createIndex(
  { anioIngreso: 1, carreraObjetivo: 1 },
  { name: 'cohorte_carrera_unica', unique: true },
);
