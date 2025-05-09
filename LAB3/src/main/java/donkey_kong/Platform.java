package donkey_kong;

import java.awt.Graphics2D;
import java.awt.Image;

public class Platform extends GameObject {
    protected int width;
    protected int height;
    protected double angle; // angle in radians
    
    public Platform(int x, int y, int width, int height, double angleInDegrees) {
        setX(x);
        setY(y);
        this.width = width;
        this.height = height;
        this.angle = Math.toRadians(angleInDegrees);
        this.texture = TextureManager.getTexture("platform");
    }

    @Override
    public void render(Graphics2D g2d) {
        if (texture != null) {
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.rotate(angle, getX(), getY());
            g2dCopy.drawImage(texture, getX(), getY(), width, height, null);
            g2dCopy.dispose();
        }
    }

    public boolean isColliding(int px, int py) {
        // Transform point to platform's coordinate space
        double cos = Math.cos(-angle);
        double sin = Math.sin(-angle);
        double rx = (px - getX()) * cos - (py - getY()) * sin + getX();
        double ry = (px - getX()) * sin + (py - getY()) * cos + getY();
        
        return rx >= getX() && rx <= getX() + width &&
               ry >= getY() && ry <= getY() + height;
    }

    public double getAngle() {
        return angle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
} 