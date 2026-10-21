// Ejecutar en la base configurada de Neo4j. Cada sentencia es repetible.

CREATE CONSTRAINT aspirante_id_unico IF NOT EXISTS
FOR (n:Aspirante) REQUIRE n.id IS UNIQUE;

CREATE CONSTRAINT evaluacion_id_unico IF NOT EXISTS
FOR (n:Evaluacion) REQUIRE n.id IS UNIQUE;

CREATE CONSTRAINT materia_clave_unica IF NOT EXISTS
FOR (n:Materia) REQUIRE n.clave IS UNIQUE;

CREATE CONSTRAINT carrera_clave_unica IF NOT EXISTS
FOR (n:Carrera) REQUIRE n.clave IS UNIQUE;

CREATE CONSTRAINT resultado_codigo_unico IF NOT EXISTS
FOR (n:Resultado) REQUIRE n.codigo IS UNIQUE;

CREATE CONSTRAINT tipo_institucion_codigo_unico IF NOT EXISTS
FOR (n:TipoInstitucion) REQUIRE n.codigo IS UNIQUE;

CREATE CONSTRAINT departamento_clave_unica IF NOT EXISTS
FOR (n:Departamento) REQUIRE n.clave IS UNIQUE;

CREATE CONSTRAINT municipio_clave_unica IF NOT EXISTS
FOR (n:Municipio) REQUIRE n.clave IS UNIQUE;

CREATE CONSTRAINT procedencia_clave_unica IF NOT EXISTS
FOR (n:ProcedenciaEducativa) REQUIRE n.clave IS UNIQUE;

CREATE INDEX evaluacion_cohorte_materia IF NOT EXISTS
FOR (n:Evaluacion) ON (n.anioIngreso, n.materiaClave);

CREATE INDEX evaluacion_resultado IF NOT EXISTS
FOR (n:Evaluacion) ON (n.aprobacion);

CREATE INDEX evaluacion_fecha_asignacion IF NOT EXISTS
FOR (n:Evaluacion) ON (n.fechaAsignacion);
