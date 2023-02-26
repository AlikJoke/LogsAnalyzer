package org.analyzer.service.management.std;

import lombok.NonNull;
import org.analyzer.dao.HttpArchiveRepository;
import org.analyzer.entities.HttpArchiveEntity;
import org.analyzer.service.management.HttpArchivesManagementService;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoDBHttpArchivesManagementService extends MongoDBManagementServiceWithUserCountersBase<HttpArchiveEntity> implements HttpArchivesManagementService {

    private final HttpArchiveRepository httpArchiveRepository;

    @Autowired
    MongoDBHttpArchivesManagementService(
            @NonNull UserService userService,
            @NonNull HttpArchiveRepository httpArchiveRepository,
            @NonNull MongoTemplate template) {
        super(userService, template, HttpArchiveEntity.class);
        this.httpArchiveRepository = httpArchiveRepository;
    }

    @Override
    public long commonCount() {
        return this.httpArchiveRepository.count();
    }
}
