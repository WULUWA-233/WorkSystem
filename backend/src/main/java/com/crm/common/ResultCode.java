package com.crm.common;

/**
 * Unified Response Status Codes
 *
 * @author CRM Team
 */
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_ERROR(500, "Internal Server Error"),
    BUSINESS_ERROR(1001, "Business Error"),
    PARAM_ERROR(1002, "Parameter Validation Error"),
    DATA_NOT_FOUND(1003, "Data Not Found"),
    DUPLICATE_KEY(1004, "Duplicate Data"),
    OPERATION_FAILED(1005, "Operation Failed");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
