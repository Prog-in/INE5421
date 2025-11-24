package uai.helcio.t2;

import org.slf4j.event.Level;
import picocli.CommandLine;
import uai.helcio.t2.converters.FileToCFG;
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

@CommandLine.Command(
        name = "T2",
        mixinStandardHelpOptions = true,
        description = "Generates SLR Analyzer and parses input."
)
public class App implements Callable<Integer> {

    @CommandLine.Option(names = {"-l", "--log-level"}, defaultValue = "INFO")
    private Level logLevel;

    @CommandLine.Parameters(index = "0") private Path grammarFile;
    @CommandLine.Parameters(index = "1") private Path reservedWordsFile;
    @CommandLine.Parameters(index = "2") private Path inputFile;

    @Override
    public Integer call() {
        AppLogger.setLoggingLevel(logLevel);

        try {
            var lines = ResourcesUtils.readFileLines(grammarFile, false);
            var cfg = FileToCFG.convert(lines.toList());

            AppLogger.logger.info("--- Fase de Projeto: Gerando Tabela SLR ---");
            Map<Integer, Map<Symbol, TableEntry>> parsingTable = new SLRGenerator(cfg).generate();

            AppLogger.logger.info("--- Fase de Execução: Preparando Tabela de Símbolos ---");
            List<String> reservedWords = ResourcesUtils.readFileLines(reservedWordsFile, false).toList();
            SymbolTable symbolTable = new SymbolTable(reservedWords);

            List<Token> tokens = new ArrayList<>();
            final boolean[] contextVar = {false};

            try (Stream<String> inputLines = ResourcesUtils.readFileLines(inputFile, false)) {
                for (String line : inputLines.toList()) {
                    // Tokenizador simples por espaço
                    String[] lexemes = line.trim().split("\\s+");

                    for (String lexeme : lexemes) {
                        if (lexeme.isEmpty()) continue;

                        Token t = symbolTable.getOrAdd(lexeme);
                        tokens.add(t);

                        if (lexeme.equals("var")) {
                            contextVar[0] = true;
                        } else if (lexeme.equals("inicio") || lexeme.equals("begin") || lexeme.equals("const")) {
                            contextVar[0] = false;
                        }

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

            // 4. Parsing
            AppLogger.logger.info("--- Iniciando Análise Sintática ---");
            SLRParser parser = new SLRParser(parsingTable);
            boolean result = parser.parse(tokens);

            return result ? 0 : 1;

        } catch (Exception e) {
            AppLogger.logger.error("Erro fatal na aplicação", e);
            return 1;
        }
    }

    static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}