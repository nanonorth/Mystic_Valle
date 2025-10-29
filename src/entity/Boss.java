package entity;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Boss enemy with 3 attacks:
 *  - Attack1: melee (10 frames)
 *  - Attack2: radial fireballs (10 frames, burning)
 *  - Attack3: forward fireballs (7 frames, knockback)
 */



public class Boss extends Enemy {
	
	public ArrayList<Fireball> fireballs = new ArrayList<>();
	

    // sprites
    private BufferedImage[] walkR = new BufferedImage[8];
    private BufferedImage[] walkL = new BufferedImage[8];
    private BufferedImage[] idleR = new BufferedImage[8];
    private BufferedImage[] idleL = new BufferedImage[8];
    private BufferedImage[] hurtR = new BufferedImage[2];
    private BufferedImage[] hurtL = new BufferedImage[2];
    private BufferedImage[] deadR = new BufferedImage[10];
    private BufferedImage[] deadL = new BufferedImage[10];
    private BufferedImage[] atk1R = new BufferedImage[10];
    private BufferedImage[] atk1L = new BufferedImage[10];
    private BufferedImage[] atk2R = new BufferedImage[10];
    private BufferedImage[] atk2L = new BufferedImage[10];
    private BufferedImage[] atk3R = new BufferedImage[7];
    private BufferedImage[] atk3L = new BufferedImage[7];

    private int frameCounter = 0;
    private int frameIndex = 0;

    // state machine
    private enum State { IDLE, WALK, ATK1, ATK2, ATK3, HURT, DEAD }
    private State state = State.IDLE;

    // Melee range & vision
    private int vision = 360;
    private int meleeRange = 60;
    private int damageBoost = 0;

    // Attack cooldowns
    private int atkCooldown = 0;
    private int atkInterval = 90; // frames between attacks when in range

    // projectiles list lives in GamePanel to draw together; but we push into gpProjectiles
    private final List<Fireball> gpProjectiles;
    
    public Rectangle solidArea = new Rectangle(10, 200, 30, 20);

    public int maxHP = 200;
    
    public Boss(GamePanel gp, List<Fireball> sharedProjectiles) {
        super(gp);
        this.gpProjectiles = sharedProjectiles;
        
        this.maxHP = 200;
        this.HP = maxHP;

        solidArea = new Rectangle(40, 150, 70, 40);

        worldX = 20;
        worldY = 80;

        speed = 1;
        loadSprites();
    }

    private void loadSprites() {
        try {
            // helper lambda
            java.util.function.BiConsumer<BufferedImage[], String> load = (arr, path) -> {
                try {
                    BufferedImage sheet = ImageIO.read(getClass().getResourceAsStream(path));
                    int count = arr.length;
                    int w = sheet.getWidth() / count;
                    int h = sheet.getHeight();
                    for (int i=0;i<count;i++) arr[i] = sheet.getSubimage(i*w, 0, w, h);
                } catch (Exception e) { e.printStackTrace(); }
            };
            load.accept(walkR, "/boss/WalkR.png");
            load.accept(walkL, "/boss/WalkL.png");

            load.accept(idleR, "/boss/IdleR.png");
            load.accept(idleL, "/boss/IdleL.png");

            load.accept(hurtR, "/boss/HurtR.png");
            load.accept(hurtL, "/boss/HurtL.png");

            load.accept(deadR, "/boss/DeadR.png");
            load.accept(deadL, "/boss/DeadL.png");

            load.accept(atk1R, "/boss/Attack_1R.png");
            load.accept(atk1L, "/boss/Attack_1L.png");
            load.accept(atk2R, "/boss/Attack_2R.png");
            load.accept(atk2L, "/boss/Attack_2L.png");
            load.accept(atk3R, "/boss/Attack_3R.png");
            load.accept(atk3L, "/boss/Attack_3L.png");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHurt() {
        state = State.HURT;
        frameCounter = 0;
        frameIndex = 0;
    }

    @Override
    protected void onDie() {
        state = State.DEAD;
        frameCounter = 0;
        frameIndex = 0;

        // ✅ แจ้งเกมว่า Boss ตายแล้ว
        gp.bossFightActive = false;
        gp.bossDefeated = true;
    }

    private int distToPlayer() {
        int cx = worldX + solidArea.x + solidArea.width/2;
        int cy = worldY + solidArea.y + solidArea.height;
        int px = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width/2;
        int py = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height;
        int dx = px - cx;
        int dy = py - cy;
        return (int)Math.hypot(dx, dy);
    }

    private String dirToPlayer() {
        int cx = worldX + solidArea.x + solidArea.width/2;
        int px = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width/2;
        return (px >= cx) ? "right" : "left";
    }

    @Override
    public void update() {
        if (!alive && state != State.DEAD) {
            onDie();
        }
        
        int playerLevel = gp.player.getLevel();
        int damageBoost = playerLevel * 2;
        meleeRange = 60 + (playerLevel * 3);

        commonInvincibleTick();

        atkCooldown = Math.max(0, atkCooldown - 1);

        switch (state) {
            case DEAD -> updateDead();
            case HURT -> updateHurt();
            case ATK1, ATK2, ATK3 -> updateAttack();
            default -> decideAndAct();
        }
    }

    private void decideAndAct() {
        int d = distToPlayer();
        if (d > vision) {
            // idle
            state = State.IDLE;
            animate( idleR.length );
            return;
        }

        // face the player
        facing = dirToPlayer();

        if (d <= meleeRange && atkCooldown == 0) {
            state = State.ATK1;
            frameCounter = frameIndex = 0;
            atkCooldown = atkInterval;
            return;
        }

        if (d <= 220 && atkCooldown == 0) {
            state = State.ATK3; // forward shots
            frameCounter = frameIndex = 0;
            atkCooldown = atkInterval;
            return;
        }

        // occasionally do radial burst
        if (atkCooldown == 0) {
            state = State.ATK2;
            frameCounter = frameIndex = 0;
            atkCooldown = atkInterval + 60;
            return;
        }

        // chase
        state = State.WALK;
        chasePlayer();
        animate( walkR.length );
    }

    private void chasePlayer() {
        int px = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width/2;
        int py = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height;

        int cx = worldX + solidArea.x + solidArea.width/2;
        int cy = worldY + solidArea.y + solidArea.height;

        int dx = (px > cx) ? 1 : (px < cx ? -1 : 0);
        int dy = (py > cy) ? 1 : (py < cy ? -1 : 0);

        moveWithCollision(dx * speed, dy * speed);
        if (dx < 0) facing = "left";
        if (dx > 0) facing = "right";
    }

    private void updateAttack() {
        frameCounter++;
        int max = switch (state) {
            case ATK1 -> atk1R.length;
            case ATK2 -> atk2R.length;
            case ATK3 -> atk3R.length;
            default -> 1;
        };

        if (frameCounter > 6) { // animation speed
            frameCounter = 0;
            frameIndex++;

            // 🔥 Melee attack hit frame
            if (state == State.ATK1 && (frameIndex == 3 || frameIndex == 4)) {
                checkMeleeHit();
            }

            // Projectile spawn
            if (state == State.ATK2 && frameIndex == 4)
                spawnRadial();

            if (state == State.ATK3 && (frameIndex == 3 || frameIndex == 5))
                spawnForward();
        }

        if (frameIndex >= max) {
            frameIndex = 0;
            state = State.IDLE;
        }
    }
    
    private void checkMeleeHit() {

        // ✅ Hitbox ของ Boss
        Rectangle bossHit = new Rectangle(
            worldX + solidArea.x,
            worldY + solidArea.y,
            solidArea.width,
            solidArea.height
        );

        // ✅ ตัว Player
        Rectangle playerHit = new Rectangle(
            gp.player.worldX + gp.player.solidArea.x,
            gp.player.worldY + gp.player.solidArea.y,
            gp.player.solidArea.width,
            gp.player.solidArea.height
        );

        // ✅ ถ้าชนกัน → Player จะโดนตี
        if (bossHit.intersects(playerHit)) {

            // ✅ ทิศของความเสียหาย ใช้ facing ของ boss
            String dir = (facing.equals("left") ? "left" : "right");

            // ✅ Damage scaling ตามเลเวลผู้เล่น
            int dmg = 12 + damageBoost;

            // ✅ Knockback + ไม่เผา (atk1)
            gp.player.takeDamageFrom(dir, dmg, true, false, 0);
        }
    }


    private void spawnForward() {
        // forward in facing direction
        int speedFB = 5;
        int cx = worldX + solidArea.x + solidArea.width/2;
        int cy = worldY + solidArea.y + solidArea.height - 10;

        int dx = gp.player.worldX - worldX;
        int dy = gp.player.worldY - worldY;
        double dist = Math.hypot(dx, dy);
        int vx = (int)(dx / dist * speedFB * 2);
        int vy = (int)(dy / dist * speedFB * 2);

        // Attack3 A) knockback + damage
        gpProjectiles.add(new Fireball(gp, cx, cy, vx, vy, true, false));
    }

    private void spawnRadial() {
        // Attack2 C) burning around
        int cx = worldX + solidArea.x + solidArea.width/2;
        int cy = worldY + solidArea.y + solidArea.height - 10;

        int bullets = 12;
        int spd = 4;
        for (int i = 0; i < bullets; i++) {
            double ang = (2 * Math.PI * i) / bullets;
            int vx = (int)Math.round(Math.cos(ang) * spd);
            int vy = (int)Math.round(Math.sin(ang) * spd);
            gpProjectiles.add(new Fireball(gp, cx, cy, vx, vy, false, true));
        }
    }

    private void updateHurt() {
        // short flinch
        animate( hurtR.length );
        if (frameIndex >= hurtR.length - 1) {
            state = State.IDLE;
            frameIndex = 0;
        }
    }
    
    public void resetBoss() {
        this.HP = maxHP;
        this.alive = true;
        state = State.IDLE;
        frameIndex = 0;
        frameCounter = 0;
    }

    private void updateDead() {
        // play 10 frames then stay on last
        if (frameIndex < deadR.length - 1) {
            if (++frameCounter > 8) {
                frameCounter = 0;
                frameIndex++;
            }
        }
    }

    private void animate(int frames) {
        if (++frameCounter > 8) {
            frameCounter = 0;
            frameIndex++;
            if (frameIndex >= frames) frameIndex = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2, int cameraX, int cameraY, double zoom) {
        BufferedImage img = pickImage();

        int feetX = worldX + solidArea.x + solidArea.width / 2;
        int feetY = worldY + solidArea.y + solidArea.height;

        int screenX = (int)((feetX - cameraX) * zoom);
        int screenY = (int)((feetY - cameraY) * zoom);

        int w = (int)(img.getWidth() * zoom);
        int h = (int)(img.getHeight() * zoom);

        screenX -= w/2;
        screenY -= h;

        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        }
        g2.drawImage(img, screenX, screenY, w, h, null);
        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private BufferedImage pickImage() {
        return switch (state) {
            case DEAD -> (facing.equals("left") ? deadL : deadR)[Math.min(frameIndex, deadR.length-1)];
            case HURT -> (facing.equals("left") ? hurtL : hurtR)[Math.min(frameIndex, hurtR.length-1)];
            case ATK1 -> (facing.equals("left") ? atk1L : atk1R)[Math.min(frameIndex, atk1R.length-1)];
            case ATK2 -> (facing.equals("left") ? atk2L : atk2R)[Math.min(frameIndex, atk2R.length-1)];
            case ATK3 -> (facing.equals("left") ? atk3L : atk3R)[Math.min(frameIndex, atk3R.length-1)];
            case WALK -> (facing.equals("left") ? walkL : walkR)[spriteIndex(walkR.length)];
            default    -> (facing.equals("left") ? idleL : idleR)[spriteIndex(idleR.length)];
        };
    }

    private int spriteIndex(int frames) {
        return Math.min(frameIndex % frames, frames - 1);
    }
}