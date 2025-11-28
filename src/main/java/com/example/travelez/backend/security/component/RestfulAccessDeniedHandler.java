package com.example.travelez.backend.security.component;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        BaseResponse<Object> baseResponse = BaseResponse.builder()
                .success(false)
                .code(ResultCode.FORBIDDEN.getCode())
                .message(ResultCode.FORBIDDEN.getMessage())
                .data(null)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(baseResponse));
    }

}
