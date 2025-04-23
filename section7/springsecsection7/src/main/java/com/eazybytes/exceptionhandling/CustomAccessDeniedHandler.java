package com.eazybytes.exceptionhandling;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Populate dynamic values
        String message =
                (accessDeniedException != null && accessDeniedException.getMessage() != null) ?
                        accessDeniedException.getMessage(): "Unauthorized";

        response.setLocale(Locale.ENGLISH);
        response.setHeader("eazybank-denied-reason", "Authorisation Failed!");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        String path = request.getServletPath();

        // Construct the JSON response
        String jsonResp =
                String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                        LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                        message, path);

        response.getWriter().append(jsonResp);
    }
}
