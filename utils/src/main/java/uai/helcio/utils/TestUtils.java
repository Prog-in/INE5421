package uai.helcio.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class TestUtils {
    public static List<String> getFileContent(ClassLoader classLoader, String dirPrefix, int testIndex, String fileName) {
        InputStream fileInputStream = getFileInputStream(classLoader, dirPrefix, testIndex, fileName);
        return readInputStream(fileInputStream);
    }

    public static List<String> readInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return Collections.emptyList();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().toList();
        } catch (Exception e) {
            AppLogger.logger.error("Error while reading test file", e);
            return Collections.emptyList();
        }
    }

    public static InputStream getFileInputStream(ClassLoader classLoader, String dirPrefix, int testIndex, String fileName) {
        return classLoader.getResourceAsStream(Path.of(dirPrefix + testIndex,  fileName).toString());
    }
}
