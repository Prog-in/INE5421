package uai.helcio.t2;

import uai.helcio.t2.converters.FileToCFG;
import uai.helcio.t2.entities.CFG;
import uai.helcio.t2.entities.Symbol;
import uai.helcio.t2.entities.Token;
import uai.helcio.t2.generators.SLRGenerator;
import uai.helcio.t2.table.SLRParser;
import uai.helcio.t2.table.SymbolTable;
import uai.helcio.t2.table.TableEntry;

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

    public List<Token> populateSymbolTable(List<String> sourceFile) {
        List<Token> tokens = new ArrayList<>();
        final boolean[] contextVar = {false};
        for (String line : sourceFile) {
            // Simple tokenizer splitting by whitespace
            String[] lexemes = line.trim().split("\\s+");

            for (String lexeme : lexemes) {
                if (lexeme.isEmpty()) continue;

                // Get token from table
                Token t = symbolTable.getOrAdd(lexeme);
                tokens.add(t);

                if (lexeme.equals("var")) {
                    contextVar[0] = true;
                } else if (lexeme.equals("inicio") || lexeme.equals("begin") || lexeme.equals("const")) {
                    contextVar[0] = false;
                }

                // Mark identifiers as VARIABLES if inside a 'var' block and not a reserved word
                if (contextVar[0] && !reservedWords.contains(lexeme) && Character.isLetter(lexeme.charAt(0))) {
                    if (!lexeme.equals("inteiro") && !lexeme.equals("real") &&
                            !lexeme.equals("vetor") && !lexeme.equals("of")) {
                        symbolTable.declareVariable(lexeme, "var_declarada");
                    }
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
