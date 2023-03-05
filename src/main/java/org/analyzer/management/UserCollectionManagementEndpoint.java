package org.analyzer.management;

import lombok.NonNull;
import org.analyzer.service.exceptions.UnsupportedApplicationOperationException;
import org.analyzer.service.management.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "users")
public class UserCollectionManagementEndpoint extends MongoDBCollectionManagementEndpoint<UsersManagementService> {

    @Autowired
    protected UserCollectionManagementEndpoint(@NonNull UsersManagementService managementService) {
        super(managementService);
    }

    @ReadOperation
    public Map<String, Object> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            return Map.of(
                    "common", this.managementService.count(false),
                    "active", this.managementService.count(true)
            );
        }

        return super.read(operation);
    }

    @DeleteOperation
    public boolean delete(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation != null && "disable-user".equals(operation[0])) {

            if (operation.length == 2) {
                this.managementService.disableUser(operation[1]);
                return true;
            }

            throw new UnsupportedApplicationOperationException("Username not specified");
        }

        return super.delete(operation);
    }
}