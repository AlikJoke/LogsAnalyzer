package org.analyzer.logs.management;

import org.analyzer.logs.service.management.LogsManagementService;
import org.analyzer.logs.service.management.StatisticsManagementService;
import org.analyzer.logs.service.management.UserQueriesManagementService;
import org.analyzer.logs.service.management.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
@EndpointWebExtension(endpoint = InfoEndpoint.class)
public class InfoWebEndpointExtension {

    @Autowired
    private InfoEndpoint delegate;
    @Autowired
    private LogsManagementService logsManagementService;
    @Autowired
    private UsersManagementService usersManagementService;
    @Autowired
    private StatisticsManagementService statisticsManagementService;
    @Autowired
    private UserQueriesManagementService userQueriesManagementService;

    @ReadOperation
    public Mono<WebEndpointResponse<Map<String, Object>>> info() {
        final Map<String, Object> info = this.delegate.info();
        return
            this.logsManagementService.existsIndex()
                    .zipWith(this.logsManagementService.indexInfo())
                    .doOnNext(tuple -> {
                        info.put("elasticsearch",
                                    tuple.getT1()
                                            ? tuple.getT2()
                                            : Collections.singletonMap("index-exists", false)
                        );
                    })
                    .mergeWith(this.usersManagementService.existsCollection()
                            .zipWith(this.usersManagementService.indexesInfo())
                            .doOnNext(tuple -> {
                                info.put("mongodb-users",
                                            tuple.getT1()
                                                    ? tuple.getT2()
                                                    : Collections.singletonMap("collection-exists", false)
                                );
                            }))
                    .mergeWith(this.statisticsManagementService.existsCollection()
                            .zipWith(this.statisticsManagementService.indexesInfo())
                            .doOnNext(tuple -> {
                                info.put("mongodb-statistics",
                                            tuple.getT1()
                                                    ? tuple.getT2()
                                                    : Collections.singletonMap("collection-exists", false)
                                );
                            }))
                    .mergeWith(this.userQueriesManagementService.existsCollection()
                            .zipWith(this.userQueriesManagementService.indexesInfo())
                            .doOnNext(tuple -> {
                                info.put("mongodb-queries",
                                        tuple.getT1()
                                                ? tuple.getT2()
                                                : Collections.singletonMap("collection-exists", false)
                                );
                            }))
                    .then(Mono.just(new WebEndpointResponse<>(info)));
    }
}
