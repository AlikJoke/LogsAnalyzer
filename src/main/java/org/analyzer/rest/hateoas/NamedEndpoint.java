package org.analyzer.rest.hateoas;

import javax.annotation.Nonnull;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NamedEndpoints.class)
public @interface NamedEndpoint {

    @Nonnull
    String value();

    @Nonnull
    Class<?> includeTo();
}
