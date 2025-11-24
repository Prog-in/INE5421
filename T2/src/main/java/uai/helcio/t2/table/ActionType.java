package uai.helcio.t2.table;

/**
 * Enumerates the possible actions that an LR/SLR parser can perform at each step of the parsing process.
 * <p>
 * The parsing table maps a pair of (State, Symbol) to one of these actions.
 * The parser's runtime engine switches on these values to determine how to manipulate the stack and input.
 * </p>
 */
public enum ActionType {

    /**
     * Shift Action (s).
     * <p>
     * Indicates that the parser should shift the current lookahead terminal onto the stack
     * and transition to a new state. This effectively consumes the input token.
     * </p>
     */
    SHIFT,

    /**
     * Reduce Action (r).
     * <p>
     * Indicates that the parser has recognized a complete Right-Hand Side of a production rule.
     * The parser should pop <i>n</i> items from the stack
     * and push the state determined by the GOTO table for the production's Left-Hand Side.
     * </p>
     */
    REDUCE,

    /**
     * Accept Action (acc).
     * <p>
     * Indicates that the parser has successfully recognized the start symbol of the augmented grammar and the input buffer is empty.
     * The parsing process terminates successfully.
     * </p>
     */
    ACCEPT,

    /**
     * Error Action.
     * <p>
     * Indicates that the current lookahead symbol is not valid for the current state according to the grammar.
     * The parsing process terminates with a syntax error.
     * </p>
     */
    ERROR
}