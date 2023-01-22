package org.analyzer.logs.service.std.postfilters;

public class PostFilterNotFoundException extends RuntimeException {

    public PostFilterNotFoundException(final String postFilterName) {
        super("Post filter " + postFilterName + " not found");
    }
}
