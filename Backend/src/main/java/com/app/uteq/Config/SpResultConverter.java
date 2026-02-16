package com.app.uteq.Config;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilidades compartidas para convertir resultados de Stored Procedures.
 * Los SPs devuelven Object[] y cada valor necesita ser convertido al tipo Java correcto.
 *
 * Uso: import static com.app.uteq.Config.SpResultConverter.*;
 */
public final class SpResultConverter {

    private SpResultConverter() {
        // Utility class
    }

    public static Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    public static String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    public static Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }

    public static LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        if (v instanceof java.util.Date d) return new Timestamp(d.getTime()).toLocalDateTime();
        throw new IllegalArgumentException("Tipo timestamp no soportado: " + v.getClass());
    }

    public static LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof java.sql.Date d) return d.toLocalDate();
        if (v instanceof java.util.Date d) return new java.sql.Date(d.getTime()).toLocalDate();
        throw new IllegalArgumentException("Tipo date no soportado: " + v.getClass());
    }
}
