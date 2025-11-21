package uai.helcio.t2.converters;

import org.apache.commons.lang3.tuple.Pair;
import uai.helcio.t2.entities.CFG;
import uai.helcio.t2.entities.NonTerminal;
import uai.helcio.t2.entities.Symbol;
import uai.helcio.t2.entities.Terminal;

import java.util.ArrayList;
import java.util.List;

public class FileToCFG {
    public static CFG convert(List<String> lines) {
        CFG cfg = new CFG();
        for (String line : lines) {
            Pair<String, List<Symbol>> headAndBody = convertLine(line);
            String head = headAndBody.getLeft();
            List<Symbol> body = headAndBody.getRight();
            cfg.addProduction(head, body);
        }
        return cfg;
    }

    public static Pair<String, List<Symbol>> convertLine(String line) {
        int i = 0;
        Pair<String, Integer> headAndNewI = getCharBetween(line, i, '<', '>');
        String head = headAndNewI.getLeft();
        i = headAndNewI.getRight();

        i = jumpToExpr(i, line, "::=");
        i = jumpSpaces(i, line);

        List<Symbol> body = new ArrayList<>();
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == '<') {
                Pair<String, Integer> nonTerminalAndNewI = getCharBetween(line, i, '<', '>');
                i = nonTerminalAndNewI.getRight()-1;
                body.add(NonTerminal.of(nonTerminalAndNewI.getLeft()));
            } else {
                body.add(Terminal.of(String.valueOf(c)));
            }
        }

        return Pair.of(head, body);
    }

    private static int jumpToExpr(int i, String str, String subexpr) {
        while (++i < str.length() && str.substring(i, Math.min(str.length(), i + 3)).equals(subexpr)) {};
        return i + (i < str.length() ? subexpr.length()-1 : 0);
    }

    private static int jumpSpaces(int i, String str) {
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        };
        return i;
    }

    private static Pair<String, Integer> getCharBetween(String line, int i, char begin, char end) {
        StringBuilder sb = new StringBuilder();
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c != begin) {
                continue;
            }
            while (++i < line.length() && (c = line.charAt(i)) != end) {
                sb.append(c);
            }
            if (c == end) {
                i++;
                break;
            }
        }
        return Pair.of(sb.toString(), i);
    }
}
