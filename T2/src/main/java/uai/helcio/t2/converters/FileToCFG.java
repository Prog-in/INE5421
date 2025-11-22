package uai.helcio.t2.converters;

import org.apache.commons.lang3.tuple.Pair;
import uai.helcio.t2.entities.CFG;
import uai.helcio.t2.entities.NonTerminal;
import uai.helcio.t2.entities.Symbol;
import uai.helcio.t2.entities.Terminal;
import uai.helcio.utils.FileParsingUtils;

import java.util.ArrayList;
import java.util.List;

public class FileToCFG {
    public static CFG convert(List<String> lines) {
        CFG cfg = new CFG();
        for (String line : lines) {
            Pair<String, List<Symbol>> headAndBody = convertLine(line);
            String head = headAndBody.getLeft();
            List<Symbol> body = headAndBody.getRight();
            if (head.isEmpty() || body.isEmpty()) {
                continue;
            }
            cfg.addProduction(head, body);
        }
        return cfg;
    }

    public static Pair<String, List<Symbol>> convertLine(String line) {
        int i = 0;
        Pair<String, Integer> headAndNewI = FileParsingUtils.getStringBetween(line, i, '<', '>', false);
        String head = headAndNewI.getLeft();
        i = headAndNewI.getRight();

        i = FileParsingUtils.jumpToExpr(line, i, "::=");
        i = FileParsingUtils.jumpSpaces(line, i);

        List<Symbol> body = new ArrayList<>();
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            switch (c) {
                case '&': {
                    body.add(Terminal.EPSILON);
                    break;
                } case '<': {
                    Pair<String, Integer> nonTerminalAndNewI = FileParsingUtils.getStringBetween(line, i, '<', '>', false);
                    String nonTerminal = nonTerminalAndNewI.getLeft();
                    int newI = nonTerminalAndNewI.getRight() - 1;
                    if (newI < line.length() && line.charAt(newI) == '>' && !nonTerminal.isEmpty()) {
                        i = newI;
                        body.add(NonTerminal.of(nonTerminalAndNewI.getLeft()));
                        break;
                    }
                } default: {
                    Pair<String, Integer> terminalAndNewI = FileParsingUtils.captureUntil(line, i, ' ', false);
                    i = terminalAndNewI.getRight() - 1;
                    body.add(Terminal.of(terminalAndNewI.getLeft()));
                }
            }
        }

        return Pair.of(head, body);
    }
}
