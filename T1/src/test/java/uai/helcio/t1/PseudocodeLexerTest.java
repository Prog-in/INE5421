package uai.helcio.t1;

import org.junit.jupiter.api.Test;
import uai.helcio.t1.Automata.DFA;
import uai.helcio.t1.Automata.DFABuilder;
import uai.helcio.t1.Automata.DFAMinimizer;
import uai.helcio.t1.Automata.NFA;
import uai.helcio.t1.Automata.NFAUnionBuilder;
import uai.helcio.t1.converters.ExtendedToPureRegexConverter;
import uai.helcio.t1.converters.NFAToDFAConverter;
import uai.helcio.t1.converters.RegexToTreeConverter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PseudocodeLexerTest {

    @Test
    public void testPseudocodeLexicalAnalysis() {
        try {
            Path regexFile = findTestFile("lex_pseudocode.txt");
            Path inputFile = findTestFile("input_pseudocode.txt");
            Path expectedFile = findTestFile("expected-output_pseudocode.txt");

            DFA lexicalAnalyzer = buildLexicalAnalyzer(regexFile);

            List<String> actualOutput = processInput(inputFile, lexicalAnalyzer);

            List<String> expectedOutput = Files.readAllLines(expectedFile);

            // Compare outputs
            assertEquals(expectedOutput.size(), actualOutput.size(),
                    String.format("Number of tokens doesn't match. Expected: %d, Got: %d",
                            expectedOutput.size(), actualOutput.size()));

            for (int i = 0; i < expectedOutput.size(); i++) {
                String expected = expectedOutput.get(i).trim();
                String actual = actualOutput.get(i).trim();
                assertEquals(expected, actual,
                        String.format("Token mismatch at line %d:\n  Expected: %s\n  Actual:   %s",
                                i + 1, expected, actual));
            }

            System.out.println("✓ All " + actualOutput.size() + " tokens matched successfully!");

        } catch (IOException e) {
            fail("Failed to read test files: " + e.getMessage());
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage(), e);
        }
    }

    /**
     * Try to find test file in multiple possible locations
     */
    private Path findTestFile(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(filename);
        if (resource != null) {
            try {
                return Paths.get(resource.toURI());
            } catch (URISyntaxException ignored) {
                throw new IOException("Could not find test file: " + filename +
                        "\nTried: classloader, src/test/resources/, T1/src/test/resources/, test/resources/");
            }
        }
        throw new IOException("Could not find test file: " + filename +
                "\nTried: classloader, src/test/resources/, T1/src/test/resources/, test/resources/");
    }

    private DFA buildLexicalAnalyzer(Path regexFile) throws IOException {
        List<DFA> individualDFAs = Files.readAllLines(regexFile).stream()
                .filter(line -> !line.trim().isEmpty())
                .map(ExtendedToPureRegexConverter::convert)
                .map(RegexToTreeConverter::convert)
                .map(DFABuilder::build)
                .map(DFAMinimizer::minimize)
                .toList();

        List<String> priorityOrder = individualDFAs.stream()
                .map(DFA::getTokenName)
                .toList();

        NFA unitedNFA = NFAUnionBuilder.union(individualDFAs);
        DFA lexicalAnalyzer = NFAToDFAConverter.convert(unitedNFA, priorityOrder);
        return DFAMinimizer.minimize(lexicalAnalyzer);
    }

    private List<String> processInput(Path inputFile, DFA lexer) throws IOException {
        List<String> output = new ArrayList<>();
        List<String> lines = Files.readAllLines(inputFile);

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            int currentPos = 0;
            while (currentPos < line.length()) {
                DFA.TokenResult result = lexer.nextToken(line, currentPos);

                if (result != null) {
                    if (!result.tokenName().equals("ws")) {
                        String token = String.format("<%s, %s>", result.lexeme(), result.tokenName());
                        output.add(token);
                    }
                    currentPos = result.endPosition();
                } else {
                    String invalidChar = String.valueOf(line.charAt(currentPos));
                    if (!invalidChar.trim().isEmpty()) {
                        String token = String.format("<%s, ERROR>", invalidChar);
                        output.add(token);
                    }
                    currentPos++;
                }
            }
        }

        return output;
    }
}