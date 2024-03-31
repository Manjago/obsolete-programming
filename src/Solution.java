import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    void exec(@NotNull String s) {
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
        }
    }

    @Nullable Integer tryParse(@NotNull String token) {
        Integer result;
        try {
            result = Integer.valueOf(token);
        } catch (NumberFormatException e) {
            result = null;
        }
        return result;
    }


}