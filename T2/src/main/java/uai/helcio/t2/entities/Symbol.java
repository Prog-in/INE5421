package uai.helcio.t2.entities;

import java.util.Objects;

public abstract class Symbol {
    protected final String repr;

    public Symbol(String repr) {
        this.repr = Objects.requireNonNullElse(repr, "");
    }

    public abstract String toString();

    @Override
    final public int hashCode() {
        return repr.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Symbol otherSymbol)) {
            return false;
        }
        return repr.equals(otherSymbol.repr);
    }

    public final boolean isTerminal() {
        return this instanceof Terminal;
    }

    public final boolean isNonTerminal() {
        return this instanceof NonTerminal;
    }

    public String getRepr() {
        return repr;
    }
}
