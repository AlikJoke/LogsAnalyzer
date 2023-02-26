package org.analyzer.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.service.exceptions.EntityNotFoundException;
import org.analyzer.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.service.exceptions.UserAlreadyExistsException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ControllerBase {

    @RequestMapping(method = RequestMethod.OPTIONS)
    public void options(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic().getHeaderValue());
        final var methodsString = supportedMethods()
                .stream()
                .map(HttpMethod::name)
                .collect(Collectors.joining(", "));
        response.setHeader(HttpHeaders.ALLOW, methodsString);
        if (request.getHeader(HttpHeaders.ORIGIN) != null) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, methodsString);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ExceptionResource> commonHandler(RuntimeException ex) {
        log.error("", ex);
        return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ExceptionResource(exceptionToString(ex)));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<ExceptionResource> clientErrorHandler(HttpClientErrorException ex) {
        log.error("", ex);
        return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(new ExceptionResource(exceptionToString(ex)));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<ExceptionResource> entityNotFoundHandler(RuntimeException ex) {
        log.error("", ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResource(exceptionToString(ex)));
    }

    @ExceptionHandler({UserAlreadyDisabledException.class, UserAlreadyExistsException.class})
    protected ResponseEntity<ExceptionResource> userOperationsHandler(RuntimeException ex) {
        log.error("", ex);
        return ResponseEntity
                    .badRequest()
                    .body(new ExceptionResource(exceptionToString(ex)));
    }

    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET);
    }

    private String exceptionToString(Throwable e) {
        final var sb = new StringBuilder();
        sb.append(String.format("Exception: %s\r\n", e.getClass().getName()));
        sb.append(String.format("Message: %s\r\n", e.getMessage() == null ? "" : e.getMessage()));
        sb.append(String.format("Current date: %s\r\n", LocalDateTime.now()));

        final var stackTrace = new StringWriter();
        final var printWriter = new PrintWriter(stackTrace);
        e.printStackTrace(printWriter);
        sb.append(String.format("Stack trace: %s\r\n", stackTrace));

        return sb.toString();
    }
}
