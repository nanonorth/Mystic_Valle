package tile;

import java.awt.image.BufferedImage;

public class Tile2 {
    private BufferedImage image;
    private boolean collision; // ถ้า true ผู้เล่นไม่สามารถเดินผ่านได้
    
    public int worldBottomY;

    public Tile2(BufferedImage image, boolean collision) {
        this.image = image;
        this.collision = collision;
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean hasCollision() {
        return collision;
    }
}
