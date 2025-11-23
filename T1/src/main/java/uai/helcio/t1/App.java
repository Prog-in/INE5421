package uai.helcio.t1;

import org.slf4j.event.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import uai.helcio.t1.Automata.*;
import uai.helcio.t1.converters.ExtendedToPureRegexConverter;
import uai.helcio.t1.converters.NFAToDFAConverter;
import uai.helcio.t1.converters.RegexToTreeConverter;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Command(
        name = "app",
        mixinStandardHelpOptions = true,
        description = "Generates a lexical analyzer from a file containing regular definitions and processes an input file."
)
public class App implements Callable<Integer> {

    @Option(
            names = {"-l", "--log-level"},
            description = "Specifies the logging level. Possible values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}.",
            defaultValue = "TRACE"
    )
    private Level logLevel;

    @Option(
            names = {"-p", "--parallel"},
            description = "Enables parallel processing."
    )
    private boolean parallel;

    @Parameters(
            index = "0",
            paramLabel = "REGEX_FILE",
            description = "Path to the file containing regex definitions."
    )
    private Path regexFile;

    @Parameters(
            index = "1",
            paramLabel = "SOURCE_FILE",
            description = "Path to the file containing the strings to evaluate."
    )
    private Path sourceFile;

    @Override
    public Integer call() {
        if (!validateInput()) {
            return CommandLine.ExitCode.USAGE;
        }

        AppLogger.setLoggingLevel(logLevel);

        try {
            List<DFA> individualDFAs;


            try (Stream<String> lines = ResourcesUtils.readFileLines(regexFile, parallel)) {
                individualDFAs = lines
                        .peek(l -> AppLogger.logger.debug("Reading RegEX: {}", l))
                        .map(ExtendedToPureRegexConverter::convert)
                        .map(RegexToTreeConverter::convert)
                        .map(DFABuilder::build)
                        .peek(dfa -> dfa.logStructure("DFA built"))
                        .map(DFAMinimizer::minimize)
                        .peek(dfa -> dfa.logStructure("DFA minimized"))
                        .toList();
            }

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

            AppLogger.logger.info(">>> STARTING LEXICAL ANALYSIS FROM SOURCE FILE <<<");

            try (Stream<String> inputs = ResourcesUtils.readFileLines(sourceFile, false)) {
                inputs.forEach(line -> processInputLine(line, minimizedLexicalAnalyzer));
            }

        } catch (Exception e) {
            AppLogger.logger.error("An error occurred during the execution", e);
            return 1;
        }
        return 0;
    }

    private void processInputLine(String input, DFA lexer) {
        if (input.trim().isEmpty()) return;

        int currentPos = 0;

        while (currentPos < input.length()) {
            DFA.TokenResult result = lexer.nextToken(input, currentPos);
            if (result != null) {
                // avoid printing white space
                if (!result.tokenName().equals("ws")) {
                    AppLogger.peekInfo(String.format("<%s, %s>", result.lexeme(), result.tokenName()));
                }
                currentPos = result.endPosition();
            } else {
                String invalidChar = String.valueOf(input.charAt(currentPos));
                if (!invalidChar.trim().isEmpty()) {
                    AppLogger.peekError(String.format("<%s, ERROR>", invalidChar));
                }
                currentPos++;
            }
        }
    }


    private boolean validateInput() {
        if (!Files.exists(regexFile)) {
            AppLogger.peekError("Arquivo de regras inexistente: " + regexFile);
            return false;
        }
        if (!Files.exists(sourceFile)) {
            AppLogger.peekError("Arquivo fonte inexistente: " + sourceFile);
            return false;
        }
        return Arrays.stream(Level.values())
                .anyMatch(Predicate.isEqual(logLevel));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}