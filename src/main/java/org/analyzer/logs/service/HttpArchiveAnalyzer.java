package org.analyzer.logs.service;

import org.analyzer.logs.model.HttpArchiveBody;

import javax.annotation.Nonnull;
import java.util.Map;

public interface HttpArchiveAnalyzer {

    @Nonnull
    Map<String, Object> analyze(@Nonnull HttpArchiveBody httpArchiveBody);
}
