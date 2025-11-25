package uai.helcio.t2;

import org.junit.jupiter.api.Test;
import uai.helcio.t1.entities.Token;
import uai.helcio.utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    private static String TEST_DIR_PREFIX = "test";
    private static String SOURCE_FILE = "input.txt";
    private static String GRAMMAR_FILE = "grammar.txt";
    private static String RESERVED_KW_FILE = "reserved.txt";
    private static String EXPECTED_OUTPUT_FILE = "expected-output.txt";

    @Test
    public void test() {
        for (int i = 1; i < 3; i++) {
            Quartet<List<String>, List<String>, List<String>, List<String>> testFiles = readTestFiles(i);
            boolean expectedOutput = Boolean.parseBoolean(testFiles.fourth.getFirst());

            Parser parser = new Parser(testFiles.second, testFiles.third);
            List<Token> tokens = parser.populateSymbolTable(testFiles.first);
            boolean output = parser.parse(tokens);
            assertEquals(expectedOutput, output);
        }
    }

    private Quartet<List<String>, List<String>, List<String>, List<String>> readTestFiles(int testIndex) {
        ClassLoader classLoader = AppTest.class.getClassLoader();
        List<String> sourceFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, SOURCE_FILE);
        List<String> grammarFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, GRAMMAR_FILE);
        List<String> reservedFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, RESERVED_KW_FILE);
        List<String> expectedOutputFile = TestUtils.getFileContent(classLoader, TEST_DIR_PREFIX, testIndex, EXPECTED_OUTPUT_FILE);
        return new Quartet<>(sourceFile, grammarFile, reservedFile, expectedOutputFile);
    }

    public record Quartet<A, B, C, D>(A first, B second, C third, D fourth) {}
}
