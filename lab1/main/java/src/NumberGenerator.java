package src;
import java.util.Arrays;
import java.util.Random;

/**
 * Class to generate numbers
 */

class NumberGenerator {
    /**
     * Function to generate secret numbers
     * @param numsAmount amount of figure in secret number
     * @return secret number
     */
    public int[] generateNumber(int numsAmount) {
        Random random = new Random();
        int[] secret = new int[numsAmount];
        boolean[] used = new boolean[10]; // Для проверки уникальности цифр

        for (int i = 0; i < numsAmount; i++) {
            int digit;
            do {
                digit = random.nextInt(9)+1;
            } while (used[digit]);

            secret[i] = digit;
            used[digit] = true;
        }

        return secret;
    }
}