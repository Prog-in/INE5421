package uai.helcio.t2.table;

import uai.helcio.t2.entities.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SymbolTable {
    private final Map<String, SymbolData> table = new HashMap<>();
    private int idCounter = 10;

    private static final Set<String> STATIC_SYMBOLS = Set.of(
            ".", "=", ";", ":", "(", ")", ",", "..", "[", "]",
            ":=", "<", ">", ">=", "<=", "<>", "+", "-", "*", "/"
    );

    public SymbolTable(List<String> reservedWords) {
        for (String rw : reservedWords) addKeyword(rw);
        for (String sym : STATIC_SYMBOLS) addKeyword(sym);
    }

    private void addKeyword(String lexeme) {
        if (!table.containsKey(lexeme)) {
            SymbolData data = new SymbolData(SymbolCategory.KEYWORD);
            data.addOccurrence(new Token(lexeme, "PR"));
            table.put(lexeme, data);
        }
    }

    public Token getOrAdd(String lexeme) {
        if (table.containsKey(lexeme)) {
            SymbolData data = table.get(lexeme);
            Token reference = data.getFirstOccurrence();
            Token newOccurrence = new Token(reference.type(), reference.attribute());
            data.addOccurrence(newOccurrence);
            return newOccurrence;
        }

        Token newToken = new Token("id", String.valueOf(idCounter++));
        SymbolData newData = new SymbolData(SymbolCategory.UNDEFINED);
        newData.addOccurrence(newToken);
        table.put(lexeme, newData);
        return newToken;
    }

    public void declareVariable(String lexeme, String type) {
        if (table.containsKey(lexeme)) {
            table.get(lexeme).setAsVariable(type);
        }
    }

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