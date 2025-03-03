package src;

import junit.framework.TestCase;
import src.BullsAndCows;
import src.Result;

/**
 *  Game class, provides all mechanics of "Bulls and Cows"
 */

public class BullsAndCowsTest extends TestCase {

    private BullsAndCows game;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        game = new BullsAndCows();
    }

    public void testCheckGuess_allBulls() {
        int[] secret = {1, 2, 3, 4};
        game.setSecretNum(secret);
        int[] guess = {1, 2, 3, 4};

        Result result = game.checkGuess(guess);

        assertEquals("Все цифры совпадают (быки)", 4, result.getBulls());
        assertEquals("Нет коров", 0, result.getCows());
    }

    public void testCheckGuess_someBullsAndCows() {
        int[] secret = {1, 2, 3, 4};
        game.setSecretNum(secret);
        int[] guess = {1, 3, 4, 2};

        Result result = game.checkGuess(guess);

        assertEquals("Одна цифра на правильной позиции (бык)", 1, result.getBulls());
        assertEquals("Три цифры совпадают, но на неправильных позициях (коровы)", 3, result.getCows());
    }

    public void testCheckGuess_noMatches() {
        int[] secret = {1, 2, 3, 4};
        game.setSecretNum(secret);
        int[] guess = {5, 6, 7, 8};

        Result result = game.checkGuess(guess);

        assertEquals("Нет быков", 0, result.getBulls());
        assertEquals("Нет коров", 0, result.getCows());
    }

    public void testContains() {
        int[] array = {1, 2, 3, 4};

        assertTrue("Массив содержит число 3", game.contains(array, 3));
        assertFalse("Массив не содержит число 5", game.contains(array, 5));
    }
}