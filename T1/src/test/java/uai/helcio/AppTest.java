package uai.helcio;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import uai.helcio.t1.Tokenizer;
import uai.helcio.utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    private static String TEST_DIR_PREFIX = "test";
    private static String SOURCE_FILE = "input.txt";
    private static String REGEX_FILE = "regexes.txt";
    private static String EXPECTED_OUTPUT_FILE = "expected-output.txt";

    @Test
    public void test() {
        for (int i = 0; i < 2; i++) {
            Triple<List<String>, List<String>, List<String>> testFiles = readTestFiles(i);
            List<String> expectedOutput = testFiles.getRight();

            Tokenizer tokenizer = new Tokenizer(testFiles.getMiddle(), testFiles.getLeft(), false);
            List<String> tokens = tokenizer.tokenize();
            assertEquals(expectedOutput, tokens);
        }
    }

    private Triple<List<String>, List<String>, List<String>> readTestFiles(int testIndex) {
        ClassLoader classLoader = AppTest.class.getClassLoader();
        List<String> sourceFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, SOURCE_FILE);
        List<String> regexFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, REGEX_FILE);
        List<String> expectedOutputFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, EXPECTED_OUTPUT_FILE);
        return Triple.of(sourceFile, regexFile, expectedOutputFile);
    }
}
