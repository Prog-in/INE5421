package uai.helcio;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import uai.helcio.t1.Tokenizer;
import uai.helcio.utils.AppLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    private static String TEST_DIR_PREFIX = "test";
    private static String SOURCE_FILE = "input.txt";
    private static String REGEX_FILE = "regexes.txt";
    private static String EXPECTED_OUTPUT_FILE = "expected-output.txt";

    @Test
    public void test() {
        for (int i = 0; i < 2; i++) {
            Triple<Stream<String>, Stream<String>, Stream<String>> testFiles = readTestFiles(i);
            List<String> expectedOutput = getExpectedOutput(testFiles.getRight());

            Tokenizer tokenizer = new Tokenizer(testFiles.getMiddle(), testFiles.getLeft());
            List<String> tokens = tokenizer.tokenize();
            assertEquals(expectedOutput, tokens);
        }
    }

    private List<String> getExpectedOutput(Stream<String> expectedOutputStream) {
        return expectedOutputStream.toList();
    }

    private Triple<Stream<String>, Stream<String>, Stream<String>> readTestFiles(int testIndex) {
        Stream<String> sourceFile = getFileStream(SOURCE_FILE, testIndex);
        Stream<String> regexFile = getFileStream(REGEX_FILE, testIndex);
        Stream<String> expectedOutputFile = getFileStream(EXPECTED_OUTPUT_FILE, testIndex);
        return Triple.of(sourceFile, regexFile, expectedOutputFile);
    }

    private Stream<String> getFileStream(String fileName, int testIndex) {
        InputStream fileInputStream = getFileInputStream(fileName, testIndex);
        return inputStreamToStream(fileInputStream);
    }

    private Stream<String> inputStreamToStream(InputStream inputStream) {
        if (inputStream == null) {
            return Stream.empty();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().toList().stream();
        } catch (Exception e) {
            AppLogger.logger.error("Error while reading test file", e);
            return Stream.empty();
        }
    }

    private InputStream getFileInputStream(String fileName, int testIndex) {
        return AppTest.class.getClassLoader().getResourceAsStream(Path.of(TEST_DIR_PREFIX + testIndex,  fileName).toString());
    }
}
