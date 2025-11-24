package uai.helcio.t2.entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents an LR(0) Item, which is a fundamental unit in the construction of SLR parsing tables.
 * <p>
 * An item is a production rule from the grammar with a dot (•) at some position in the right-hand side.
 * The dot indicates how much of the production has been scanned/parsed so far.
 * </p>
 * <p>
 * Example: For a production <code>A -> X Y Z</code>:
 * <ul>
 * <li><code>A -> • X Y Z</code></li>
 * <li><code>A -> X • Y Z</code></li>
 * <li><code>A -> X Y Z •</code></li>
 * </ul>
 * </p>
 *
 * @param head        The NonTerminal on the left-hand side of the production.
 * @param body        The list of Symbols representing the right-hand side of the production.
 * @param dotPosition The current index of the dot within the body.
 */
public record Item(NonTerminal head, List<Symbol> body, int dotPosition) {

    /**
     * Retrieves the symbol immediately following the current dot position.
     * <p>
     * This symbol represents the next expected token or non-terminal in the parsing process.
     * </p>
     *
     * @return The {@link Symbol} after the dot, or {@code null} if the dot is at the end of the production
     * or if the symbol is {@link Terminal#EPSILON}.
     */
    public Symbol getSymbolAfterDot() {
        if (dotPosition >= body.size()) {
            return null;
        }
        Symbol s = body.get(dotPosition);
        if (s.equals(Terminal.EPSILON)) {
            return null;
        }
        return s;
    }

    /**
     * Creates a new Item with the dot advanced by one position to the right.
     * <p>
     * This operation corresponds to the "Shift" action or moving over a NonTerminal in a GOTO operation.
     * </p>
     *
     * @return A new {@code Item} instance representing the state after consuming the symbol at the current dot.
     * @throws IllegalStateException if the item is already completed (the dot is at the end) and it is not an Epsilon production.
     */
    public Item advance() {
        if (getSymbolAfterDot() == null && !isEpsilonProduction()) {
            throw new IllegalStateException("Cannot advance: item already completed");
        }
        return new Item(head, body, dotPosition + 1);
    }

    /**
     * Determines if this item represents a completed production ready for reduction.
     *
     * @return {@code true} if the parser can perform a REDUCE action based on this item; {@code false} otherwise.
     */
    public boolean isReduce() {
        if (isEpsilonProduction()) return true;
        return dotPosition == body.size();
    }

    /**
     * Helper method to check if this is an Epsilon production
     *
     * @return {@code true} if the body contains only the EPSILON terminal.
     */
    private boolean isEpsilonProduction() {
        return body.size() == 1 && body.getFirst().equals(Terminal.EPSILON);
    }

    /**
     * Checks equality based on the head, the body, and the exact position of the dot.
     *
     * @param o The object to compare.
     * @return {@code true} if the items are identical in structure and state.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item(NonTerminal head1, List<Symbol> body1, int position))) return false;
        return dotPosition == position &&
                Objects.equals(head, head1) &&
                Objects.equals(body, body1);
    }

    /**
     * Returns a string representation of the item, visually indicating the dot's position.
     * <p>
     * Example format: <code>&lt;A&gt; -> a • &lt;B&gt; c</code>
     * </p>
     *
     * @return A formatted string useful for debugging and logging the parsing tables.
     */
    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.getRepr()).append(" -> ");
        if (isEpsilonProduction()) {
            sb.append("• (ε)");
        } else {
            for (int i = 0; i < body.size(); i++) {
                if (i == dotPosition) sb.append("• ");
                sb.append(body.get(i).getRepr()).append(" ");
            }
            if (dotPosition == body.size()) sb.append("•");
        }
        return sb.toString();
    }
}