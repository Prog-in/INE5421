package uai.helcio;

import org.slf4j.event.Level;
import uai.helcio.converters.ExtendedToPureRegexConverter;
import uai.helcio.converters.RegexToTreeConverter;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.ResourcesUtils;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) {
        AppLogger.setLoggingLevel(Level.valueOf(args[1]));
        try (Stream<String> lines = ResourcesUtils.readFileLines(args[0], StandardCharsets.US_ASCII)) {
            // name: regex
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
    }
}
