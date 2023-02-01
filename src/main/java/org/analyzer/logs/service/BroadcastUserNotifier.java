package org.analyzer.logs.service;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface BroadcastUserNotifier {

    @Nonnull
    Mono<Void> broadcast(@Nonnull String message);
}
