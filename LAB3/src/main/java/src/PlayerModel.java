package src;

public class PlayerModel {
    private int posX = 0;
    private int posY = 0;
    private int lives = 1;
    private int score = 0;
    private int speed = 1;

    public void setPos(int x, int y){
        this.posX = x;
        this.posY = y;
    }
    public int getPosX(){
        return posX;
    }
    public int getPosY(){
        return posY;
    }
    public void decrementLives(){
        this.lives--;
    }
    public int getLives() {
        return lives;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getScore(){
        return score;
    }
    public void shoot(){

    }
    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
