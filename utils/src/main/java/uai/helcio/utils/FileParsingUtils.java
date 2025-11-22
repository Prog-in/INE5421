package uai.helcio.utils;

import org.apache.commons.lang3.tuple.Pair;

public class FileParsingUtils {
    public static int jumpToExpr(String str, int i, String subexpr) {
        while (++i < str.length() && str.substring(i, Math.min(str.length(), i + 3)).equals(subexpr));
        return i + (i < str.length() ? subexpr.length()-1 : 0);
    }

    public static int jumpSpaces(String str, int i) {
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        };
        return i;
    }

    public static Pair<String, Integer> getStringBetween(String str, int i, char begin, char end, boolean captureSpaces) {
        String token = "";
        for (; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != begin) {
                continue;
            }
            i++;
            Pair<String, Integer> tokenAndNewIndex = captureUntil(str, i, end, captureSpaces);
            token = tokenAndNewIndex.getLeft();
            i = tokenAndNewIndex.getRight();
            break;
        }
        return Pair.of(token, i);
    }

    public static Pair<String, Integer> captureUntil(String str, int i, char end, boolean captureSpaces) {
        StringBuilder sb = new StringBuilder();
        for (; i < str.length(); i++) {
            char c = str.charAt(i);
            while (i < str.length() && (c = str.charAt(i)) != end) {
                if (!captureSpaces || !Character.isWhitespace(c)) {
                    sb.append(c);
                }
                i++;
            }
            if (c == end) {
                i++;
                break;
            }
        }
        return Pair.of(sb.toString(), i);
    }
}
