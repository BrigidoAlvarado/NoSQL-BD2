package com.nosql.apiadmisionesusac.etl;

/** Error de sintaxis CSV que impide determinar de forma segura las filas. */
public final class CsvFormatException extends Exception {
    private final int line;

    public CsvFormatException(int line, String message) {
        super(message);
        this.line = line;
    }

    public int line() {
        return line;
    }
}
