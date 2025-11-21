package uai.helcio.t2.entities;

public class NonTerminal extends Symbol {
    public static NonTerminal of(String repr) {
        return new NonTerminal(repr);
    }

    private NonTerminal(String repr) {
        super(repr);
    }

    @Override
    public String toString() {
        return String.format("<%s>", repr);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonTerminal)) {
            return false;
        }
        return super.equals(o);
    }
}
