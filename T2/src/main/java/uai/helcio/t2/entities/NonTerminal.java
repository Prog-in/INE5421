package uai.helcio.t2.entities;

/**
 * Represents a Non-Terminal symbol in a Context-Free Grammar (CFG).
 */
public class NonTerminal extends Symbol {

    /**
     * Static factory method to create an instance of a NonTerminal.
     *
     * @return A new {@code NonTerminal} instance.
     */
    public static NonTerminal of(String repr) {
        return new NonTerminal(repr);
    }

    /**
     * Private constructor to enforce the use of the factory method {@link #of(String)}.
     *
     * @param repr The raw string representation of the symbol.
     */
    private NonTerminal(String repr) {
        super(repr);
    }

    /**
     * Returns the string representation of this non-terminal, typically enclosed in angle brackets.
     *
     * @return The formatted string representation.
     */
    @Override
    public String toString() {
        return String.format("<%s>", repr);
    }

    /**
     * Checks if this object is equal to another object.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonTerminal)) {
            return false;
        }
        return super.equals(o);
    }
}