package uai.helcio.t1.converters;

import org.apache.commons.lang3.tuple.Pair;
import uai.helcio.t1.entities.Rule;
import uai.helcio.utils.AppLogger;
import uai.helcio.utils.FileParsingUtils;

public class ExtendedToPureRegexConverter {
    public static Rule convert(String line) {
        int i = 0;

        Pair<String, Integer> ruleNameAndNewI = FileParsingUtils.captureUntil(line, i, ':', false);
        String ruleName = ruleNameAndNewI.getLeft();
        i = ruleNameAndNewI.getRight();

        i = FileParsingUtils.jumpSpaces(line, i);

        String regex = captureRegex(line, i);

        return new Rule(ruleName, regex);
    }

    private static String captureRegex(String line, int i) {
        String regex = line.substring(i);
        return toPure(regex);
    }

    private static String toPure(String regex) {
        StringBuilder pureRegex = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            // Escape logic
            if (c == '\\' && i + 1 < regex.length()) {
                pureRegex.append(c);
                pureRegex.append(regex.charAt(++i));
                continue;
            }

            if (c == '[') {
                StringBuilder buf = new StringBuilder("[");
                int j = i + 1;
                char next;
                do {
                    next = regex.charAt(j);
                    buf.append(next);
                    j++;
                } while (next != ']');
                i = j - 1;
                String pureOr = extendedToPureOr(buf.toString());
                pureRegex.append(pureOr);
                continue;
            }
            pureRegex.append(c);
        }
        String result = pureRegex.toString();
        AppLogger.logger.debug("Original RegEX: '{}' -> Pure: '{}'", regex, result);
        return result;
    }

    /**
     * Converts the syntatic sugar [x_1-x_2...x_n-x_(n+1)] to (x_1 | x_1+1 | ... | x_2 | ... | x_n | ... | x_(n_+1))
     * @param squareBracketsExp an expression in the form [x_1-x_2...x_n-x_(n+1)]
     * @return the converted expression (x_1 | x_1+1 | ... | x_2 | ... | x_n | ... | x_(n_+1))
     *
     * @apiNote Just works when |x_1| = |x_2| = ... = |x_(n+1)| = 1
     */
    private static String extendedToPureOr(String squareBracketsExp) {
        StringBuilder pureOr = new StringBuilder("(");
        for (int i = 1; i < squareBracketsExp.length() - 1; i++) {
            char current = squareBracketsExp.charAt(i);
            if (current == '\\') {
                continue;
            }

            if (i + 2 < squareBracketsExp.length() && squareBracketsExp.charAt(i + 1) == '-') {
                char end = squareBracketsExp.charAt(i + 2);

                // expansion
                for (char c = current; c <= end; c++) {
                    pureOr.append(c).append('|');
                }

                // skips - and final char
                i += 2;
            } else {
                pureOr.append(current).append('|');
            }
        }

        if (pureOr.length() > 1) {
            pureOr.setLength(pureOr.length() - 1);
        }

        pureOr.append(")");
        return pureOr.toString();
    }
}
