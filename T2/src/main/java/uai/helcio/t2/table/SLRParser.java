package uai.helcio.t2.table;

import uai.helcio.t2.entities.Symbol;
import uai.helcio.t2.entities.Terminal;
import uai.helcio.t2.entities.Token;
import uai.helcio.utils.AppLogger;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SLRParser {
    private final Map<Integer, Map<uai.helcio.t2.entities.Symbol, TableEntry>> parsingTable;
    private final Stack<Integer> stack = new Stack<>();

    public SLRParser(Map<Integer, Map<uai.helcio.t2.entities.Symbol, TableEntry>> parsingTable) {
        this.parsingTable = parsingTable;
    }

    public boolean parse(List<Token> inputs) {
        stack.clear();
        stack.push(0);

        inputs.add(new Token(Terminal.END_REPR, "EOF"));

        int cursor = 0;

        while (true) {
            int currentState = stack.peek();
            Token currentToken = inputs.get(cursor);

            Terminal symbol;
            if (currentToken.type().equals(Terminal.END_REPR)) {
                symbol = Terminal.END;
            } else {
                symbol = Terminal.of(currentToken.type());
            }

            Map<Symbol, TableEntry> row = parsingTable.get(currentState);

            if (row == null) {
                AppLogger.logger.error("Erro Sintático: Estado {} inválido (sem transições).", currentState);
                return false;
            }

            TableEntry action = row.get(symbol);

            if (action == null) {
                AppLogger.logger.error("Erro Sintático: Token inesperado {} no estado {}", currentToken, currentState);
                AppLogger.logger.debug("Esperado neste estado: {}", row.keySet());
                return false;
            }

            AppLogger.logger.trace("State: {} | Input: {} | Action: {}", currentState, currentToken, action);

            switch (action.type()) {
                case SHIFT -> {
                    stack.push(action.targetState());
                    cursor++;
                }
                case REDUCE -> {
                    var prod = action.productionToReduce();

                    int sizeToPop = prod.body().size();
                    if (sizeToPop == 1 && prod.body().getFirst().equals(Terminal.EPSILON)) {
                        sizeToPop = 0;
                    }

                    for (int i = 0; i < sizeToPop; i++) {
                        stack.pop();
                    }

                    int stateUncovered = stack.peek();
                    Map<Symbol, TableEntry> gotoRow = parsingTable.get(stateUncovered);

                    if (gotoRow == null || !gotoRow.containsKey(prod.head())) {
                        AppLogger.logger.error("Erro fatal no GOTO após redução de {}", prod.head());
                        return false;
                    }

                    int nextState = gotoRow.get(prod.head()).targetState();
                    stack.push(nextState);

                    AppLogger.logger.info("Redução: {} ::= {}", prod.head(), prod.body());
                }
                case ACCEPT -> {
                    AppLogger.logger.info("SUCESSO: Cadeia aceita pela gramática!");
                    return true;
                }
                case ERROR -> {
                    return false;
                }
            }
        }
    }
}