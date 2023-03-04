package org.analyzer.config.lucene;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Value
@RequiredArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties("logs.analyzer.lucene.index")
public class LuceneConfiguration {

    LuceneIndex type;
    String storagePath;
    int bufferSizeMb;
}
