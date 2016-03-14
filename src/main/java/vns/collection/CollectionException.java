package vns.collection;

/**
 * CollectionException
 */
public class CollectionException extends RuntimeException {
    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(String message, String expression) {
        super(message + "; expession: " + expression);
    }
}
