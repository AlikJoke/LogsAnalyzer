package org.analyzer.logs.management;

import lombok.NonNull;
import org.analyzer.logs.service.management.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Endpoint(id = "users")
public class UserCollectionManagementEndpoint extends MongoDBCollectionManagementEndpoint<UsersManagementService> {

    @Autowired
    protected UserCollectionManagementEndpoint(@NonNull UsersManagementService managementService) {
        super(managementService);
    }

    @ReadOperation
    public Mono<Map<String, Object>> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            return this.managementService
                            .count(false)
                            .zipWith(this.managementService.count(true))
                            .map(tuple -> Map.of("common", tuple.getT1(), "active", tuple.getT2()));
        }

        return super.read(operation);
    }

    @DeleteOperation
    public Mono<Boolean> delete(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation != null && "disable-user".equals(operation[0])) {

            if (operation.length == 2) {
                return this.managementService.disableUser(operation[1]);
            }

            return Mono.error(() -> new UnsupportedOperationException("Username not specified"));
        }

        return super.delete(operation);
    }
}