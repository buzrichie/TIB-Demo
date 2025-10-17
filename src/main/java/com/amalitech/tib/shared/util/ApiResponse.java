package com.amalitech.tib.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A generic class representing an API response.
 *
 * @param <T> the type of the data payload or errors
 * @param success indicates whether the API request was successful or not
 * @param message provides a descriptive message about the response
 * @param data contains the actual data returned by the API, could be null if there is an error
 * @param timestamp indicates when the response was generated
 * @param errors contains a list of errors in case of a failed API request, could be null if not applicable
 */
public record ApiResponse<T>(
        @JsonProperty("success")
        boolean success,

        @JsonProperty("message")
        String message,

        @JsonProperty("data")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data,

        @JsonProperty("timestamp")
        String timestamp,

        @JsonProperty("errors")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<T> errors
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data,
                LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data,
                LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> error(String message, List<T> errors) {
        return new ApiResponse<>(false, message, null,
                LocalDateTime.now().toString(), errors);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null,
                LocalDateTime.now().toString(), null);
    }
}