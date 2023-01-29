package org.analyzer.logs.rest;

import lombok.Value;
import org.springframework.http.HttpMethod;

@Value
public class ResourceLink {

    String rel;
    String href;
    HttpMethod method;
}
