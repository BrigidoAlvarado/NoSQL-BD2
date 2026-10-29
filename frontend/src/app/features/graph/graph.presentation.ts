export const QUERY_TITLES: Record<string, string> = {
  G01: 'Demanda por carrera',
  G02: 'Resultados por materia',
  G03: 'Aprobados por departamento y carrera',
  G04: 'Aspirantes vinculados con distintas carreras',
  G05: 'Resultados por cohorte, carrera y materia',
  G06: 'Resultados por tipo de institución',
  G07: 'Reintentos por materia y cohorte',
  G08: 'Demanda por municipio y carrera',
  G09: 'Resultados por sexo, carrera y materia',
  G10: 'Aspirantes con resultados mixtos',
};

export function columnLabel(column: string): string {
  return (
    (
      {
        carrera: 'Carrera',
        evaluaciones: 'Evaluaciones',
        materia: 'Materia',
        resultado: 'Resultado',
        departamento: 'Departamento',
        municipio: 'Municipio',
        tipoInstitucion: 'Tipo de institución',
        sexo: 'Sexo',
        aspirantes: 'Aspirantes',
        aspirantesReintentaron: 'Aspirantes con reintento',
        promedioOportunidades: 'Promedio de oportunidades',
        carreraOrigen: 'Carrera A',
        carreraDestino: 'Carrera B',
        anioIngreso: 'Año de ingreso',
      } as Record<string, string>
    )[column] ?? column
  );
}
