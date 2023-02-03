package org.analyzer.logs.rest.queries;

import org.analyzer.logs.rest.ControllerBase;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping(UserSearchQueriesController.PATH_BASE)
public class UserSearchQueriesController extends ControllerBase {

    static final String PATH_BASE = "/queries";

    // TODO

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.DELETE);
    }
}
