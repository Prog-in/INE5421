package uai.helcio.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ResourcesUtils {
    public static Stream<String> readFileLines(Path fileName, boolean parallel) throws IOException {
        return readFileLines(fileName, StandardCharsets.US_ASCII, parallel);
    }

    public static Stream<String> readFileLines(Path fileName, Charset charset, boolean parallel) throws IOException {
        Stream<String> lines = Files.lines(fileName, charset);
        return parallel ? lines.parallel() : lines;
    }
}
