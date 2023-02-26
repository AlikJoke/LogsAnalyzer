package org.analyzer.service.har;

import javax.annotation.Nonnull;
import java.util.Map;

public interface HttpArchiveAnalyzer {

    @Nonnull
    Map<String, Object> analyze(@Nonnull HttpArchiveBody httpArchiveBody);
}
