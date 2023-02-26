package org.analyzer.rest;

import org.springframework.web.bind.annotation.RequestMethod;

public record ResourceLink(String rel, String href, RequestMethod method) {
}
