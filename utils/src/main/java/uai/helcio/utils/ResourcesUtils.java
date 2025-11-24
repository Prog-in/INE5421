package uai.helcio.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

public class ResourcesUtils {
    public static List<String> fileLinesToList(Path filePath) throws IOException {
        try (Stream<String> lines = readFileLines(filePath, false)) {
            return lines.toList();
        }
    }
    public static Stream<String> readFileLines(Path fileName, boolean parallel) throws IOException {
        return readFileLines(fileName, StandardCharsets.US_ASCII, parallel);
    }

    public static Stream<String> readFileLines(Path fileName, Charset charset, boolean parallel) throws IOException {
        Stream<String> lines = Files.lines(fileName, charset);
        return parallel ? lines.parallel() : lines;
    }

    public static void writeToFile(Path fileName, List<String> lines) {
        writeToFile(fileName, StandardCharsets.UTF_8, lines);
    }

    public static void writeToFile(Path fileName, Charset charset, List<String> lines) {
        try {
            Path parent = fileName.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(fileName, charset,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                for (String line : lines) {
                    if (!line.isEmpty()) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            AppLogger.logger.error("Error while writing to file {}", fileName);
        }
    }
}
