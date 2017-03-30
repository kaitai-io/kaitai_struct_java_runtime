package io.kaitai.struct;

public class ConsistencyError extends RuntimeException {
    private String id;
    private String actual;
    private String expected;

    public ConsistencyError(String id, String actual, String expected) {
        super("Check failed: " + id + ", expected: " + expected + ", actual: " + actual);

        this.id = id;
        this.actual = actual;
        this.expected = expected;
    }

    public ConsistencyError(String id, long actual, long expected) {
        this(id, "" + actual, "" + expected);
    }

    public String id() { return id; }
    public String actual() { return actual; }
    public String expected() { return expected; }
}
