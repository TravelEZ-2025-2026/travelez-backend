package com.example.travelez.backend.common.api.doc;

import com.example.travelez.backend.common.api.BaseResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                value = "{\"code\": 401, " +
                                        "\"message\": \"Authentication required\", " +
                                        "\"data\": null, " +
                                        "\"success\": false}"))),
        @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                value = "{\"code\": 403, " +
                                        "\"message\": \"Access denied\", " +
                                        "\"data\": null, " +
                                        "\"success\": false}")))
})

public @interface ApiAuthResponses {
}
