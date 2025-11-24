package uai.helcio.t2;

import org.slf4j.event.Level;
import picocli.CommandLine;
import uai.helcio.t2.converters.FileToCFG;
import uai.helcio.t2.entities.CFG;
import uai.helcio.t2.entities.Symbol;
import uai.helcio.t2.entities.Token;
import uai.helcio.t2.generators.SLRGenerator;
import uai.helcio.t2.table.SLRParser;
import uai.helcio.t2.table.SymbolTable;
import uai.helcio.t2.table.TableEntry;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * The main entry point for the SLR Syntactic Analyzer Generator.
 */
@CommandLine.Command(
        name = "T2",
        mixinStandardHelpOptions = true,
        description = "Generates SLR Analyzer and parses input."
)
public class App implements Callable<Integer> {

    /**
     * CLI option to set the logging verbosity level.
     * Default is INFO. Use DEBUG or TRACE to see the parser stack changes and table details.
     */
    @CommandLine.Option(names = {"-l", "--log-level"}, defaultValue = "INFO")
    private Level logLevel;

    @CommandLine.Parameters(index = "0")
    private Path grammarFile;

    @CommandLine.Parameters(index = "1")
    private Path reservedWordsFile;

    @CommandLine.Parameters(index = "2")
    private Path inputFile;

    /**
     * Executes the main logic of the application.
     * <p>
     * This method is invoked by the Picocli framework after parsing the command line arguments.
     * It follows the sequence:
     * <ul>
     * <li><b>Step 1:</b> Parse Grammar File -> Convert to {@link CFG}.</li>
     * <li><b>Step 2:</b> Generate Parsing Table using {@link SLRGenerator}.</li>
     * <li><b>Step 3:</b> Initialize {@link SymbolTable} with reserved words.</li>
     * <li><b>Step 4:</b> Read Input File -> Tokenize -> Update Symbol Table.</li>
     * <li><b>Step 5:</b> Run {@link SLRParser} to validate the token stream.</li>
     * </ul>
     * </p>
     *
     * @return Exit code
     */
    @Override
    public Integer call() {
        AppLogger.setLoggingLevel(logLevel);

        try {
            AppLogger.logger.info("--- Fase de Projeto: Gerando Tabela SLR ---");
            List<String> grammarFileLines = ResourcesUtils.fileLinesToList(grammarFile);

            AppLogger.logger.info("--- Fase de Execução: Preparando Tabela de Símbolos ---");
            List<String> reservedWords = ResourcesUtils.fileLinesToList(reservedWordsFile);
            Parser parser = new Parser(grammarFileLines, reservedWords);

            // Lexical Analysis & Semantic Scans
            List<Token> tokens = parser.populateSymbolTable(ResourcesUtils.fileLinesToList(inputFile));

            AppLogger.logger.info("Tokens identificados: {}", tokens);
            AppLogger.logger.info("Estado da Tabela de Símbolos (Pós-Varredura):");
            AppLogger.logger.info(parser.getSymbolTable().toString());

            // 4. Parsing Execution
            AppLogger.logger.info("--- Iniciando Análise Sintática ---");
            boolean result = parser.parse(tokens);

            return result ? 0 : 1;

        } catch (Exception e) {
            AppLogger.logger.error("Erro fatal na aplicação", e);
            return 1;
        }
    }

    /**
     * Application Entry Point.
     *
     * @param args Command line arguments passed to Picocli.
     */
    static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}