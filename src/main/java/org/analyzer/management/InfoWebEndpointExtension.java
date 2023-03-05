package org.analyzer.management;

import org.analyzer.service.management.LogsManagementService;
import org.analyzer.service.management.StatisticsManagementService;
import org.analyzer.service.management.UserQueriesManagementService;
import org.analyzer.service.management.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.stereotype.Component;

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
    public WebEndpointResponse<Map<String, Object>> info() {
        final Map<String, Object> info = this.delegate.info();

        info.put("search-subsystem",
                this.logsManagementService.existsIndex()
                        ? this.logsManagementService.indexInfo()
                        : Map.of("index-exists", false)
        );

        info.put("mongodb-users",
                this.usersManagementService.existsCollection()
                        ? this.usersManagementService.indexesInfo()
                        : Map.of("collection-exists", false)
        );

        info.put("mongodb-statistics",
                this.statisticsManagementService.existsCollection()
                        ? this.statisticsManagementService.indexesInfo()
                        : Map.of("collection-exists", false)
        );

        info.put("mongodb-queries",
                this.userQueriesManagementService.existsCollection()
                        ? this.userQueriesManagementService.indexesInfo()
                        : Map.of("collection-exists", false)
        );

        return new WebEndpointResponse<>(info);
    }
}
