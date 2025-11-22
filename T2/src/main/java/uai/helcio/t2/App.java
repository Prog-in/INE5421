package uai.helcio.t2;

import org.slf4j.event.Level;
import picocli.CommandLine;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;
import uai.helcio.t2.converters.FileToCFG;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "T2",
        mixinStandardHelpOptions = true,
        description = "Generates a SLR syntactical analyzer from a file containing regular definitions"
)
public class App implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-l", "--log-level"},
            description = "Specifies the logging level. Possible values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}.",
            defaultValue = "TRACE"
    )
    private Level logLevel;

    @CommandLine.Option(
            names = {"-p", "--parallel"},
            description = "Enables parallel processing."
    )
    private boolean parallel;

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "GRAMMAR_FILE",
            description = "Path to the input file containing grammar rules."
    )
    private Path grammarFile;

    @Override
    public Integer call() {
        if (!validateInput()) {
            return CommandLine.ExitCode.USAGE;
        }

        AppLogger.setLoggingLevel(logLevel);
        try (Stream<String> lines = ResourcesUtils.readFileLines(grammarFile, parallel)) {
            var cfg = FileToCFG.convert(lines.toList());
            AppLogger.logger.trace(cfg.toString());
        } catch (Exception e) {
            AppLogger.logger.error("An error happened during the generation of the syntactical analyser", e);
        }
        return 0;
    }

    private boolean validateInput() {
        if (!Files.exists(grammarFile)) {
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
