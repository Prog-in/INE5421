package uai.helcio.t2.table;

import uai.helcio.t1.entities.Token;

import java.util.LinkedList;

/**
 * Encapsulates the metadata and usage history associated with a specific entry in the Symbol Table.
 * <p>
 * This class implements the <b>Cross-Reference Table</b> concept commonly found in compilers.
 * Instead of storing just the symbol name, it stores:
 * <ul>
 * <li>The semantic category.</li>
 * <li>The data type, if applicable.</li>
 * <li>A linked list of all token instances occurring in the source code.</li>
 * </ul>
 * </p>
 */
public class SymbolData {

    /**
     * The semantic category of the symbol.
     */
    private SymbolCategory category;

    /**
     * The data type associated with the symbol.
     * Defaults to "-" for keywords or undefined symbols.
     */
    private String type;

    /**
     * A list of all occurrences of this symbol in the source code.
     * This allows for error reporting  and usage analysis.
     */
    private final LinkedList<Token> occurrences;

    /**
     * Constructs a new metadata container with an initial category.
     *
     * @param category The initial {@link SymbolCategory} of the symbol.
     */
    public SymbolData(SymbolCategory category) {
        this.category = category;
        this.type = "-";
        this.occurrences = new LinkedList<>();
    }

    /**
     * Registers a new occurrence of this symbol found in the source code.
     *
     * @param t The token instance representing the new occurrence.
     */
    public void addOccurrence(Token t) {
        this.occurrences.add(t);
    }

    /**
     * Retrieves the first token recorded for this symbol.
     * <p>
     * This is typically used to access the canonical "Address" or ID assigned
     * to the identifier when it was first encountered.
     * </p>
     *
     * @return The first {@link Token} added, or {@code null} if no occurrences exist.
     */
    public Token getFirstOccurrence() {
        return occurrences.isEmpty() ? null : occurrences.getFirst();
    }

    /**
     * Updates the metadata to mark this symbol as a Variable with a specific type.
     * <p>
     * This method is usually called during semantic analysis when a declaration is parsed.
     * </p>
     *
     * @param type The data type string.
     */
    public void setAsVariable(String type) {
        this.category = SymbolCategory.VARIABLE;
        this.type = type;
    }

    /**
     * Returns a formatted string summarizing the symbol's metadata and usage count.
     * <p>
     * Format examples:
     * <ul>
     * <li><code>[KEYWORD] -> 5 refs</code></li>
     * <li><code>[VARIABLE] integer -> 3 refs</code></li>
     * </ul>
     * </p>
     *
     * @return A string representation suitable for symbol table dumps.
     */
    @Override
    public String toString() {
        if (category == SymbolCategory.KEYWORD) {
            return String.format("[KEYWORD] -> %d refs", occurrences.size());
        }
        return String.format("[%s] %s -> %d refs", category, type, occurrences.size());
    }
}