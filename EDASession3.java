// CRV STL SAZ EDPA1-3-03

import java.util.*;
    
public class EDASession3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter postfix expression:");
        String input = scanner.nextLine();
        String[] tokens = input.split("\\s+");
        Stack<String> stack = new Stack<>();

        for (String token : tokens) {
            switch (token) {
                case "+": // (+) Binary operator that concatenates two Strings.
                    if (stack.size() < 2) {
                        System.out.println("Error: Not enough operands for '+'");
                        return;
                    }
                    String b = stack.pop();
                    String a = stack.pop();
                    String concatenated = a + b;
                    System.out.println(a + " + " + b + " = " + concatenated);
                    stack.push(concatenated);
                    break;
                case "-": // (-) Binary operator that removes the characters from the second String in the first String. 
                    if (stack.size() < 2) {
                        System.out.println("Error: Not enough operands for '-'");
                        return;
                    }
                    b = stack.pop();
                    a = stack.pop();
                    String removed = remove(a, b);
                    System.out.println(a + " - " + b + " = " + removed);
                    stack.push(removed);
                    break;
                case "@": // (@) Unary operator that reverses the String. This operation will be implemented using an auxiliary stack to reverse the String.
                    if (stack.isEmpty()) {
                        System.out.println("Error: Not enough operands for '@'");
                        return;
                    }
                    a = stack.pop();
                    String reversed = reverse(a);
                    System.out.println(a + " @ = " + reversed);
                    stack.push(reversed);
                    break;
                case "*": // (*) Binary operator that intersects two Strings, selecting the characters that are present in both. 
                    b = stack.pop();
                    a = stack.pop();
                    String intersection = intersect(a, b);
                    System.out.println(a + " * " + b + " = " + intersection);
                    stack.push(intersection);
                    System.out.println("Contents in the stack:");
                    while (!stack.isEmpty()) {
                        System.out.println(stack.pop());
                    }

                    break;

                default:
                    stack.push(token);
                    break;
            }
        }

        if (!stack.isEmpty()) {
            System.out.println("Final result: " + stack.peek());
        }
        else {
            System.out.println("No content in the stack.");
        }
    }

    // (-) Remove function
    private static String remove(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (char c : a.toCharArray()) {
            if (b.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // (@) Reverse function 
    private static String reverse(String s) {
        Stack<Character> charStack = new Stack<>();
        for (char c : s.toCharArray()) {
            charStack.push(c);
        }
        StringBuilder sb = new StringBuilder();
        while (!charStack.isEmpty()) {
            sb.append(charStack.pop());
        }
        return sb.toString();
    }

    // (*) Intersect function 
    private static String intersect(String a, String b) {
        Set<Character> setB = new HashSet<>();
        for (char c : b.toCharArray())
            setB.add(c);
        StringBuilder sb = new StringBuilder();
        for (char c : a.toCharArray()) {
            if (setB.contains(c))
                sb.append(c);
        }
        return sb.toString();
    }
}