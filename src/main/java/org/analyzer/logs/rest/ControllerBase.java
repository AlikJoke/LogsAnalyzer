package org.analyzer.logs.rest;

import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestControllerAdvice
public class ControllerBase {

    private final Logger logger = Loggers.getLogger(getClass());

    @RequestMapping(method = RequestMethod.OPTIONS)
    public Mono<Void> options(ServerRequest request, ServerResponse response) {
        response.headers().setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        response.headers().setAllow(supportedMethods());
        if (!request.headers().header(HttpHeaders.ORIGIN).isEmpty()) {
            response.headers().setAccessControlAllowMethods(List.copyOf(supportedMethods()));
        }

        return Mono.empty();
    }

    @ExceptionHandler(RuntimeException.class)
    protected Mono<ResponseEntity<ExceptionResource>> commonHandler(RuntimeException ex) {
        logger.error("", ex);
        return exceptionToString(ex)
                    .map(ExceptionResource::new)
                    .map(resource ->
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(resource)
                    );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    protected Mono<ResponseEntity<ExceptionResource>> clientErrorHandler(HttpClientErrorException ex) {
        logger.error("", ex);
        return exceptionToString(ex)
                .map(ExceptionResource::new)
                .map(resource ->
                        ResponseEntity.status(ex.getStatusCode()).body(resource)
                );
    }

    @ExceptionHandler({ UserAlreadyDisabledException.class, UserNotFoundException.class, UserAlreadyExistsException.class})
    protected Mono<ResponseEntity<ExceptionResource>> userNotFoundHandler(RuntimeException ex) {
        logger.error("", ex);
        return exceptionToString(ex)
                .map(ExceptionResource::new)
                .map(resource ->
                        ResponseEntity.badRequest().body(resource)
                );
    }

    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET);
    }

    protected <T> Mono<T> onError(final Throwable ex) {
        logger.error("", ex);
        return Mono.error(ex);
    }

    private Mono<String> exceptionToString(Throwable e) {
        return Mono.fromSupplier(() -> {
            final var sb = new StringBuilder();
            sb.append(String.format("Exception: %s\r\n", e.getClass().getName()));
            sb.append(String.format("Message: %s\r\n", e.getMessage() == null ? "" : e.getMessage()));
            sb.append(String.format("Current date: %s\r\n", LocalDateTime.now()));

            final var stackTrace = new StringWriter();
            final var printWriter = new PrintWriter(stackTrace);
            e.printStackTrace(printWriter);
            sb.append(String.format("Stack trace: %s\r\n", stackTrace.toString()));

            return sb.toString();
        });
    }
}
