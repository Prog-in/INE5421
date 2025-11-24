package uai.helcio.t2.entities;

import java.util.Objects;

/**
 * Abstract base class representing a symbol in a Context-Free Grammar.
 * <p>
 * A symbol acts as the fundamental building block of a grammar and can be either a
 * {@link Terminal} or a {@link NonTerminal}.
 * </p>
 */
public abstract class Symbol {

    /**
     * The raw string representation of the symbol.
     */
    protected final String repr;

    /**
     * Constructs a new Symbol with the given string representation.
     *
     * @param repr The string representation of the symbol. If {@code null}, it defaults to an empty string.
     */
    public Symbol(String repr) {
        this.repr = Objects.requireNonNullElse(repr, "");
    }

    /**
     * Returns the string representation of this symbol.
     *
     * @return The formatted string representation.
     */
    public abstract String toString();

    /**
     * Computes the hash code based on the string representation.
     *
     * @return The hash code of the symbol's representation.
     */
    @Override
    final public int hashCode() {
        return repr.hashCode();
    }

    /**
     * Checks if this symbol is equal to another object.
     *
     * @param o The object to compare with.
     * @return {@code true} if the symbols are equal, {@code false} otherwise.
     */
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

    /**
     * Utility method to check if this symbol is a Terminal.
     *
     * @return {@code true} if this instance is of type {@link Terminal}, {@code false} otherwise.
     */
    public final boolean isTerminal() {
        return this instanceof Terminal;
    }

    /**
     * Utility method to check if this symbol is a NonTerminal.
     *
     * @return {@code true} if this instance is of type {@link NonTerminal}, {@code false} otherwise.
     */
    public final boolean isNonTerminal() {
        return this instanceof NonTerminal;
    }

    /**
     * Retrieves the raw string representation of this symbol.
     *
     * @return The symbol's internal string value.
     */
    public String getRepr() {
        return repr;
    }
}