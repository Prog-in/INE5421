package uai.helcio;

import org.slf4j.event.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import uai.helcio.converters.ExtendedToPureRegexConverter;
import uai.helcio.converters.RegexToTreeConverter;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Command(
        name = "app",
        mixinStandardHelpOptions = true,
        description = "Generates a lexical analyzer from a file containing regular definitions"
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
            paramLabel = "INPUT_FILE",
            description = "Path to the input file containing regular definitions."
    )
    private Path inputFile;

    @Override
    public Integer call() {
        if (!validateInput()) {
            return CommandLine.ExitCode.USAGE;
        }

        AppLogger.setLoggingLevel(logLevel);
        try (Stream<String> lines = ResourcesUtils.readFileLines(inputFile, parallel)) {
            var a = lines
                    .peek(AppLogger::peekDebug)
                    .map(ExtendedToPureRegexConverter::convert)
                    .peek(AppLogger::peekDebug)
                    .map(RegexToTreeConverter::convert)
                    .peek(AppLogger::peekDebug)
                    .toList();
            AppLogger.logger.trace(a.toString());
        } catch (Exception e) {
            AppLogger.logger.error("Ocorreu um erro durante a geração do analisador léxico", e);
        }
        return 0;
    }

    private boolean validateInput() {
        if (!Files.exists(inputFile)) {
            AppLogger.logger.error("Arquivo de entrada inexistente");
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
