package donkey_kong;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final Map<String, Image> textureCache = new HashMap<>();
    private static final String ASSETS_PATH = "src/main/java/donkey_kong/Assets/";
    
    // Sprite positions in a.png
    private static final int MARIO_X = 0;
    private static final int MARIO_Y = 0;
    private static final int MARIO_WIDTH = 16;
    private static final int MARIO_HEIGHT = 16;
    
    private static final int KONG_X = 32;
    private static final int KONG_Y = 0;
    private static final int KONG_WIDTH = 32;
    private static final int KONG_HEIGHT = 32;
    
    private static final int PRINCESS_X = 64;
    private static final int PRINCESS_Y = 0;
    private static final int PRINCESS_WIDTH = 16;
    private static final int PRINCESS_HEIGHT = 16;
    
    private static final int BARREL_X = 96;
    private static final int BARREL_Y = 0;
    private static final int BARREL_WIDTH = 16;
    private static final int BARREL_HEIGHT = 16;

    public static void initializeTextures() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(ASSETS_PATH + "a.png"));
            
            // Extract individual sprites
            textureCache.put("mario", spriteSheet.getSubimage(MARIO_X, MARIO_Y, MARIO_WIDTH, MARIO_HEIGHT));
            textureCache.put("kong", spriteSheet.getSubimage(KONG_X, KONG_Y, KONG_WIDTH, KONG_HEIGHT));
            textureCache.put("princess", spriteSheet.getSubimage(PRINCESS_X, PRINCESS_Y, PRINCESS_WIDTH, PRINCESS_HEIGHT));
            textureCache.put("barrel", spriteSheet.getSubimage(BARREL_X, BARREL_Y, BARREL_WIDTH, BARREL_HEIGHT));
            
            // Load other textures
            textureCache.put("platform", ImageIO.read(new File(ASSETS_PATH + "platform.png")));
            textureCache.put("ladder", ImageIO.read(new File(ASSETS_PATH + "ladder.png")));
            textureCache.put("background", ImageIO.read(new File(ASSETS_PATH + "background-2.jpg")));
            
        } catch (IOException e) {
            System.err.println("Failed to load textures");
            e.printStackTrace();
        }
    }

    public static Image getTexture(String textureName) {
        return textureCache.get(textureName);
    }

    public static void preloadTextures() {
        String[] textureFiles = {
            "platform.png",
            "ladder.png",
            "bar.png",
            "background-2.jpg",
            "health.png",
            "boost.png"
        };

        for (String texture : textureFiles) {
            getTexture(texture);
        }
    }
} 