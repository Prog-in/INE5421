package uai.helcio.t1.converters;

import org.apache.commons.lang3.tuple.Pair;
import uai.helcio.t1.entities.Rule;
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
            // ignore space chars
            if (Character.isSpaceChar(c)) {
                continue;
            }
            pureRegex.append(c);
        }
        return pureRegex.toString();
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
        for (int i = 1; i < squareBracketsExp.length()-1; i++) {
            char min = squareBracketsExp.charAt(i);
            i += 2; // '-'
            char max = squareBracketsExp.charAt(i);
            for (char num = min; num <= max; num++) {
                pureOr.append(num).append('|');
            }
        }
        // delete last '|'
        pureOr.deleteCharAt(pureOr.length() - 1);
        pureOr.append(")");
        return pureOr.toString();
    }
}
