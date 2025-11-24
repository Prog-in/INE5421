package uai.helcio.t2.entities;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a lexical Token produced by the lexical analyzer..
 *
 * @param type      The abstract symbol representing the kind of lexical unit.
 *                  This corresponds to a {@link Terminal} symbol in the grammar.
 * @param attribute The specific value associated with this token instance.
 *                  <p>
 *                  </p>
 */
public record Token(String type, String attribute) {

    /**
     * Returns the string representation of the token in the standard compiler notation.
     * <p>
     * Format: <code>&lt;type, attribute&gt;</code>
     * </p>
     *
     * @return A formatted string suitable for logging and debugging the token stream.
     */
    @Override
    public @NotNull String toString() {
        return String.format("<%s, %s>", type, attribute);
    }
}