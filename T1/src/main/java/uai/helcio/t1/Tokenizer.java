package uai.helcio.t1;

import uai.helcio.t1.Automata.*;
import uai.helcio.t1.converters.ExtendedToPureRegexConverter;
import uai.helcio.t1.converters.NFAToDFAConverter;
import uai.helcio.t1.converters.RegexToTreeConverter;
import uai.helcio.utils.AppLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Tokenizer {
    private final Stream<String> regexStream;
    private final Stream<String> sourceStream;

    public Tokenizer(Stream<String> regexStream, Stream<String> sourceStream) {
        this.regexStream = regexStream;
        this.sourceStream = sourceStream;
    }

    public List<String> tokenize() {
        List<DFA> individualDFAs;

        individualDFAs = regexStream
                .peek(l -> AppLogger.logger.debug("Reading RegEX: {}", l))
                .map(ExtendedToPureRegexConverter::convert)
                .map(RegexToTreeConverter::convert)
                .map(DFABuilder::build)
                .peek(dfa -> dfa.logStructure("DFA built"))
                .map(DFAMinimizer::minimize)
                .peek(dfa -> dfa.logStructure("DFA minimized"))
                .toList();

        // write each DFA to a file
        individualDFAs.forEach(dfa -> AppLogger.logToFile("regular_definitions_dfas.txt", "T1", dfa.toString()));

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

        List<String> lines = new ArrayList<>();
        sourceStream.forEach(line -> lines.addAll(processInputLine(line, minimizedLexicalAnalyzer)));
        return lines;
    }

    private List<String> processInputLine(String input, DFA lexer) {
        List<String> bla = new ArrayList<>();
        if (input.trim().isEmpty()) return bla;

        int currentPos = 0;

        while (currentPos < input.length()) {
            DFA.TokenResult result = lexer.nextToken(input, currentPos);
            if (result != null) {
                // avoid printing white space
                if (!result.tokenName().equals("ws")) {
                    String token = String.format("<%s, %s>", result.lexeme(), result.tokenName());
                    AppLogger.peekDebug(token);
                    bla.add(token);
                }
                currentPos = result.endPosition();
            } else {
                String invalidChar = String.valueOf(input.charAt(currentPos));
                if (!invalidChar.trim().isEmpty()) {
                    String token = String.format("<%s, ERROR>", invalidChar);
                    AppLogger.peekDebug(token);
                    bla.add(token);
                }
                currentPos++;
            }
        }
        return bla;
    }
}
