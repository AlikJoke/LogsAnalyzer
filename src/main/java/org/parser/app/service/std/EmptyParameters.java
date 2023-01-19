package org.parser.app.service.std;

import lombok.NonNull;

public final class EmptyParameters {

    private static final EmptyParameters instance = new EmptyParameters();

    @NonNull
    public static EmptyParameters getInstance() {
        return instance;
    }

    private EmptyParameters() {
    }
}
