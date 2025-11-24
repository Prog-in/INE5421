package uai.helcio.t2.table;

import org.jetbrains.annotations.NotNull;
import uai.helcio.t2.entities.Item;

/**
 * Represents a single cell entry in the SLR Parsing Table.
 * <p>
 * Each entry dictates the action the parser must take when it is in a specific state
 * and encounters a specific lookahead symbol. The table is essentially a matrix of these entries.
 * </p>
 *
 * @param type               The type of action to perform.
 * @param targetState        The index of the next state to push onto the stack.
 *                           <b>Only valid when type is {@link ActionType#SHIFT}.</b>
 * @param productionToReduce The grammar production rule to use for reduction.
 *                           <b>Only valid when type is {@link ActionType#REDUCE}.</b>
 */
public record TableEntry(ActionType type, int targetState, Item productionToReduce) {

    /**
     * Factory method to create a SHIFT action entry.
     * <p>
     * A shift action pushes the current token onto the stack and transitions the parser
     * to the specified <code>state</code>.
     * </p>
     *
     * @param state The index of the target state to transition to.
     * @return A {@code TableEntry} configured for a SHIFT action.
     */
    public static TableEntry shift(int state) {
        return new TableEntry(ActionType.SHIFT, state, null);
    }

    /**
     * Factory method to create a REDUCE action entry.
     * <p>
     * A reduce action indicates that a handle has been recognized. The parser will pop
     * symbols corresponding to the production body and transition based on the production head.
     * </p>
     *
     * @param item The specific production rule that is being reduced.
     *             (e.g., <code>A -> alpha .</code>)
     * @return A {@code TableEntry} configured for a REDUCE action.
     */
    public static TableEntry reduce(Item item) {
        return new TableEntry(ActionType.REDUCE, -1, item);
    }

    /**
     * Factory method to create an ACCEPT action entry.
     * <p>
     * An accept action signals that the input has been successfully parsed according
     * to the grammar rules.
     * </p>
     *
     * @return A {@code TableEntry} configured for an ACCEPT action.
     */
    public static TableEntry accept() {
        return new TableEntry(ActionType.ACCEPT, -1, null);
    }

    /**
     * Returns a compact string representation of the table entry, useful for logging and debugging.
     * <p>
     * Formats:
     * <ul>
     * <li>SHIFT: <code>"s{stateIndex}"</code</li>
     * <li>REDUCE: <code>"r({Head}->{Body})"</code></li>
     * <li>ACCEPT: <code>"acc"</code></li>
     * <li>ERROR: <code>"err"</code></li>
     * </ul>
     * </p>
     *
     * @return The short string code for the action.
     */
    @Override
    public @NotNull String toString() {
        return switch (type) {
            case SHIFT -> "s" + targetState;
            case REDUCE -> "r(" + productionToReduce.head() + "->" + productionToReduce.body() + ")";
            case ACCEPT -> "acc";
            case ERROR -> "err";
        };
    }
}