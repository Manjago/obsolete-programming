import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;


enum State {
    NORMAL, WAIT_NAME, RECORD_DEF
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
        if (pos >= 0) {
            return s.substring(0, pos);
        } else {
            return s;
        }
    }
}

class Interpreter {

    final Deque<Integer> stack = new LinkedList<>();
    final Deque<IfElsFi> ifStack = new LinkedList<>();
    final Map<String, List<String>> defs = new HashMap<>();
    private final Set<String> noTraceDefs = Set.of("ABS");
    String currentDefName;
    State state = State.NORMAL;
    private boolean traceOn = true;

    void exec(String s) {
        final List<String> tokens = Arrays.stream(s.split(" ")).filter(it -> !it.isBlank()).toList();
        exec(tokens);
    }

    private void exec(List<String> tokens) {
        for (String token : tokens) {
            final String oldStack = stack.toString();
            final String oldIfStack = ifStack.toString();
            final String oldState = state.toString();

            if (ifPreprocessOk(token)) {
                processOneToken(token);
            }

            trace(token, oldStack, stack, oldIfStack, ifStack, oldState, state);
        }
    }

    private boolean ifPreprocessOk(String token) {

        if (state != State.NORMAL) {
            return true;
        }

        switch (token) {
            case "IF":
                if (!ifStack.isEmpty() && !ifStack.peek().needCalc) {
                    return false; // надо помечать, что заскипали
                }
                final Integer arg = stack.pop();
                ifStack.push(new IfElsFi(token, arg != 0));
                return false;
            case "ELS":
                final IfElsFi ifElsFi = ifStack.pop();
                if (!ifElsFi.kind.equals("IF")) {
                    throw new IllegalStateException("Unexpected " + ifElsFi);
                }
                ifStack.push(ifElsFi);
                ifStack.push(new IfElsFi("ELS", !ifElsFi.needCalc));
                return false;
            case "FI":
                final IfElsFi pretender = ifStack.pop();
                if (pretender.kind.equals("ELS")) {
                    final IfElsFi mustBeIf = ifStack.pop();
                    if (!mustBeIf.kind.equals("IF")) {
                        throw new IllegalStateException("Must be IF, but " + mustBeIf);
                    }
                } else if (pretender.kind.equals("IF")) {
                    // do nothing
                } else {
                    throw new IllegalStateException("Bad stack item " + pretender);
                }
                return false;
            default:
                if (ifStack.isEmpty()) {
                    return true;
                } else {
                    return ifStack.peek().needCalc;
                }
        }
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

    private void trace(String s, String oldStack, Deque<Integer> newStack, String oldIfState, Deque<IfElsFi> ifState, String oldState, State state) {
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

    private static class IfElsFi {
        private String kind;
        private boolean needCalc;

        public IfElsFi(String kind, boolean needCalc) {
            this.kind = kind;
            this.needCalc = needCalc;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", IfElsFi.class.getSimpleName() + "[", "]").add("kind='" + kind + "'").add("needCalc=" + needCalc).toString();
        }
    }
}