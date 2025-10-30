package entity;

import main.GamePanel;
import tile.TileManager2;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Fireball {
    public int x, y;
    public int vx, vy;
    public int radius = 6;
    public int damage = 8;

    public boolean knockback = false;
    public boolean burning   = false;
    public int burnFrames = 3 * 120;

    public boolean alive = true;

    private final GamePanel gp;
    private BufferedImage sprite;

    public Fireball(GamePanel gp, int x, int y, int vx, int vy, boolean knockback, boolean burning) {
        this.gp = gp;
        this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        this.knockback = knockback;
        this.burning = burning;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/boss/Fire_1.png"));
        } catch (Exception ignored) {}
    }
    
    public boolean hitPlayer(Player p) {
        Rectangle pr = new Rectangle(
                p.worldX + p.solidArea.x,
                p.worldY + p.solidArea.y,
                p.solidArea.width,
                p.solidArea.height
        );
        Rectangle fb = new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
        if (pr.intersects(fb)) {
            p.takeDamageFrom(directionToPlayer(p), damage, knockback, burning, burnFrames);
            return true;
        }
        return false;
    }

    private String directionToPlayer(Player p) {
        int px = p.worldX;
        int py = p.worldY;
        return (px > x) ? "right" : "left";
    }

    public void update() {
        if (!alive) return;
        x += vx;
        y += vy;

        TileManager2 tm = gp.currentMap;
        if (tm != null) {
            if (tm.isSolidAtPixel(x, y)) {
                alive = false;
                return;
            }
        }

        if (hitPlayer()) {

            String dir = Math.abs(vx) > Math.abs(vy)
                    ? (vx > 0 ? "right" : "left")
                    : (vy > 0 ? "down" : "up");

            gp.player.takeDamageFrom(dir, damage, knockback, burning, burnFrames);
            
            
            alive = false;
        }
    }

    private boolean hitPlayer() {
        int px = gp.player.worldX + gp.player.solidArea.x;
        int py = gp.player.worldY + gp.player.solidArea.y;
        int pw = gp.player.solidArea.width;
        int ph = gp.player.solidArea.height;

        Rectangle pr = new Rectangle(px, py, pw, ph);
        Rectangle fb = new Rectangle(x - radius, y - radius, radius*2, radius*2);
        return pr.intersects(fb);
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY, double zoom) {
        if (!alive) return;

        int screenX = (int)((x - cameraX) * zoom);
        int screenY = (int)((y - cameraY) * zoom);

        if (sprite != null) {
            int w = (int)(sprite.getWidth() * 0.5 * zoom);
            int h = (int)(sprite.getHeight() * 0.5 * zoom);
            g2.drawImage(sprite, screenX - w/2, screenY - h/2, w, h, null);
        } else {
            // fallback simple circle
            g2.setColor(new Color(255,120,40));
            int r = (int)(radius * zoom);
            g2.fillOval(screenX - r, screenY - r, 2*r, 2*r);
        }
    }
    
}