package org.analyzer.logs.rest.hateoas;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.rest.ResourceLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.Method;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LinksCollector {

    private final Map<Class<?>, List<ResourceLink>> linksCache;

    @Autowired
    public LinksCollector(
            @NonNull ApplicationContext applicationContext,
            @Value("${spring.web-flux.base-path}") String apiPrefix) {
        final Map<Class<?>, List<ResourceLink>> collectedLinks = new HashMap<>();
        applicationContext.getBeansWithAnnotation(RestController.class)
                .values()
                .forEach(controller -> {
                    final Class<?> controllerClass = controller.getClass();
                    final RequestMapping baseMapping = controllerClass.getAnnotation(RequestMapping.class);
                    final String basePath = getFirstFromArrayOrDefault(baseMapping.path(), "");
                    final RequestMethod baseMethod = getFirstFromArrayOrDefault(baseMapping.method(), RequestMethod.GET);
                    Arrays.stream(controllerClass.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(NamedEndpoint.class))
                            .forEach(method -> {
                                final NamedEndpoint[] namedEndpoints = method.getAnnotationsByType(NamedEndpoint.class);
                                final Tuple2<String, RequestMethod> path2method = getMapping2HttpMethodFromJavaMethod(method);

                                if (path2method == null) {
                                    return;
                                }

                                for (final NamedEndpoint ne : namedEndpoints) {
                                    log.debug("Process endpoint configuration {}:{} for controller {}",
                                            ne.value(), ne.includeTo(), controllerClass);

                                    final String path = path2method.getT1();
                                    final ResourceLink link = new ResourceLink(ne.value(), apiPrefix + basePath + path, path2method.getT2());
                                    final List<ResourceLink> links = collectedLinks.getOrDefault(ne.includeTo(), new ArrayList<>());
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

    private Tuple2<String, RequestMethod> getMapping2HttpMethodFromJavaMethod(final Method method) {
        final GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return Tuples.of(getFirstFromArrayOrDefault(getMapping.path(), ""), RequestMethod.GET);
        }

        final PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return Tuples.of(getFirstFromArrayOrDefault(postMapping.path(), ""), RequestMethod.POST);
        }

        final PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return Tuples.of(getFirstFromArrayOrDefault(putMapping.path(), ""), RequestMethod.PUT);
        }

        final DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return Tuples.of(getFirstFromArrayOrDefault(deleteMapping.path(), ""), RequestMethod.DELETE);
        }

        final RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            return Tuples.of(getFirstFromArrayOrDefault(mapping.path(), ""), getFirstFromArray(mapping.method()));
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
