package src;
import java.util.Scanner;

public class BullsAndCows{
    private int[] secretNum;
    private int maxAttempts = 10;

    public void start(){
        NumberGenerator numberGenerator = new NumberGenerator();
        Scanner scanner = new Scanner(System.in);
        int numsAmount;
        do {
            System.out.println("Введите количество цифр для отгадывания от 3 до 6: ");
            numsAmount = scanner.nextInt();
        }while (numsAmount>6 || numsAmount < 3);
        secretNum = numberGenerator.generateNumber(numsAmount);
        int attempts = 0;

        while (attempts < maxAttempts) {
            System.out.print("Введите число: ");
            String guessStr = scanner.nextLine();
            if (!Validator.isValid(guessStr, numsAmount)) {
                System.out.println("Некорректный ввод. Убедитесь, что число состоит из " +numsAmount + " уникальных цифр.");
                continue;
            }
            int[] guess = Validator.parseInput(guessStr);
            Result result = checkGuess(secretNum, guess);
            System.out.println("Быки: " + result.getBulls() + ", Коровы: " + result.getCows());

            if (result.getBulls() == 4) {
                System.out.println("Поздравляем! Вы угадали число!");
                break;
            }

            attempts++;
        }

        if (attempts == maxAttempts) {
            System.out.println("Вы исчерпали попытки. Загаданное число: " + secretNum);
        }
    }

    private Result checkGuess(int[] secret, int[] guess) {
        int bulls = 0, cows = 0;

        for (int i = 0; i < secret.length; i++) {
            if (secret[i] == guess[i]) {
                bulls++;
            } else if (contains(secret, guess[i])) {
                cows++;
            }
        }

        return new Result(bulls, cows);
    }
    private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }
}