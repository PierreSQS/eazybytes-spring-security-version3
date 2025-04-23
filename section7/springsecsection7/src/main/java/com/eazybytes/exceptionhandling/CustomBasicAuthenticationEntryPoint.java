package com.eazybytes.exceptionhandling;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Populate dynamic values
        String message =
                (authException != null && authException.getMessage() != null) ?
                        authException.getMessage(): "Unauthenticated";

        response.setLocale(Locale.ENGLISH);
        response.setHeader("eazybank-error-reason", "Authentication Failed!");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        String path = request.getServletPath();

        // Construct the JSON response
        String jsonResp =
                String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                        LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message, path);

        response.getWriter().append(jsonResp);
    }
}
