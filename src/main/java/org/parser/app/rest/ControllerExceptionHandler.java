package org.parser.app.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value = { Exception.class, RuntimeException.class })
    public void handle(Exception e, HttpServletResponse response, HttpServletRequest request) {
        writeErrorToResponse(request, response, HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private void writeErrorToResponse(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final HttpStatus status,
            final Throwable ex) {
        response.setStatus(status.value());
        response.setContentType(MediaType.TEXT_PLAIN.toString());
        try {
            final var messageBytes = exceptionToString(ex).getBytes(StandardCharsets.UTF_8);
            response.setContentLength(messageBytes.length);
            response.getOutputStream().write(messageBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write text response", e);
        }
    }

    private String exceptionToString(Throwable e) {

        final var formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:S");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exception: %s\r\n", e.getClass().getName()));
        sb.append(String.format("Message: %s\r\n", e.getMessage() == null ? "" : e.getMessage()));
        sb.append(String.format("Current date: %s\r\n", formatter.format(new Date())));

        final var stackTrace = new StringWriter();
        final var printWriter = new PrintWriter(stackTrace);
        e.printStackTrace(printWriter);
        sb.append(String.format("Stack trace: %s\r\n", stackTrace.toString()));
        return sb.toString();
    }
}
