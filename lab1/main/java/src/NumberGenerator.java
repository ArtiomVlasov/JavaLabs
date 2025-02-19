package src;
import java.util.Random;

class NumberGenerator {
    public int[] generateNumber(int numsAmount) {
        Random random = new Random();
        int[] secret = new int[numsAmount];
        boolean[] used = new boolean[10]; // Для проверки уникальности цифр

        for (int i = 0; i < numsAmount; i++) {
            int digit;
            do {
                digit = random.nextInt(10);
            } while (used[digit]);

            secret[i] = digit;
            used[digit] = true;
        }

        return secret;
    }
}