import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;



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
            interpreter.exec(line);
        }
    }
}

class Interpreter {

    final Deque<Integer> stack = new LinkedList<>();
    final Map<String, List<String>> defs = new HashMap<>();

    final Deque<IfState> ifState = new LinkedList<>();

    String currentDefName;

    State state = State.NORMAL;

    void exec(String s) {
        final List<String> tokens = Arrays.stream(s.split(" ")).filter(it -> !it.isBlank()).toList();
        exec(tokens);
    }

    private void exec(List<String> tokens) {
        for (String token : tokens) {
            final String oldStack = stack.toString();
            final String oldIfState = ifState.toString();
            final String oldState = state.toString();
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
            trace(token, oldStack, stack, oldIfState, ifState, oldState, state);
        }
    }

    private void execNormalToken(String token) {
        if("IF".equals(token)) {
            Integer arg = stack.pop();
            if (arg != 0) {
                ifState.push(IfState.IN_TRUE_DO);
            } else {
                ifState.push(IfState.IN_TRUE_SKIP);
            }
            return;
        }

        if (!ifState.isEmpty()) {
            final IfState currentIfState = ifState.peek();
            switch (currentIfState) {
                case IN_TRUE_DO -> {
                    if ("ELS".equals(token)) {
                        ifState.pop();
                        ifState.add(IfState.IN_FALSE_SKIP);
                        return;
                    }
                    if ("FI".equals(token)) {
                        ifState.pop();
                        return;
                    }
                }
                case IN_TRUE_SKIP -> {
                    if ("ELS".equals(token)) {
                        ifState.pop();
                        ifState.add(IfState.IN_FALSE_DO);
                        return;
                    }
                    if ("FI".equals(token)) {
                        ifState.pop();
                        return;
                    }
                    return;
                }
                case IN_FALSE_DO -> {
                    if ("FI".equals(token)) {
                        ifState.pop();
                        return;
                    }
                }
                case IN_FALSE_SKIP -> {
                    if ("FI".equals(token)) {
                        ifState.pop();
                        return;
                    }
                    return;
                }
            }
        }

        final Integer number = tryParse(token);

        if (number != null) {
            stack.push(number);
            return;
        }

        final List<String> customDef = defs.get(token);
        if (customDef != null) {
            trace("Start custom '" + token+"'");
            exec(customDef);
            trace("End custom '" + token+"'");
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
       System.err.println(s + " " + oldStack + "->" + newStack + ", " + oldIfState + "->" + ifState +", " + oldState +"->"+ state);
   }

   private void trace(String s) {
       System.err.println(s);
   }
}