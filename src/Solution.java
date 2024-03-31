import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
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

        // Write an answer using System.out.println()
        // To debug: System.err.println("Debug messages...");

       // System.out.println("answer");
    }
}

class Interpreter {

    final Deque<Integer> stack = new LinkedList<>();

    void exec(String s) {
        final String[] tokens = s.split(" ");
        for(String token : tokens) {
            final Integer number = tryParse(token);
            if (number != null) {
                stack.push(number);
                continue;
            }

            if ("ADD".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg0 + arg1);
                continue;
            }

            if ("SUB".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg1 - arg0);
                continue;
            }

            if ("MUL".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg0 * arg1);
                continue;
            }

            if ("DIV".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg1 / arg0);
                continue;
            }

            if ("MOD".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg1 % arg0);
                continue;
            }

            if ("OUT".equals(token)) {
                final Integer arg0 = stack.pop();
                System.out.println(arg0);
                continue;
            }

            if ("POP".equals(token)) {
                stack.pop();
                continue;
            }

            if ("SWP".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg0);
                stack.push(arg1);
                continue;
            }

            if ("DUP".equals(token)) {
                final Integer arg0 = stack.pop();
                stack.push(arg0);
                stack.push(arg0);
                continue;
            }

            if ("ROT".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                final Integer arg2 = stack.pop();
                stack.push(arg1);
                stack.push(arg0);
                stack.push(arg2);
                continue;
            }

            if ("OVR".equals(token)) {
                final Integer arg0 = stack.pop();
                final Integer arg1 = stack.pop();
                stack.push(arg1);
                stack.push(arg0);
                stack.push(arg1);
                continue;
            }

            if ("NOT".equals(token)) {
                final Integer arg0 = stack.pop();
                if (0 == arg0) {
                    stack.push(1);
                } else {
                    stack.push(0);
                }
                continue;
            }

            if ("POS".equals(token)) {
                final Integer arg0 = stack.pop();
                if (arg0 >= 0) {
                    stack.push(1);
                } else {
                    stack.push(0);
                }
                continue;
            }

            throw new IllegalArgumentException("Unexpected '" + token + "'");
        }
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


}