package org.analyzer.service.logs.std.postfilters;

public class PostFilterNotFoundException extends RuntimeException {

    public PostFilterNotFoundException(final String postFilterName) {
        super("Post filter " + postFilterName + " not found");
    }
}
