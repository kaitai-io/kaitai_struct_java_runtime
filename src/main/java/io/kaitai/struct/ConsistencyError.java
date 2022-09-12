package io.kaitai.struct;

public class ConsistencyError extends RuntimeException {
    private final String id;
    private final Object actual;
    private final Object expected;

    public ConsistencyError(String id, Object actual, Object expected) {
        super("Check failed: " + id + ", expected: " + expected + ", actual: " + actual);

        this.id = id;
        this.actual = actual;
        this.expected = expected;
    }

    public String id() { return id; }
    public Object actual() { return actual; }
    public Object expected() { return expected; }

    public static class SizeMismatch extends ConsistencyError {
        public SizeMismatch(String id, long actual, long expected) {
            super(id, actual, expected);
        }
    }
}
