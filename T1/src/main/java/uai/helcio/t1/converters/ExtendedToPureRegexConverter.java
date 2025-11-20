package uai.helcio.t1.converters;

import uai.helcio.t1.entities.Rule;

public class ExtendedToPureRegexConverter {
    public static Rule convert(String line) {
        StringBuilder ruleName = new StringBuilder();

        // getting rule name: everything until the first space char or ':'
        int i = 0;
        char c = 0;
        for (; i < line.length(); i++) {
            c = line.charAt(i);
            if (c == ':' || Character.isSpaceChar(c)) {
                break;
            }
            ruleName.append(c);
        }

        // going to the second part of the line
        while (i < line.length() && (c == ':' || Character.isSpaceChar(line.charAt(i)))) {
            i++;
            c = line.charAt(i);
        }

        // getting regex
        String regex = line.substring(i);
        String pureRegex = toPure(regex);

        return new Rule(ruleName.toString(), pureRegex);
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
