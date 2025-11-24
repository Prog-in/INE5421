package uai.helcio.t2.entities;

/**
 * Represents a Terminal symbol in a Context-Free Grammar.
 *
 */
public class Terminal extends Symbol {

    /**
     * The string representation for the Epsilon symbol.
     */
    public static final String EPSILON_REPR = "&";

    /**
     * The singleton instance representing the Epsilon symbol.
     */
    public static final Terminal EPSILON = new Terminal(EPSILON_REPR);

    /**
     * The string representation for the End-of-Input symbol.
     */
    public static final String END_REPR = "$";

    /**
     * The singleton instance representing the End-of-Input marker ($).
     */
    public static final Terminal END = new Terminal(END_REPR);

    /**
     * Static factory method to obtain a Terminal instance.
     *
     * @param repr The string representation of the lexema.
     * @return A {@code Terminal} instance corresponding to the representation.
     */
    public static Terminal of(String repr) {
        if (END_REPR.equals(repr)) {
            return EPSILON;
        }
        return new Terminal(repr);
    }

    /**
     * Private constructor to enforce the use of the factory method {@link #of(String)}
     * and standard constants.
     *
     * @param repr The raw string representation of the terminal.
     */
    private Terminal(String repr) {
        super(repr);
    }

    /**
     * Returns the string representation of this terminal.
     *
     * @return The terminal's string value.
     */
    @Override
    public String toString() {
        return repr;
    }

    /**
     * Checks if this object is equal to another object.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Terminal)) {
            return false;
        }
        return super.equals(o);
    }
}