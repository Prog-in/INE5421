package uai.helcio.t2.table;

/**
 * Categorizes the symbols stored in the Symbol Table based on their semantic role.
 * <p>
 * This classification allows the compiler to distinguish between fixed language elements and user-defined entities.
 * It is used primarily within the {@link SymbolData} class to track the nature of each lexeme.
 * </p>
 */
public enum SymbolCategory {

    /**
     * Represents a symbol that has been identified as an identifier but has not yet
     * been assigned a specific context or is being used before declaration.
     * <p>
     * This is the default state for any new user-defined identifier found in the source code.
     * </p>
     */
    UNDEFINED,

    /**
     * Represents a symbol that has been explicitly declared or identified as a variable.
     * <p>
     * This category implies the symbol is a user-defined identifier found in a context that denotes it holds a value.
     * </p>
     */
    VARIABLE,

    /**
     * Represents a reserved word or a fixed terminal symbol of the language.
     * <p>
     * This includes:
     * <ul>
     * <li>Control flow words.</li>
     * <li>Type definitions .</li>
     * <li>Operators and punctuation.</li>
     * </ul>
     * Symbols in this category cannot be redefined as variables.
     * </p>
     */
    KEYWORD
}