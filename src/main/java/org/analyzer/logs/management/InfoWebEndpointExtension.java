package org.analyzer.logs.management;

import org.analyzer.logs.service.management.LogsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@EndpointWebExtension(endpoint = InfoEndpoint.class)
public class InfoWebEndpointExtension {

    @Autowired
    private InfoEndpoint delegate;
    @Autowired
    private LogsManagementService managementService;

    @ReadOperation
    public Mono<WebEndpointResponse<Map<String, Object>>> info() {
        return Mono.fromSupplier(() -> {

            final Map<String, Object> info = this.delegate.info();
            final Optional<Boolean> indexExists = this.managementService.existsIndex().blockOptional();
            info.put("elasticsearch", indexExists.isPresent()
                    ? this.managementService.indexInfo().block()
                    : Collections.singletonMap("index-exists", false));
            return new WebEndpointResponse<>(info);
        });
    }
}
