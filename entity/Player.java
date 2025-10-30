package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;
import tile.TileManager2;

// Inheritance

public class Player extends Entity {

	GamePanel gp;
	KeyHandler keyH;
	private String facingDirection = "right";

	public Rectangle solidArea = new Rectangle(10, 200, 30, 20);

	private int idleCounter = 0;
	private int idleSpriteNum = 1;

	public int depthY;
	public boolean isOnRoof;

	public boolean isDead = false;
	private int deadCounter = 0;
	private int deadFrame = 1;
	private final int MAX_DEAD_FRAME = 4;

	public boolean isHurt = false;
	private int hurtCounter = 0;
	private int hurtFrame = 1;

	private boolean invincible = false;
	private int invincibleTime = 60;
	private int invincibilityCounter = 0;

	public boolean isAttacking = false;
	private int attackCounter = 0;
	private int attackFrame = 1;

	private boolean knockback = false;
	private int knockbackPower = 6;
	private int knockbackCounter = 0;
	private String knockbackDirection = "";

	
	// Encapsulation
	
	private int HP = 100;
	private int maxHP = 100;
	private int baseHP = 100;

	private int mana = 100;
	private int maxMana = 100;
	private int baseMana = 100;

	private int stamina = 100;
	private int maxStamina = 100;
	private int baseStamina = 100;
	private boolean staminaDepleted = false;

	private int XP = 0;
	private int maxXP = 100;

	private int baseXP = 0;
	private int basemaxXP = 100;
	
	
	private int level = 1;

	private int gold = 0;
	
	//

	public Player(GamePanel gp, KeyHandler keyH) {
		this.gp = gp;
		this.keyH = keyH;
		setDefaultValues();
		getPlayerImage();
	}

	public void setDefaultValues() {
		float speed = 2;
		direction = "right";
		facingDirection = "right";
	}

	public void setSpawnPoint(int sx, int sy) {
		this.worldX = sx;
		this.worldY = sy;
	}

	public void getPlayerImage() {
		try {
			BufferedImage walkRightSheet = ImageIO.read(getClass().getResourceAsStream("/player/WalkR.png"));
			BufferedImage walkLeftSheet = ImageIO.read(getClass().getResourceAsStream("/player/WalkL.png"));

			int walkFrameWidth = walkRightSheet.getWidth() / 7;
			int walkFrameHeight = walkRightSheet.getHeight();

			r1 = walkRightSheet.getSubimage(0 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r2 = walkRightSheet.getSubimage(1 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r3 = walkRightSheet.getSubimage(2 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r4 = walkRightSheet.getSubimage(3 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r5 = walkRightSheet.getSubimage(4 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r6 = walkRightSheet.getSubimage(5 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			r7 = walkRightSheet.getSubimage(6 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);

			l1 = walkLeftSheet.getSubimage(0 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l2 = walkLeftSheet.getSubimage(1 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l3 = walkLeftSheet.getSubimage(2 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l4 = walkLeftSheet.getSubimage(3 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l5 = walkLeftSheet.getSubimage(4 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l6 = walkLeftSheet.getSubimage(5 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);
			l7 = walkLeftSheet.getSubimage(6 * walkFrameWidth, 0, walkFrameWidth, walkFrameHeight);

			BufferedImage runRightSheet = ImageIO.read(getClass().getResourceAsStream("/player/RunR.png"));
			BufferedImage runLeftSheet = ImageIO.read(getClass().getResourceAsStream("/player/RunL.png"));

			int runFrameWidth = runRightSheet.getWidth() / 8;
			int runFrameHeight = runRightSheet.getHeight();

			rr1 = runRightSheet.getSubimage(0 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr2 = runRightSheet.getSubimage(1 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr3 = runRightSheet.getSubimage(2 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr4 = runRightSheet.getSubimage(3 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr5 = runRightSheet.getSubimage(4 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr6 = runRightSheet.getSubimage(5 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr7 = runRightSheet.getSubimage(6 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rr8 = runRightSheet.getSubimage(7 * runFrameWidth, 0, runFrameWidth, runFrameHeight);

			rl1 = runLeftSheet.getSubimage(0 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl2 = runLeftSheet.getSubimage(1 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl3 = runLeftSheet.getSubimage(2 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl4 = runLeftSheet.getSubimage(3 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl5 = runLeftSheet.getSubimage(4 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl6 = runLeftSheet.getSubimage(5 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl7 = runLeftSheet.getSubimage(6 * runFrameWidth, 0, runFrameWidth, runFrameHeight);
			rl8 = runLeftSheet.getSubimage(7 * runFrameWidth, 0, runFrameWidth, runFrameHeight);

			BufferedImage idleRightSheet = ImageIO.read(getClass().getResourceAsStream("/player/IdleR.png"));
			BufferedImage idleLeftSheet = ImageIO.read(getClass().getResourceAsStream("/player/IdleL.png"));

			int idleFrameWidth = idleRightSheet.getWidth() / 8;
			int idleFrameHeight = idleRightSheet.getHeight();

			idr1 = idleRightSheet.getSubimage(0 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr2 = idleRightSheet.getSubimage(1 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr3 = idleRightSheet.getSubimage(2 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr4 = idleRightSheet.getSubimage(3 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr5 = idleRightSheet.getSubimage(4 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr6 = idleRightSheet.getSubimage(5 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr7 = idleRightSheet.getSubimage(6 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idr8 = idleRightSheet.getSubimage(7 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);

			idl1 = idleLeftSheet.getSubimage(0 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl2 = idleLeftSheet.getSubimage(1 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl3 = idleLeftSheet.getSubimage(2 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl4 = idleLeftSheet.getSubimage(3 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl5 = idleLeftSheet.getSubimage(4 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl6 = idleLeftSheet.getSubimage(5 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl7 = idleLeftSheet.getSubimage(6 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
			idl8 = idleLeftSheet.getSubimage(7 * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);

			BufferedImage atkR = ImageIO.read(getClass().getResourceAsStream("/player/Attack_1R.png"));
			BufferedImage atkL = ImageIO.read(getClass().getResourceAsStream("/player/Attack_1L.png"));

			int atkW = atkR.getWidth() / 7;
			int atkH = atkR.getHeight();

			atkR1 = atkR.getSubimage(0 * atkW, 0, atkW, atkH);
			atkR2 = atkR.getSubimage(1 * atkW, 0, atkW, atkH);
			atkR3 = atkR.getSubimage(2 * atkW, 0, atkW, atkH);
			atkR4 = atkR.getSubimage(3 * atkW, 0, atkW, atkH);
			atkR5 = atkR.getSubimage(4 * atkW, 0, atkW, atkH);
			atkR6 = atkR.getSubimage(5 * atkW, 0, atkW, atkH);
			atkR7 = atkR.getSubimage(6 * atkW, 0, atkW, atkH);

			atkL1 = atkL.getSubimage(0 * atkW, 0, atkW, atkH);
			atkL2 = atkL.getSubimage(1 * atkW, 0, atkW, atkH);
			atkL3 = atkL.getSubimage(2 * atkW, 0, atkW, atkH);
			atkL4 = atkL.getSubimage(3 * atkW, 0, atkW, atkH);
			atkL5 = atkL.getSubimage(4 * atkW, 0, atkW, atkH);
			atkL6 = atkL.getSubimage(5 * atkW, 0, atkW, atkH);
			atkL7 = atkL.getSubimage(6 * atkW, 0, atkW, atkH);

			BufferedImage deadRightSheet = ImageIO.read(getClass().getResourceAsStream("/player/DeadR.png"));
			BufferedImage deadLeftSheet = ImageIO.read(getClass().getResourceAsStream("/player/DeadL.png"));

			int deadW = deadRightSheet.getWidth() / 4;
			int deadH = deadRightSheet.getHeight();

			deadR1 = deadRightSheet.getSubimage(0 * deadW, 0, deadW, deadH);
			deadR2 = deadRightSheet.getSubimage(1 * deadW, 0, deadW, deadH);
			deadR3 = deadRightSheet.getSubimage(2 * deadW, 0, deadW, deadH);
			deadR4 = deadRightSheet.getSubimage(3 * deadW, 0, deadW, deadH);

			deadL1 = deadLeftSheet.getSubimage(0 * deadW, 0, deadW, deadH);
			deadL2 = deadLeftSheet.getSubimage(1 * deadW, 0, deadW, deadH);
			deadL3 = deadLeftSheet.getSubimage(2 * deadW, 0, deadW, deadH);
			deadL4 = deadLeftSheet.getSubimage(3 * deadW, 0, deadW, deadH);

			BufferedImage hurtR = ImageIO.read(getClass().getResourceAsStream("/player/HurtR.png"));
			BufferedImage hurtL = ImageIO.read(getClass().getResourceAsStream("/player/HurtL.png"));

			int hurtW = hurtR.getWidth() / 4;
			int hurtH = hurtR.getHeight();

			hurtR1 = hurtR.getSubimage(0 * hurtW, 0, hurtW, hurtH);
			hurtR2 = hurtR.getSubimage(1 * hurtW, 0, hurtW, hurtH);
			hurtR3 = hurtR.getSubimage(2 * hurtW, 0, hurtW, hurtH);
			hurtR4 = hurtR.getSubimage(3 * hurtW, 0, hurtW, hurtH);

			hurtL1 = hurtL.getSubimage(0 * hurtW, 0, hurtW, hurtH);
			hurtL2 = hurtL.getSubimage(1 * hurtW, 0, hurtW, hurtH);
			hurtL3 = hurtL.getSubimage(1 * hurtW, 0, hurtW, hurtH);
			hurtL4 = hurtL.getSubimage(3 * hurtW, 0, hurtW, hurtH);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {

		if (isDead) {
			deadCounter++;

			if (deadCounter > 15) {
				deadFrame++;
				deadCounter = 0;

				if (deadFrame > MAX_DEAD_FRAME) {
					deadFrame = MAX_DEAD_FRAME;
				}
			}
			return;
		}

		if (HP <= 0) {
			die();
			return;
		}

		if (knockback) {
			knockbackCounter++;

			int kbSpeed = knockbackPower;
			int dx = 0, dy = 0;

			switch (knockbackDirection) {
			case "left":
				dx = kbSpeed;
				break;
			case "right":
				dx = -kbSpeed;
				break;
			case "up":
				dy = kbSpeed;
				break;
			case "down":
				dy = -kbSpeed;
				break;
			}

			if (!collidesAtOffset(dx, dy, gp.currentMap)) {
				worldX += dx;
				worldY += dy;
			} else {

				knockback = false;
			}

			if (knockbackCounter > 10) {
				knockback = false;
			}

			return;
		}

		if (isHurt) {

			invincible = true;
			hurtCounter++;

			if (hurtCounter > 30) {
				hurtFrame++;
				hurtCounter = 0;

				if (hurtFrame > 4) {
					hurtFrame = 1;
					isHurt = false;
				}
			}
			return;
		}

		if (invincible) {
			invincibilityCounter++;
			if (invincibilityCounter > invincibleTime) {
				invincibilityCounter = 0;
				invincible = false;
			}
		}

		if (isAttacking) {

			attackCounter++;

			if (attackFrame == 4) {
				checkAttackHit();
			}

			if (attackCounter > 10) {
				attackFrame++;
				attackCounter = 0;

				if (attackFrame > 7) {
					attackFrame = 1;
					isAttacking = false;
				}
			}
			return;
		}

		int dx = 0;
		int dy = 0;

		if (keyH.upPressed) {
			dy -= speed;
			direction = "up";
		}
		if (keyH.downPressed) {
			dy += speed;
			direction = "down";
		}
		if (keyH.leftPressed) {
			dx -= speed;
			direction = "left";
			facingDirection = "left";
		}
		if (keyH.rightPressed) {
			dx += speed;
			direction = "right";
			facingDirection = "right";
		}

		updateStamina();

		boolean moving = (dx != 0 || dy != 0);
		isRunning = keyH.shiftPressed && moving && !staminaDepleted;

		speed = isRunning ? 2 : 1;

		if (keyH.upPressed)
			dy = -speed;
		if (keyH.downPressed)
			dy = speed;
		if (keyH.leftPressed)
			dx = -speed;
		if (keyH.rightPressed)
			dx = speed;

		if (dx != 0 || dy != 0) {
			moveWithCollision(dx, dy);

			spriteCounter++;

			if (isRunning) {
				if (spriteCounter > 20) {
					spriteNum++;
					spriteCounter = 0;
					if (spriteNum > 8)
						spriteNum = 1;
				}
			} else {
				if (spriteCounter > 20) {
					spriteNum++;
					spriteCounter = 0;
					if (spriteNum > 7)
						spriteNum = 1;
				}
			}

			idleCounter = 0;
			idleSpriteNum = 1;

		} else {
			idleCounter++;
			if (idleCounter > 45) {
				idleSpriteNum++;
				if (idleSpriteNum > 8)
					idleSpriteNum = 1;
				idleCounter = 0;
			}
			spriteNum = 1;
			spriteCounter = 0;
		}

		int feetY = worldY + solidArea.y + solidArea.height;
		depthY = feetY;

		if (gp.currentMap == null || gp.currentMap.getTileWidth() == 0 || gp.currentMap.getTileHeight() == 0)
			return;

		int tileX = (worldX + solidArea.x + solidArea.width / 2) / gp.currentMap.getTileWidth();
		int tileY = (worldY + solidArea.y + solidArea.height) / gp.currentMap.getTileHeight();

		if (!gp.warpEffect.teleporting) {
			if (gp.currentMapId == 0 && tileX == gp.warpX && tileY == gp.warpY) {

				// Object Interaction
				
				gp.warpEffect.triggerWarp(1);
				return;
			}

			if (gp.currentMapId == 1 && tileX == gp.warpBackX && tileY == gp.warpBackY) {

				gp.warpEffect.triggerWarp(0);
				return;
			}
		}
		
		if (gp.currentMapId != 1 && gp.boss != null) {
		    gp.boss.resetBoss();
		}
	}

	private void moveWithCollision(int dx, int dy) {
		TileManager2 tm = gp.currentMap;
		if (tm == null)
			return;

		if (dx != 0 && dy != 0) {
			if (!collidesAtOffset(dx, 0, tm))
				worldX += dx;
			if (!collidesAtOffset(0, dy, tm))
				worldY += dy;
		} else {
			if (!collidesAtOffset(dx, dy, tm)) {
				worldX += dx;
				worldY += dy;
			}
		}
	}

	private boolean collidesAtOffset(int ox, int oy, TileManager2 tm) {
		int left = worldX + ox + solidArea.x;
		int top = worldY + oy + solidArea.y;
		int right = left + solidArea.width - 1;
		int bottom = top + solidArea.height - 1;

		return tm.isSolidAtPixel(left, top) || tm.isSolidAtPixel(right, top) || tm.isSolidAtPixel(left, bottom)
				|| tm.isSolidAtPixel(right, bottom);
	}
	
	// Abstraction

	public void draw(Graphics2D g2, int cameraX, int cameraY, double zoom) {
		BufferedImage image = null;

		// Polymorphism
		
		if (isDead) {
			image = facingDirection.equals("left") ? getDeadLeftSprite() : getDeadRightSprite();
		} else if (isHurt) {
			image = facingDirection.equals("left") ? getHurtLeftSprite() : getHurtRightSprite();
		} else if (isAttacking) {
			image = facingDirection.equals("left") ? getAttackLeftSprite() : getAttackRightSprite();
		} else {
			boolean moving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;

			if (moving) {
				if (isRunning) {
					image = facingDirection.equals("left") ? getRunLeftSprite() : getRunRightSprite();
				} else {
					image = facingDirection.equals("left") ? getLeftSprite() : getRightSprite();
				}
			} else {
				image = facingDirection.equals("left") ? getIdleLeftSprite() : getIdleRightSprite();
			}
		}

		int feetX = worldX + solidArea.x + solidArea.width / 2;
		int feetY = worldY + solidArea.y + solidArea.height;

		int screenX = (int) ((feetX - cameraX) * zoom);
		int screenY = (int) ((feetY - cameraY) * zoom);

		int drawWidth = (int) (image.getWidth() * zoom);
		int drawHeight = (int) (image.getHeight() * zoom);

		screenX -= drawWidth / 2;
		screenY -= drawHeight;

		if (isHurt) {
			g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.9f));
		}

		g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);

		if (isHurt) {
			g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
		}

//	    // ✅ Debug hitbox
//	    g2.setColor(new Color(255, 0, 0, 120));
//	    g2.fillRect((int) ((worldX + solidArea.x - cameraX) * zoom),
//	                (int) ((worldY + solidArea.y - cameraY) * zoom),
//	                (int) (solidArea.width * zoom),
//	                (int) (solidArea.height * zoom));
//

	}

	private BufferedImage getRightSprite() {
		switch (spriteNum) {
		case 1:
			return r1;
		case 2:
			return r2;
		case 3:
			return r3;
		case 4:
			return r4;
		case 5:
			return r5;
		case 6:
			return r6;
		case 7:
			return r7;
		default:
			return r1;
		}
	}

	private BufferedImage getLeftSprite() {
		switch (spriteNum) {
		case 1:
			return l7;
		case 2:
			return l6;
		case 3:
			return l5;
		case 4:
			return l4;
		case 5:
			return l3;
		case 6:
			return l2;
		case 7:
			return l1;
		default:
			return l7;
		}
	}

	private BufferedImage getIdleRightSprite() {
		switch (idleSpriteNum) {
		case 1:
			return idr1;
		case 2:
			return idr2;
		case 3:
			return idr3;
		case 4:
			return idr4;
		case 5:
			return idr5;
		case 6:
			return idr6;
		case 7:
			return idr7;
		case 8:
			return idr8;
		default:
			return idr1;
		}
	}

	private BufferedImage getIdleLeftSprite() {
		switch (idleSpriteNum) {
		case 1:
			return idl8;
		case 2:
			return idl7;
		case 3:
			return idl6;
		case 4:
			return idl5;
		case 5:
			return idl4;
		case 6:
			return idl3;
		case 7:
			return idl2;
		case 8:
			return idl1;
		default:
			return idl8;
		}
	}

	private BufferedImage getRunRightSprite() {
		switch (spriteNum) {
		case 1:
			return rr1;
		case 2:
			return rr2;
		case 3:
			return rr3;
		case 4:
			return rr4;
		case 5:
			return rr5;
		case 6:
			return rr6;
		case 7:
			return rr7;
		case 8:
			return rr8;
		default:
			return rr1;
		}
	}

	private BufferedImage getRunLeftSprite() {
		switch (spriteNum) {
		case 1:
			return rl8;
		case 2:
			return rl7;
		case 3:
			return rl6;
		case 4:
			return rl5;
		case 5:
			return rl4;
		case 6:
			return rl3;
		case 7:
			return rl2;
		case 8:
			return rl1;
		default:
			return rl8;
		}
	}

	private BufferedImage getAttackRightSprite() {
		switch (attackFrame) {
		case 1:
			return atkR1;
		case 2:
			return atkR2;
		case 3:
			return atkR3;
		case 4:
			return atkR4;
		case 5:
			return atkR5;
		case 6:
			return atkR6;
		case 7:
			return atkR7;
		default:
			return atkR1;
		}
	}

	private BufferedImage getAttackLeftSprite() {
		switch (attackFrame) {
		case 1:
			return atkL7;
		case 2:
			return atkL6;
		case 3:
			return atkL5;
		case 4:
			return atkL4;
		case 5:
			return atkL3;
		case 6:
			return atkL2;
		case 7:
			return atkL1;
		default:
			return atkL7;
		}
	}

	private BufferedImage getDeadRightSprite() {
		switch (deadFrame) {
		case 1:
			return deadR1;
		case 2:
			return deadR2;
		case 3:
			return deadR3;
		case 4:
			return deadR4;
		}
		return deadR4;
	}

	private BufferedImage getDeadLeftSprite() {
		switch (deadFrame) {
		case 1:
			return deadL4;
		case 2:
			return deadL3;
		case 3:
			return deadL2;
		case 4:
			return deadL1;
		}
		return deadL1;
	}

	private BufferedImage getHurtRightSprite() {
		switch (hurtFrame) {
		case 1:
			return hurtR1;
		case 2:
			return hurtR2;
		case 3:
			return hurtR3;
		case 4:
			return hurtR4;
		default:
			return hurtR1;
		}
	}

	private BufferedImage getHurtLeftSprite() {
		switch (hurtFrame) {
		case 1:
			return hurtL4;
		case 2:
			return hurtL3;
		case 3:
			return hurtL2;
		case 4:
			return hurtL1;
		default:
			return hurtL4;
		}
	}

	public BufferedImage getCurrentSprite() {
		boolean moving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;
		if (moving)
			return facingDirection.equals("left") ? getLeftSprite() : getRightSprite();
		return facingDirection.equals("left") ? getIdleLeftSprite() : getIdleRightSprite();
	}

	public boolean checkOnRoof(TileManager2 tm) {
		int px = worldX + solidArea.x + solidArea.width / 2;
		int py = worldY + solidArea.y + solidArea.height;

		return tm.isTopTileAtPixel(px, py);
	}

	public void moveToSafeSpot() {
		TileManager2 tm = gp.currentMap;

		while (tm.isSolidAtPixel(worldX, worldY + solidArea.height)) {
			worldY += gp.tileSize;
		}
	}

	public void startAttack() {
		if (!isAttacking) {
			isAttacking = true;
			attackFrame = 1;
			attackCounter = 0;
		}
	}

	public void takeDamage(int dmg) {
		if (isDead || invincible)
			return;

		HP -= dmg;
		isHurt = true;
		hurtFrame = 1;
		hurtCounter = 0;

		if (HP <= 0) {
			HP = 0;
			die();
		}
	}

	public void takeDamage(int dmg, String atkDirection) {
		if (isDead || invincible)
			return;

		HP -= dmg;
		isHurt = true;
		hurtFrame = 1;
		hurtCounter = 0;

		knockback = true;
		knockbackCounter = 0;
		knockbackDirection = atkDirection;

		if (HP <= 0) {
			HP = 0;
			die();
		}

		invincible = true;
		invincibilityCounter = 0;
	}

	public void takeDamageFrom(String dir, int dmg, boolean knockback, boolean burn, int burnFrames) {

		if (isDead || invincible)
			return;

		HP -= dmg;
		isHurt = true;
		hurtFrame = 1;
		hurtCounter = 0;

		if (dir != null)
			facingDirection = dir;

		if (knockback) {
			final int kbX;
			final int kbY;

			switch (dir) {
			case "left":
				kbX = 5;
				kbY = 0;
				break;
			case "right":
				kbX = -5;
				kbY = 0;
				break;
			case "up":
				kbX = 0;
				kbY = 5;
				break;
			case "down":
				kbX = 0;
				kbY = -5;
				break;
			default:
				kbX = 0;
				kbY = 0;
				break;
			}

			new Thread(() -> {
				for (int i = 0; i < 6; i++) {
					moveWithCollision(kbX, kbY);
					try {
						Thread.sleep(10);
					} catch (Exception ignored) {
					}
				}
			}).start();
		}

		if (burn) {
			new Thread(() -> {
				int t = burnFrames;
				while (t > 0 && !isDead) {
					HP -= 1;
					try {
						Thread.sleep(1000);
					} catch (Exception ignored) {
					}
					t--;
				}
				if (HP <= 0)
					die();
			}).start();
		}

		invincible = true;
		invincibilityCounter = 0;

		if (HP <= 0) {
			HP = 0;
			die();
		}
	}

	public void die() {

	    level -= 3;
	    if (level < 1) level = 1;
	    maxXP = basemaxXP;
	    XP = baseXP;


	    gp.currentMapId = 0;
	    gp.currentMap = gp.mapVillage;

	    worldX = gp.warpReturnX;
	    worldY = gp.warpReturnY;

	    maxHP = baseHP;
	    HP = maxHP;
	    
	    maxMana = baseMana;
	    mana = maxMana;
	    
	    maxStamina = baseStamina;
	    stamina = maxStamina;

	    isDead = false;
	    isHurt = false;
	    isAttacking = false;
	    knockback = false;
	    deadCounter = 0;
	    deadFrame = 1;

	    invincible = true;
	    invincibilityCounter = 0;

	    if (gp.boss != null) {
	        gp.boss.scaleWithPlayer(level);
	    }
	}

	private void checkAttackHit() {

	    if (gp.currentMapId != 1) return;

	    Boss boss = gp.boss;
	    if (boss == null || !boss.isAlive()) return;

	    Rectangle atkArea = new Rectangle(
	            worldX + solidArea.x,
	            worldY + solidArea.y,
	            solidArea.width,
	            solidArea.height
	    );

	    if (facingDirection.equals("right")) atkArea.x += 30;
	    if (facingDirection.equals("left")) atkArea.x -= 30;
	    if (facingDirection.equals("up")) atkArea.y -= 30;
	    if (facingDirection.equals("down")) atkArea.y += 30;

	    // Boss Hitbox
	    Rectangle bossHit = new Rectangle(
	            boss.worldX + boss.solidArea.x,
	            boss.worldY + boss.solidArea.y,
	            boss.solidArea.width,
	            boss.solidArea.height
	    );

	    // Object Interaction
	    
	    if (atkArea.intersects(bossHit)) {
	        boss.takeDamage(10);
	    }
	}

	// GAIN REWARDS
	public void gainGold(int amount) {
		gold += amount;
	}

	public void gainXP(int amount) {
		XP += amount;

		// Level Up!
		while (XP >= maxXP) {
			levelUp();
			if (gp.boss != null) {
			    gp.boss.onHurt();
			}
		}
	}

	private void levelUp() {
	    level++;
	    XP -= maxXP;
	    maxXP += 20;

	    // Reset level (30)
	    if (level >= 30) {
	        level = 0;
	        XP = 0;
	        maxXP = 100;
	    }

	    // STATUS UP BY LEVEL
	    if (level > 0) {
	        maxHP += 15;
	        maxMana += 10;
	        maxStamina += 10;

	        // RE
	        HP = maxHP;
	        mana = maxMana;
	        stamina = maxStamina;

	        // BOSS UP BY LAVEL
	        if (gp.boss != null) {
	            gp.boss.scaleWithPlayer(level);
	        }
	    }
	}


	// RUN STAMINA SYSTEM
	private void updateStamina() {
		if (isRunning) {
			// USE STAMINA RUN
			stamina -= 1;
			if (stamina <= 0) {
				stamina = 0;
				staminaDepleted = true;
				isRunning = false;
			}
		} else {
			// RE-STAMINA (NOT RUN)
			if (stamina < maxStamina) {
				stamina += 1;
			}

			// 
			if (stamina >= maxStamina * 0.1) {
				staminaDepleted = false;
			}
		}
	}

	// GETTERS
	public int getHP() {
		return HP;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public int getMana() {
		return mana;
	}

	public int getMaxMana() {
		return maxMana;
	}

	public int getStamina() {
		return stamina;
	}

	public int getMaxStamina() {
		return maxStamina;
	}

	public int getXP() {
		return XP;
	}

	public int getMaxXP() {
		return maxXP;
	}

	public int getLevel() {
		return level;
	}

	public int getGold() {
		return gold;
	}
}
