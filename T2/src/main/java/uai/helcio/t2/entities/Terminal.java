package uai.helcio.t2.entities;

public class Terminal extends Symbol {
    public static final String EPSILON_REPR = "&";
    public static final Terminal EPSILON = new Terminal(EPSILON_REPR);
    public static final String END_REPR = "$";
    public static final Terminal END = new Terminal(END_REPR);

    public static Terminal of(String repr) {
        if (END_REPR.equals(repr)) {
            return EPSILON;
        }
        return new Terminal(repr);
    }

    private Terminal(String repr) {
        super(repr);
    }

    @Override
    public String toString() {
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Terminal)) {
            return false;
        }
        return super.equals(o);
    }
}
