package org.analyzer.events;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

@Slf4j
abstract class MongoEventGenerationListener<T> extends AbstractMongoEventListener<T> {

    private static final String ID_KEY = "_id";

    protected final EventSender eventSender;
    protected final String channel;

    protected MongoEventGenerationListener(
            @NonNull EventSender eventSender,
            @NonNull String channel) {
        this.eventSender = eventSender;
        this.channel = channel;
    }

    @Override
    public void onAfterSave(@NonNull AfterSaveEvent<T> event) {
        super.onAfterSave(event);
        final var eventToSend =
                new EntitySavedEvent<T>()
                        .setEntity(buildEntityToSend(event.getSource()))
                        .setSourceCollection(event.getCollectionName());
        sendEvent(eventToSend);
    }

    @Override
    public void onAfterDelete(@NonNull AfterDeleteEvent<T> event) {
        super.onAfterDelete(event);
        final var eventToSend =
                new EntityDeletedEvent()
                        .setEntityId(event.getSource().getString(ID_KEY))
                        .setSourceCollection(event.getCollectionName());
        sendEvent(eventToSend);
    }

    protected abstract T buildEntityToSend(final T sourceEntity);

    protected void sendEvent(final Object source) {
        this.eventSender.send(this.channel, source);
    }
}
