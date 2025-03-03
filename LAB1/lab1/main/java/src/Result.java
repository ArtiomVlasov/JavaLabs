package src;


/**
 * Class to write amount of Bulls and Cows
 */
public class Result {
    private final int bulls;
    private final int cows;

    /**
     * Result constructor
     * @param bulls amount of Bulls
     * @param cows amount of Cows
     */
    public Result(int bulls, int cows) {
        this.bulls = bulls;
        this.cows = cows;
    }

    /**
     * @return Bulls amount
     */
    public int getBulls() {
        return bulls;
    }

    /**
     * @return Cows amount
     */
    public int getCows() {
        return cows;
    }
}