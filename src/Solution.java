import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


enum State {
    NORMAL, WAIT_NAME, RECORD_DEF
}

enum IfState {
    IN_TRUE_DO, IN_TRUE_SKIP, IN_FALSE_DO, IN_FALSE_SKIP
}

class Solution {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }
        final Interpreter interpreter = new Interpreter();
        for (int i = 0; i < n; i++) {
            String line = in.nextLine();
            interpreter.exec(clean(line));
        }
    }

    private static String clean(String s) {
        final int pos = s.indexOf('#');
        if (pos >=0) {
            return s.substring(0, pos);
        } else {
            return s;
        }
    }
}

class Interpreter {

    final Deque<Integer> stack = new LinkedList<>();
    final Map<String, List<String>> defs = new HashMap<>();
    final Deque<IfState> ifState = new LinkedList<>();
    private final Set<String> noTraceDefs = Set.of("ABS");
    String currentDefName;
    State state = State.NORMAL;
    private boolean traceOn = true;

    void exec(String s) {
        final List<String> tokens = Arrays.stream(s.split(" "))
                .filter(it -> !it.isBlank())
                .toList();
        exec(tokens);
    }

    private void exec(List<String> tokens) {
        for (String token : tokens) {
            final String oldStack = stack.toString();
            final String oldIfState = ifState.toString();
            final String oldState = state.toString();

            if (ifPreprocessOk(token)) {
                processOneToken(token);
            }

            trace(token, oldStack, stack, oldIfState, ifState, oldState, state);
        }
    }

    private boolean ifPreprocessOk(String token) {

        if (ifState.isEmpty()) {
            return true;
        }

        final IfState currentIfState = ifState.peek();
        switch (currentIfState) {
            case IN_TRUE_DO -> {
                switch (token) {
                    case "ELS":
                        ifState.pop();
                        ifState.push(IfState.IN_FALSE_SKIP);
                        return false;
                    case "FI":
                        ifState.pop();
                        return true;
                }
            }
            case IN_TRUE_SKIP -> {
            }
            case IN_FALSE_DO -> {
            }
            case IN_FALSE_SKIP -> {
            }
        }

        return true;
    }

    private void processOneToken(String token) {
        switch (state) {
            case NORMAL -> execNormalToken(token);
            case WAIT_NAME -> {
                currentDefName = token;
                defs.put(currentDefName, new ArrayList<>());
                state = State.RECORD_DEF;
            }
            case RECORD_DEF -> {
                if ("END".equals(token)) {
                    state = State.NORMAL;
                } else {
                    defs.get(currentDefName).add(token);
                }
            }
        }
    }

    private void execNormalToken(String token) {

        if ("IF".equals(token)) {
            final Integer arg = stack.pop();
            if (arg != 0) {
                ifState.push(IfState.IN_TRUE_DO);
            } else {
                ifState.push(IfState.IN_TRUE_SKIP);
            }
            return;
        }

        final Integer number = tryParse(token);

        if (number != null) {
            stack.push(number);
            return;
        }

        final List<String> customDef = defs.get(token);
        if (customDef != null) {
            traceOn = !noTraceDefs.contains(token);
            traceCall(token, "Start custom");
            exec(customDef);
            traceCall(token, "End custom");
            traceOn = noTraceDefs.contains(token);
            return;
        }

        if ("DEF".equals(token)) {
            state = State.WAIT_NAME;
            return;
        }

        if ("ADD".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg0 + arg1);
            return;
        }

        if ("SUB".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg1 - arg0);
            return;
        }

        if ("MUL".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg0 * arg1);
            return;
        }

        if ("DIV".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg1 / arg0);
            return;
        }

        if ("MOD".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg1 % arg0);
            return;
        }

        if ("OUT".equals(token)) {
            final Integer arg0 = stack.pop();
            System.out.println(arg0);
            return;
        }

        if ("POP".equals(token)) {
            stack.pop();
            return;
        }

        if ("SWP".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg0);
            stack.push(arg1);
            return;
        }

        if ("DUP".equals(token)) {
            final Integer arg0 = stack.pop();
            stack.push(arg0);
            stack.push(arg0);
            return;
        }

        if ("ROT".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            final Integer arg2 = stack.pop();
            stack.push(arg1);
            stack.push(arg0);
            stack.push(arg2);
            return;
        }

        if ("OVR".equals(token)) {
            final Integer arg0 = stack.pop();
            final Integer arg1 = stack.pop();
            stack.push(arg1);
            stack.push(arg0);
            stack.push(arg1);
            return;
        }

        if ("NOT".equals(token)) {
            final Integer arg0 = stack.pop();
            if (0 == arg0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
            return;
        }

        if ("POS".equals(token)) {
            final Integer arg0 = stack.pop();
            if (arg0 >= 0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
            return;
        }

        throw new IllegalArgumentException("Unexpected '" + token + "'");
    }

    private Integer tryParse(String token) {
        Integer result;
        try {
            result = Integer.valueOf(token);
        } catch (NumberFormatException e) {
            result = null;
        }
        return result;
    }

    private void trace(String s, String oldStack, Deque<Integer> newStack, String oldIfState, Deque<IfState> ifState, String oldState, State state) {
        if (!traceOn) {
            return;
        }
        System.err.println(s + " " + oldStack + "->" + newStack + ", " + oldIfState + "->" + ifState + ", " + oldState + "->" + state);
    }

    private void traceCall(String name, String s) {
        if (!traceOn) {
            return;
        }
        System.err.println(s + "'" + name + '"');
    }
}