package entity;

import main.GamePanel;
import tile.TileManager2;

import java.awt.*;
import java.awt.image.BufferedImage;

// Inheritance

public abstract class Enemy extends Entity {

    protected final GamePanel gp;

    // Basic state
    public int HP = 200;
    protected boolean alive = true;
    protected boolean invincible = false;
    protected int invincibleCounter = 0;
    protected int invincibleTime = 30;

    // Animation counters
    protected int spriteCounter = 0;
    protected int spriteNum = 1;

    // Facing
    protected String facing = "right";

    public Rectangle solidArea = new Rectangle(20, 160, 80, 60);

    // Render depth ordering
    public int depthY = 0;

    public Enemy(GamePanel gp) {
        this.gp = gp;
        speed = 1;
    }

    public boolean isAlive() { return alive; }

    protected void moveWithCollision(int dx, int dy) {
        TileManager2 tm = gp.currentMap;
        if (tm == null) return;

        if (dx != 0 && dy != 0) {
            if (!collidesAtOffset(dx, 0, tm)) worldX += dx;
            if (!collidesAtOffset(0, dy, tm)) worldY += dy;
        } else {
            if (!collidesAtOffset(dx, dy, tm)) {
                worldX += dx;
                worldY += dy;
            }
        }
    }

    protected boolean collidesAtOffset(int ox, int oy, TileManager2 tm) {

        int left = worldX + ox + solidArea.x;
        int top = worldY + oy + solidArea.y;
        int right = left + solidArea.width - 1;
        int bottom = top + solidArea.height - 1;

        return tm.isSolidAtPixel(left, top)
            || tm.isSolidAtPixel(right, top)
            || tm.isSolidAtPixel(left, bottom)
            || tm.isSolidAtPixel(right, bottom);
    }

    public void takeDamage(int dmg) {

        if (!alive || invincible) return;

        HP -= dmg;

        if (HP <= 0) {
            HP = 0;
            die();
        } else {
            onHurt();
            invincible = true;
            invincibleCounter = 0;
        }
    }

    protected void commonInvincibleTick() {
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > invincibleTime) {
                invincibleCounter = 0;
                invincible = false;
            }
        }
    }

    protected void die() {
        alive = false;
        onDie();
    }

    protected abstract void onHurt();
    protected abstract void onDie();

    public abstract void update();
    public abstract void draw(Graphics2D g2, int cameraX, int cameraY, double zoom);
}
