package org.analyzer.logs.events;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Slf4j
abstract class MongoEventGenerationListener<T> extends AbstractMongoEventListener<T> {

    private static final String ID_KEY = "_id";

    protected final ReactiveRedisTemplate<String, Object> redisTemplate;
    protected final String topicName;

    protected MongoEventGenerationListener(
            @NonNull ReactiveRedisTemplate<String, Object> redisTemplate,
            @NonNull String topicName) {
        this.redisTemplate = redisTemplate;
        this.topicName = topicName;
    }

    @Override
    public void onAfterSave(@NonNull AfterSaveEvent<T> event) {
        super.onAfterSave(event);
        final EntitySavedEvent<T> eventToSend =
                new EntitySavedEvent<T>()
                        .setEntity(buildEntityToSend(event.getSource()))
                        .setSourceCollection(event.getCollectionName());
        sendEventToTopic(eventToSend);
    }

    @Override
    public void onAfterDelete(@NonNull AfterDeleteEvent<T> event) {
        super.onAfterDelete(event);
        final EntityDeletedEvent eventToSend =
                new EntityDeletedEvent()
                        .setEntityId(event.getSource().getString(ID_KEY))
                        .setSourceCollection(event.getCollectionName());
        sendEventToTopic(eventToSend);
    }

    protected abstract T buildEntityToSend(final T sourceEntity);

    protected void sendEventToTopic(final Object source) {
        this.redisTemplate.convertAndSend(this.topicName, source)
                .subscribe(
                        count -> log.info("Received by {} consumers", count),
                        ex -> log.error("", ex)
                );
    }
}
