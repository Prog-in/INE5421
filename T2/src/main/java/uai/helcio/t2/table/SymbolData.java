package uai.helcio.t2.table;

import uai.helcio.t2.entities.Token;

import java.util.LinkedList;

public class SymbolData {
    private SymbolCategory category;
    private String type;
    private final LinkedList<Token> occurrences;

    public SymbolData(SymbolCategory category) {
        this.category = category;
        this.type = "-";
        this.occurrences = new LinkedList<>();
    }

    public void addOccurrence(Token t) {
        this.occurrences.add(t);
    }

    public Token getFirstOccurrence() {
        return occurrences.isEmpty() ? null : occurrences.getFirst();
    }

    public void setAsVariable(String type) {
        this.category = SymbolCategory.VARIABLE;
        this.type = type;
    }

    @Override
    public String toString() {
        if (category == SymbolCategory.KEYWORD) {
            return String.format("[KEYWORD] -> %d refs", occurrences.size());
        }
        return String.format("[%s] %s -> %d refs", category, type, occurrences.size());
    }
}