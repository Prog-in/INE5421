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
            // Read and Convert Grammar
            var lines = ResourcesUtils.readFileLines(grammarFile, false);
            var cfg = FileToCFG.convert(lines.toList());

            AppLogger.logger.info("--- Fase de Projeto: Gerando Tabela SLR ---");
            // Generate the SLR Action/Goto Table
            Map<Integer, Map<Symbol, TableEntry>> parsingTable = new SLRGenerator(cfg).generate();

            // Prepare Symbol Table
            AppLogger.logger.info("--- Fase de Execução: Preparando Tabela de Símbolos ---");
            List<String> reservedWords = ResourcesUtils.readFileLines(reservedWordsFile, false).toList();
            SymbolTable symbolTable = new SymbolTable(reservedWords);

            // Lexical Analysis & Semantic Scans
            List<Token> tokens = new ArrayList<>();
            final boolean[] contextVar = {false};

            try (Stream<String> inputLines = ResourcesUtils.readFileLines(inputFile, false)) {
                for (String line : inputLines.toList()) {
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
            }

            AppLogger.logger.info("Tokens identificados: {}", tokens);
            AppLogger.logger.info("Estado da Tabela de Símbolos (Pós-Varredura):");
            AppLogger.logger.info(symbolTable.toString());

            // 4. Parsing Execution
            AppLogger.logger.info("--- Iniciando Análise Sintática ---");
            SLRParser parser = new SLRParser(parsingTable);
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