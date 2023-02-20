package org.analyzer.logs.rest.hateoas;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.rest.ResourceLink;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@Lazy
public class LinksCollector {

    private final Map<Class<?>, List<ResourceLink>> linksCache;

    @Autowired
    public LinksCollector(
            @NonNull ApplicationContext applicationContext,
            @Value("${server.servlet.context-path}") String apiPrefix) {
        final Map<Class<?>, List<ResourceLink>> collectedLinks = new HashMap<>();
        applicationContext.getBeansWithAnnotation(RestController.class)
                .values()
                .forEach(controller -> {
                    final var controllerClass = controller.getClass();
                    final var baseMapping = controllerClass.getAnnotation(RequestMapping.class);
                    final var basePath = getFirstFromArrayOrDefault(baseMapping == null ? null : baseMapping.value(), "");
                    final var baseMethod = getFirstFromArrayOrDefault(baseMapping == null ? null : baseMapping.method(), RequestMethod.GET);
                    Arrays.stream(controllerClass.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(NamedEndpoint.class) || method.isAnnotationPresent(NamedEndpoints.class))
                            .forEach(method -> {
                                final var namedEndpoints = method.getAnnotationsByType(NamedEndpoint.class);
                                final var path2method = getMapping2HttpMethodFromJavaMethod(method);

                                if (path2method == null) {
                                    return;
                                }

                                for (final var ne : namedEndpoints) {
                                    log.debug("Process endpoint configuration {}:{} for controller {}",
                                            ne.value(), ne.includeTo(), controllerClass);

                                    final var path = path2method.getKey();
                                    final var link = new ResourceLink(ne.value(), apiPrefix + basePath + path, path2method.getValue());
                                    final var links = collectedLinks.getOrDefault(ne.includeTo(), new ArrayList<>());
                                    links.add(link);
                                    collectedLinks.putIfAbsent(ne.includeTo(), links);
                                }
                            });
                });

        log.debug("All links has been collected: {}", collectedLinks.size());

        this.linksCache = collectedLinks;
    }

    @NonNull
    public List<ResourceLink> collectFor(@NonNull Class<?> resourceClass) {
        return this.linksCache.getOrDefault(resourceClass, Collections.emptyList());
    }

    private Pair<String, RequestMethod> getMapping2HttpMethodFromJavaMethod(final Method method) {
        final var getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return ImmutablePair.of(getFirstFromArrayOrDefault(getMapping.value(), ""), RequestMethod.GET);
        }

        final var postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return ImmutablePair.of(getFirstFromArrayOrDefault(postMapping.value(), ""), RequestMethod.POST);
        }

        final var putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return ImmutablePair.of(getFirstFromArrayOrDefault(putMapping.value(), ""), RequestMethod.PUT);
        }

        final var deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return ImmutablePair.of(getFirstFromArrayOrDefault(deleteMapping.value(), ""), RequestMethod.DELETE);
        }

        final var mapping = method.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            return ImmutablePair.of(getFirstFromArrayOrDefault(mapping.value(), ""), getFirstFromArray(mapping.method()));
        }

        return null;
    }

    private <T> T getFirstFromArrayOrDefault(final T[] array, final T defaultVal) {
        return array == null || array.length == 0 ? defaultVal : array[0];
    }

    private <T> T getFirstFromArray(final T[] array) {
        return getFirstFromArrayOrDefault(array, null);
    }
}
