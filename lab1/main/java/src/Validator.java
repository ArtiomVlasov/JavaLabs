package src;

public class Validator {
    public static boolean isValid(String input, int numsAmount) {
        if (input.length() != numsAmount) return false;

        boolean[] used = new boolean[10];
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c)) return false;

            int digit = c - '0';
            if (used[digit]) return false;

            used[digit] = true;
        }

        return true;
    }

    public static int[] parseInput(String input) {
        int[] result = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            result[i] = input.charAt(i) - '0';
        }
        return result;
    }
}
