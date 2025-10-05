package com.ahss.dto.response;

import java.time.Instant;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String timestamp;
    private String path;

    public static <T> ApiResponse<T> ok(T data, String message, String path) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        resp.message = message;
        resp.timestamp = Instant.now().toString();
        resp.path = path;
        return resp;
    }

    public static <T> ApiResponse<T> notOk(T data, String message, String path) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = false;
        resp.data = data;
        resp.message = message;
        resp.timestamp = Instant.now().toString();
        resp.path = path;
        return resp;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}