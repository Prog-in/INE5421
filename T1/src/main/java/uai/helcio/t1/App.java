package uai.helcio.t1;

import org.slf4j.event.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import uai.helcio.t1.entities.Token;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

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

    @Parameters(
            index = "2",
            paramLabel = "OUTPUT_FILE",
            description = "Path to the output file containing the tokenized input."
    )
    private Path outputFile;

    @Override
    public Integer call() {
        if (!validateInput()) {
            return CommandLine.ExitCode.USAGE;
        }
        try {
            AppLogger.setLoggingLevel(logLevel);

            List<String> regexes = ResourcesUtils.fileLinesToList(regexFile);
            List<String> source = ResourcesUtils.fileLinesToList(sourceFile);
            Tokenizer req = new Tokenizer(regexes, source, parallel);
            List<String> tokens = req.tokenize().stream().map(Token::toString).toList();
            ResourcesUtils.writeToFile(outputFile, tokens);
        } catch (Exception e) {
            AppLogger.logger.error("An error occurred during the execution", e);
            return 1;
        }
        return 0;
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

    static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}