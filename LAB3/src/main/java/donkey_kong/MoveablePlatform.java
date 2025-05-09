package donkey_kong;

import java.awt.Graphics2D;

public class MoveablePlatform extends Platform {
    private int minX;
    private int maxX;
    private int speed;
    private boolean movingRight;

    public MoveablePlatform(int x, int y, int width, int height, int minX, int maxX) {
        super(x, y, width, height, 0);
        this.minX = minX;
        this.maxX = maxX;
        this.speed = 2;
        this.movingRight = true;
    }

    public void update() {
        if (movingRight) {
            if (getX() + speed <= maxX) {
                setX(getX() + speed);
            } else {
                movingRight = false;
            }
        } else {
            if (getX() - speed >= minX) {
                setX(getX() - speed);
            } else {
                movingRight = true;
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        super.render(g2d);
    }
} 