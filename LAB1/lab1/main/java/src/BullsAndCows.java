package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.*;
/**
 *  Game class, provides all mechanics of "Bulls and Cows"
 */
public class BullsAndCows{
    private static final Logger logger = GameLogs.setupLogger(BullsAndCows.class.getName());
    private int[] secretNum;
    final int maxAttempts = 10;
    final int TimeToAttempt = 10000; //??

    /**
     * setter to secret number
     * @param num
     */

    public void setSecretNum(int[] num){
        secretNum = num;
        logger.info("Установлено секретное число: " + Arrays.toString(num));
    }

    /**
     * Initialize the start game.
     * Make number generator.
     * Validate data from input.
     * Compare input data with secret number.
     */

    public void start(){
        NumberGenerator numberGenerator = new NumberGenerator();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите количество цифр для отгадывания от 3 до 6: ");
        int numsAmount = -1;
        do {
            if(scanner.hasNextInt()){
                numsAmount = scanner.nextInt();
                scanner.nextLine();
                logger.info("Игрок выбрал длину числа: " + numsAmount);
            }
            else {
                scanner.next();
                logger.warning("Игрок ввел некорректное значение");
                System.out.println("Не число! Введите число от 3 до 6");

            }
        }while (numsAmount>6 || numsAmount < 3);
        secretNum = numberGenerator.generateNumber(numsAmount);
        logger.info("Секретное число сгенерировано: " + Arrays.toString(secretNum));
        int attempts = 0;

        while (attempts < maxAttempts) {
            String guessStr = "342";
            System.out.print("Введите число: ");
            long startTime = System.currentTimeMillis();
            try{
                while(System.currentTimeMillis() - startTime < TimeToAttempt){
                    if(reader.ready()){
                        guessStr = reader.readLine();
                        break;
                    }
                }
                if (System.currentTimeMillis() - startTime >= TimeToAttempt) {
                    System.out.println("\nВремя вышло. Игра завершена.");
                    logger.info("Время вышло. Игра завершена.");
                    System.exit(0);
                }
            }catch (IOException e){
                System.err.println("Failed to read data from  buffer: " + e.getMessage());
            }

            try{
                int[] guess  = Validator.parseInput(guessStr, numsAmount);
                logger.info("Игрок ввел: " + guessStr);
                Result result = checkGuess(guess);
                System.out.println("Быки: " + result.getBulls() + ", Коровы: " + result.getCows());

                if (result.getBulls() == numsAmount) {
                    System.out.println("Поздравляем! Вы угадали число!");
                    logger.info("Игрок угадал число");
                    break;
                }
                attempts++;
            }
            catch (IllegalArgumentException e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                logger.warning("Ошибка ввода: " + e.getMessage());
            }
        }

        if (attempts == maxAttempts) {
            System.out.print("Вы исчерпали попытки. Загаданное число: ");
            for (int i = 0; i < numsAmount; i++) {
                System.out.print(secretNum[i]);
            }
            logger.info("Игрок исчерпал попытки. Секретное число: " + Arrays.toString(secretNum));

        }
    }

    /**
     * Compare your sequence numbers and secret sequence numbers
     * @param guess your sequence numbers
     * @return How many Bulls, how many cows
     */

    final public Result checkGuess(int[] guess) {
        int bulls = 0, cows = 0;

        for (int i = 0; i < secretNum.length; i++) {
            if (secretNum[i] == guess[i]) {
                bulls++;
            } else if (contains(secretNum, guess[i])) {
                cows++;
            }
        }

        return new Result(bulls, cows);
    }

    /**
     *Check contains guess number in secret sequence numbers
     * @param array secret number
     * @param value your guess number
     * @return true - contains, false - not contains
     */

    final public boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }
}