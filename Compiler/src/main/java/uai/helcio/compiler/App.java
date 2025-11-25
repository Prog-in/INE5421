package uai.helcio.compiler;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.event.Level;
import picocli.CommandLine;
import uai.helcio.t1.Tokenizer;
import uai.helcio.t2.Parser;
import uai.helcio.t1.entities.Token;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

public class App implements Callable<Integer> {

    @CommandLine.Option(names = {"-l", "--log-level"}, defaultValue = "INFO")
    private Level logLevel;

    @CommandLine.Parameters(index = "0")
    private Path grammarFile;

    @CommandLine.Parameters(index = "1")
    private Path regexFile;

    @CommandLine.Parameters(index = "2")
    private Path reservedWordsFile;

    @CommandLine.Parameters(index = "3")
    private Path inputFile;

    @Override
    public Integer call() {
        AppLogger.setLoggingLevel(logLevel);

        try {
            List<String> regexes = ResourcesUtils.fileLinesToList(regexFile);
            List<String> source = ResourcesUtils.fileLinesToList(inputFile);
            Tokenizer tokenizer = new Tokenizer(regexes, source, false);
            List<Token> tokens = tokenizer.tokenize();

            AppLogger.logger.info("--- Fase de Projeto: Gerando Tabela SLR ---");
            List<String> grammarFileLines = ResourcesUtils.fileLinesToList(grammarFile);

            List<String> reservedWords = ResourcesUtils.fileLinesToList(reservedWordsFile);
            Parser parser = new Parser(grammarFileLines, reservedWords);

            AppLogger.logger.info("--- Fase de Execução: Preparando Tabela de Símbolos ---");
            List<Token> tokens2 = parser.populateSymbolTable(tokens.stream().map(Token::toString).toList());

            AppLogger.logger.info("Tokens identificados: {}", tokens2);
            AppLogger.logger.info("Estado da Tabela de Símbolos (Pós-Varredura):");
            AppLogger.logger.info(parser.getSymbolTable().toString());

            // 4. Parsing Execution
            AppLogger.logger.info("--- Iniciando Análise Sintática ---");
            boolean result = parser.parse(tokens2);

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
