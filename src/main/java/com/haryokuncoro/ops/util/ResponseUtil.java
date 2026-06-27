package com.haryokuncoro.ops.util;

import com.haryokuncoro.ops.dto.ApiResponse;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static ApiResponse<Void> success(String message) {
        return success(message);
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}