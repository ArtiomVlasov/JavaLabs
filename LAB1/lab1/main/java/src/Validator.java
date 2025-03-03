package src;

/**
 * Class to validate input
 */

public class Validator {
    /**
     * Parse and validate input
     * @param input user input line
     * @param numsAmount number of digits
     * @return parsed String to int[]
     * @throws IllegalArgumentException If the string length does not meet the requirement,
     *                                  if the string contains invalid characters (for example, no digits or the digit 0),
     *                                  or if duplicate digits are encountered.
     */
    public static int[] parseInput(String input, int numsAmount) {
        if (input.length() != numsAmount){
            throw new IllegalArgumentException("Длина вашего числа не соответствует длине угадываемого");
        }
        int[] result = new int[numsAmount];
        boolean[] used = new boolean[10];
        int i = 0;
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c) || c == '0'){
                throw new IllegalArgumentException("Ввод содержит недопустимые символы или 0");
            }

            int digit = c - '0';
            if (used[digit]) {
                throw new IllegalArgumentException("Ввод содержит повторяющиеся цифры");
            }

            result[i] = digit;
            used[digit] = true;
            i+=1;
        }

        return result;
    }
}
