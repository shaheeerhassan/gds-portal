package com.school.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private int status;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, 200);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data, null, 200);
    }

    public static ApiResponse<Void> error(String message, int status) {
        return new ApiResponse<>(false, message, null, null, status);
    }

    public static ApiResponse<Void> error(String message, int status, List<String> errors) {
        return new ApiResponse<>(false, message, null, errors != null ? errors : new ArrayList<>(), status);
    }
}
