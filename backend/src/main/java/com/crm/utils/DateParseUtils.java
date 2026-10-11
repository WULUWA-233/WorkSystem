package com.crm.utils;

import com.crm.common.BusinessException;
import com.crm.common.ResultCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateParseUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private DateParseUtils() {
    }

    public static LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) throw invalidDate(fieldName, "yyyy-MM-dd");
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw invalidDate(fieldName, "yyyy-MM-dd");
        }
    }

    public static LocalDateTime parseDateTime(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) throw invalidDate(fieldName, "yyyy-MM-dd HH:mm:ss");
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw invalidDate(fieldName, "yyyy-MM-dd HH:mm:ss");
        }
    }

    private static BusinessException invalidDate(String fieldName, String pattern) {
        return new BusinessException(ResultCode.PARAM_ERROR,
                fieldName + " must match format " + pattern);
    }
}
