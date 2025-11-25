package uai.helcio.t2.table;

import uai.helcio.t1.entities.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the storage and retrieval of symbols during compilation.
 * <p>
 * The Symbol Table serves two main purposes in this architecture:
 * <ol>
 * <li><b>Lexical Identification:</b> It acts as a "memory" for the lexer. If a lexeme is seen again,
 * it ensures the same Token ID/Address is reused, while recording the new occurrence position.</li>
 * <li><b>Semantic Storage:</b> It holds metadata about symbols, such as their category
 * and type information, allowing for semantic checks and context updates.</li>
 * </ol>
 * </p>
 *
 */
public class SymbolTable {

    /**
     * The internal storage mapping a lexeme string to its rich metadata container.
     */
    private final Map<String, SymbolData> table = new HashMap<>();

    /**
     * A counter used to generate unique numeric addresses for user-defined identifiers.
     * Starts at 10 to avoid conflict with reserved low-integer codes.
     */
    private int idCounter = 10;

    /**
     * A predefined set of static terminals
     * <p>
     * These symbols are hardcoded here to ensure they are always treated as {@link SymbolCategory#KEYWORD},
     * preventing the parser from interpreting punctuation as user variables even if the
     * external reserved words file is incomplete.
     * </p>
     */
    private static final Set<String> STATIC_SYMBOLS = Set.of(
            ".", "=", ";", ":", "(", ")", ",", "..", "[", "]",
            ":=", "<", ">", ">=", "<=", "<>", "+", "-", "*", "/"
    );

    /**
     * Initializes the Symbol Table.
     * <p>
     * Populates the table with:
     * 1. Dynamic reserved words provided via the external file.
     * 2. Static operators and punctuation defined in {@link #STATIC_SYMBOLS}.
     * </p>
     *
     * @param reservedWords A list of reserved words loaded from configuration.
     */
    public SymbolTable(List<String> reservedWords) {
        for (String rw : reservedWords) addKeyword(rw);
        for (String sym : STATIC_SYMBOLS) addKeyword(sym);
    }

    /**
     * Internal helper to register a keyword or operator.
     * <p>
     * If the lexeme is not already in the table, it creates a new entry marked as {@link SymbolCategory#KEYWORD}.
     * </p>
     *
     * @param lexeme The string representation of the keyword.
     */
    private void addKeyword(String lexeme) {
        if (!table.containsKey(lexeme)) {
            SymbolData data = new SymbolData(SymbolCategory.KEYWORD);
            // For keywords, the "type" of the token is the lexeme itself
            data.addOccurrence(new Token(lexeme, "PR"));
            table.put(lexeme, data);
        }
    }

    /**
     * The core lookup method used by the Lexer/Parser.
     * <p>
     *  If the lexeme exists, it retrieves the original token definition, creates a new occurrence for the current
     *  position, and adds it to the cross-reference list.
     * <br>
     * If the lexeme is new, it generates a new unique address ID, marks it as {@link SymbolCategory#UNDEFINED},
     * and registers it.
     * </p>
     *
     * @param lexeme The string found in the source code.
     * @return A {@link Token} object ready to be consumed by the parser.
     */
    public Token getOrAdd(String lexeme) {
        // Check if lexeme exists
        if (table.containsKey(lexeme)) {
            SymbolData data = table.get(lexeme);
            Token reference = data.getFirstOccurrence();

            // Create a new token instance reusing the type/attribute of the reference
            Token newOccurrence = new Token(reference.type(), reference.attribute());
            data.addOccurrence(newOccurrence);
            return newOccurrence;
        }

        // New User-Defined Identifier
        Token newToken = new Token("id", String.valueOf(idCounter++));
        SymbolData newData = new SymbolData(SymbolCategory.UNDEFINED);
        newData.addOccurrence(newToken);
        table.put(lexeme, newData);
        return newToken;
    }

    /**
     * Promotes a symbol to a Variable with a specific type.
     * <p>
     * This method is typically called by the semantic analyzer when a declaration pattern
     * (e.g., <code>var x : int</code>) is recognized. It updates the {@link SymbolData} metadata.
     * </p>
     *
     * @param lexeme The identifier name.
     * @param type   The data type.
     */
    public void declareVariable(String lexeme, String type) {
        if (table.containsKey(lexeme)) {
            table.get(lexeme).setAsVariable(type);
        }
    }

    /**
     * Generates a formatted table view of all symbols.
     * <p>
     * Displays the Lexeme, the Token ID/Address, the Semantic Category, Type, and Reference Count.
     * Useful for debugging the final state of compilation.
     * </p>
     *
     * @return A formatted string of the table contents.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n================ TABELA DE SÍMBOLOS ================\n");
        sb.append(String.format("%-15s | %-10s | %s\n", "LEXEMA", "TOKEN ID", "METADADOS"));
        sb.append("----------------+------------+-----------------------------\n");

        table.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String lexeme = entry.getKey();
                    SymbolData data = entry.getValue();
                    Token token = data.getFirstOccurrence();
                    sb.append(String.format("%-15s | %-10s | %s\n",
                            lexeme, token.toString(), data));
                });
        sb.append("===========================================================\n");
        return sb.toString();
    }
}