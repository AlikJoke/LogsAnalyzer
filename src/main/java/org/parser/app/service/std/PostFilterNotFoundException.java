package org.parser.app.service.std;

public class PostFilterNotFoundException extends RuntimeException {

    public PostFilterNotFoundException(final String postFilterName) {
        super("Post filter " + postFilterName + " not found");
    }
}
