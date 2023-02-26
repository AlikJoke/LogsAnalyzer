package org.analyzer.config.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogAndStopIOExceptionFilter implements org.asynchttpclient.filter.IOExceptionFilter {

    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        log.error("IO Exception occurred while async http request: response status is " + ctx.getResponseStatus().getStatusCode(), ctx.getIOException());
        throw new FilterException("Execution was interrupted due to IO error", ctx.getIOException());
    }
}
