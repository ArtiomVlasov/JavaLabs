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
        while(attempts < maxAttempts){

        

    }
}