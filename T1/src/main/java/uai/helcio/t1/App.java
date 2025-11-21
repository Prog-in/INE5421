package uai.helcio.t1;

import org.slf4j.event.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import uai.helcio.t1.converters.ExtendedToPureRegexConverter;
import uai.helcio.t1.converters.RegexToTreeConverter;
import uai.helcio.t1.entities.DFA;
import uai.helcio.t1.utils.AppLogger;
import uai.helcio.t1.utils.ResourcesUtils;

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
            List<DFA> dfas;
            try (Stream<String> lines = ResourcesUtils.readFileLines(regexFile, parallel)) {
                dfas = lines
                        .peek(AppLogger::peekDebug)
                        .map(ExtendedToPureRegexConverter::convert)
                        .peek(AppLogger::peekDebug)
                        .map(RegexToTreeConverter::convert)
                        .map(DFA::new)
                        .peek(AppLogger::peekDebug)
                        .toList();
            }

            try (Stream<String> inputs = ResourcesUtils.readFileLines(sourceFile, false)) {
                inputs.forEach(line -> processInputLine(line, dfas));
            }

        } catch (Exception e) {
            AppLogger.logger.error("Ocorreu um erro durante a execução", e);
            return 1;
        }
        return 0;
    }

    private void processInputLine(String input, List<DFA> dfas) {
        if (input.trim().isEmpty()) return;

        dfas.stream()
                .filter(dfa -> dfa.accepts(input))
                .findFirst()
                .ifPresentOrElse(
                        dfa -> AppLogger.peekInfo(String.format("<%s, %s>", input, dfa.getTokenName())),
                        () -> AppLogger.peekError(String.format("<%s, ERROR>", input))
                );
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