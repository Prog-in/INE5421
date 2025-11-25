package uai.helcio.t2;

import org.apache.commons.lang3.tuple.Pair;
import uai.helcio.t2.converters.FileToCFG;
import uai.helcio.t2.entities.CFG;
import uai.helcio.t2.entities.Symbol;
import uai.helcio.t1.entities.Token;
import uai.helcio.t2.generators.SLRGenerator;
import uai.helcio.t2.table.SLRParser;
import uai.helcio.t2.table.SymbolTable;
import uai.helcio.t2.table.TableEntry;
import uai.helcio.utils.FileParsingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Map<Integer, Map<Symbol, TableEntry>> parsingTable;
    private final List<String> reservedWords;
    private final SymbolTable symbolTable;

    public Parser(List<String> grammarLines, List<String> reservedWords) {
        this.parsingTable = generateParsingTable(grammarLines);
        this.reservedWords = reservedWords;
        this.symbolTable = new SymbolTable(reservedWords);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Map<Integer, Map<Symbol, TableEntry>> generateParsingTable(List<String> grammarLines) {
        // Read and Convert Grammar
        CFG cfg = FileToCFG.convert(grammarLines);
        // Generate the SLR Action/Goto Table
        return new SLRGenerator(cfg).generate();
    }

    public List<Token> populateSymbolTable(List<String> tokensReprs) {
        List<Token> tokens = new ArrayList<>();
        for (String tokenStr : tokensReprs) {
            for (int i = 0; i < tokenStr.length(); i++) {
                i++; // jump '<'
                // token is ','
                String lexeme;
                if (tokenStr.charAt(i) == ',' && tokenStr.charAt(i + 1) == ',') {
                    lexeme = ",";
                } else {
                    lexeme = FileParsingUtils.captureUntil(tokenStr, i, ',', false).getLeft();
                }
                if (!lexeme.isEmpty()) {
                    // Get token from table
                    Token t = symbolTable.getOrAdd(lexeme);
                    tokens.add(t);
                    break;
                }
            }
        }
        return tokens;
    }

    public boolean parse(List<Token> tokens) {
        SLRParser parser = new SLRParser(parsingTable);
        return parser.parse(tokens);
    }
}
