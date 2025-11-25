package uai.helcio.t1;

import uai.helcio.t1.Automata.*;
import uai.helcio.t1.converters.ExtendedToPureRegexConverter;
import uai.helcio.t1.converters.NFAToDFAConverter;
import uai.helcio.t1.converters.RegexToTreeConverter;
import uai.helcio.t1.entities.Token;
import uai.helcio.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Tokenizer {
    private final List<String> regexes;
    private final List<String> source;
    private final boolean parallel;

    public Tokenizer(List<String> regexes, List<String> source, boolean parallel) {
        this.regexes = regexes;
        this.source = source;
        this.parallel = parallel;
    }

    public List<Token> tokenize() {
        List<DFA> individualDFAs;

        Stream<String> regexStream = regexes.stream();
        if (parallel) {
            regexStream = regexStream.parallel();
        }
        individualDFAs = regexStream
                .peek(l -> AppLogger.logger.debug("Reading RegEX: {}", l))
                .map(ExtendedToPureRegexConverter::convert)
                .map(RegexToTreeConverter::convert)
                .map(DFABuilder::build)
                .peek(dfa -> dfa.logStructure("DFA built"))
                .map(DFAMinimizer::minimize)
                .peek(dfa -> dfa.logStructure("DFA minimized"))
                .peek(dfa -> AppLogger.logToFile("regular_definitions_dfas.txt", "T1", dfa.toString()))
                .toList();

        // the priority order is made by setting the first rule as priority 0
        List<String> priorityOrder = individualDFAs.stream()
                .map(DFA::getTokenName)
                .toList();

        NFA unitedNFA = NFAUnionBuilder.union(individualDFAs);
        unitedNFA.logStructure("Unifying DFAs");

        // Convert NFA to DFA and uses the priority order to solve conflicts
        DFA lexicalAnalyzer = NFAToDFAConverter.convert(unitedNFA, priorityOrder);
        // minimizes it
        DFA minimizedLexicalAnalyzer = DFAMinimizer.minimize(lexicalAnalyzer);
        lexicalAnalyzer.logStructure("Final determinization");

        AppLogger.logger.info("Lexical analyser built! ({} states)",
                minimizedLexicalAnalyzer.getTransitionTable().size());

        AppLogger.logToFile("final_dfa.txt", "T1", minimizedLexicalAnalyzer.toTableAsString());

        AppLogger.logger.info(">>> STARTING LEXICAL ANALYSIS FROM SOURCE FILE <<<");

        List<Token> lines = new ArrayList<>();
        source.forEach(line -> lines.addAll(processInputLine(line, minimizedLexicalAnalyzer)));
        return lines;
    }

    private List<Token> processInputLine(String input, DFA lexer) {
        List<Token> lineTokens = new ArrayList<>();
        if (input.trim().isEmpty()) return lineTokens;

        int currentPos = 0;

        while (currentPos < input.length()) {
            DFA.TokenResult result = lexer.nextToken(input, currentPos);
            if (result != null) {
                // avoid printing white space
                if (!result.tokenName().equals("ws")) {
                    Token token = new Token(result.lexeme(), result.tokenName());
                    AppLogger.peekDebug(token);
                    lineTokens.add(token);
                }
                currentPos = result.endPosition();
            } else {
                String invalidChar = String.valueOf(input.charAt(currentPos));
                if (!invalidChar.trim().isEmpty()) {
                    Token token = new Token(invalidChar, "ERROR");
                    AppLogger.peekDebug(token);
                    lineTokens.add(token);
                }
                currentPos++;
            }
        }
        return lineTokens;
    }
}
