package com.nosql.apiadmisionesusac.etl;

import java.util.ArrayList;
import java.util.List;

/**
 * Lector pequeño de CSV RFC-4180. Conserva las comas, espacios y saltos de
 * línea dentro de campos entrecomillados; estos valores no se deben alterar al
 * construir las categorías de procedencia.
 */
final class CsvRecordReader {
    private CsvRecordReader() {
    }

    static List<CsvRow> read(String text) throws CsvFormatException {
        List<CsvRow> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        int line = 1;
        int firstLine = 1;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (quoted) {
                if (current == '"') {
                    if (index + 1 < text.length() && text.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(current);
                    if (current == '\n') {
                        line++;
                    } else if (current == '\r') {
                        if (index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                            index++;
                        }
                        line++;
                    }
                }
                continue;
            }

            if (current == '"') {
                if (!field.isEmpty()) {
                    throw new CsvFormatException(line, "Comilla sin abrir al inicio de un campo");
                }
                quoted = true;
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (current == '\n' || current == '\r') {
                fields.add(field.toString());
                field.setLength(0);
                rows.add(new CsvRow(firstLine, List.copyOf(fields)));
                fields.clear();
                if (current == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                line++;
                firstLine = line;
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new CsvFormatException(firstLine, "Campo entrecomillado sin cierre");
        }
        if (!field.isEmpty() || !fields.isEmpty()) {
            fields.add(field.toString());
            rows.add(new CsvRow(firstLine, List.copyOf(fields)));
        }
        return rows;
    }
}
