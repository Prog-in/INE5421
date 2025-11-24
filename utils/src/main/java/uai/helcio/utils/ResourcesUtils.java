package uai.helcio.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ResourcesUtils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.US_ASCII;

    public static Stream<String> readFileLines(Path fileName, boolean parallel) throws IOException {
        return readFileLines(fileName, DEFAULT_CHARSET, parallel);
    }

    public static Stream<String> readFileLines(Path fileName, Charset charset, boolean parallel) throws IOException {
        Stream<String> lines = Files.lines(fileName, charset);
        return parallel ? lines.parallel() : lines;
    }

    public static void writeToFile(Path fileName, List<String> lines) {
        writeToFile(fileName, DEFAULT_CHARSET, lines);
    }

    public static void writeToFile(Path fileName, Charset charset, List<String> lines) {
        try (BufferedWriter writer = Files.newBufferedWriter(fileName, charset)) {
            for (String line : lines) {
                if (!line.isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            AppLogger.logger.error("Error while writing to file {}", fileName);
        }
    }
}
