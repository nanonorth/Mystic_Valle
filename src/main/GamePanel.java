//GamePanel
package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import entity.Boss;
import entity.Fireball;
import entity.Player;
import tile.TileManager2;

public class GamePanel extends JPanel implements Runnable {

	// ===== GAME STATES =====
	public static final int STATE_TITLE = 0;
	public static final int STATE_PLAY = 1;
	public static final int STATE_PAUSE = 2;

	BufferedImage pauseBG;

	public int gameState = STATE_PAUSE; // ✅ เริ่มด้วย Pause UI

	KeyHandler keyH;

	final int originalTileSize = 16;
	final int scale = 4;

	public final int tileSize = originalTileSize * scale;
	final int maxScreenCol = 16;
	final int maxScreenRow = 12;
	final int screenWidth = tileSize * maxScreenCol;
	final int screenHeight = tileSize * maxScreenRow;

	int FPS = 120;

	public TileManager2 mapVillage;
	public TileManager2 mapDungeon;
	public TileManager2 currentMap;

	public int currentMapId = 0;

	public Player player;

	public Boss boss;

	public WarpEffect warpEffect = new WarpEffect(this);

	Thread gameThread;

	public boolean showCollisionDebug = false;
	public double zoomFactor = 2.0;

	public int cameraX = 0;
	public int cameraY = 0;

	public int warpXS = 220;
	public int warpYS = 165;

	public int warpX = 92;
	public int warpY = 12;

	public int warpBackX = 3;
	public int warpBackY = 13;

	public int warpReturnX = 1330;
	public int warpReturnY = 10;

	public boolean bossFightActive = false;
	public boolean bossDefeated = false;

	public ArrayList<Fireball> fireballs = new ArrayList<>();

	public GamePanel() {

		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setBackground(Color.black);
		setDoubleBuffered(true);

		// ✅ สร้าง KeyHandler ก่อนใช้
		keyH = new KeyHandler(this);
		addKeyListener(keyH);
		setFocusable(true);

		mapVillage = new TileManager2("res/map/Map_V1.tmj");
		mapDungeon = new TileManager2("res/map/Map_D1.tmj");

		try {
			pauseBG = ImageIO.read(new File("res/UI/CHAT1.png"));
			System.out.println("Pause image loaded!");
		} catch (Exception e) {
			System.out.println("ERROR: CHAT1.png not found!");
		}

		currentMap = mapVillage;
		currentMapId = 0;

		// ✅ Player ต้องสร้างหลังจาก keyH ถูกสร้างแล้ว
		player = new Player(this, keyH);
		player.setSpawnPoint(warpXS, warpYS);

		// ✅ Mouse attack พร้อม keyH แล้ว
		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				if (!player.isAttacking && !player.isDead) {
					player.startAttack();
				}
			}
		});
	}

	public void drawPlayerUI(Graphics2D g2) {
		int barWidth = 260;
		int barHeight = 16;
		int spacing = 8;

		// ✅ วาง UI ล่างตรงกลาง
		int barX = screenWidth / 2 - barWidth / 2;
		int barY = screenHeight - 90;

		// HP BAR
		drawBar(g2, barX, barY, barWidth, barHeight, player.getHP(), player.getMaxHP(), new Color(200, 0, 0),
				new Color(60, 0, 0), "HP");

		// MANA BAR
		barY += barHeight + spacing;
		drawBar(g2, barX, barY, barWidth, barHeight, player.getMana(), player.getMaxMana(), new Color(0, 100, 255),
				new Color(0, 30, 80), "MANA");

		// STAMINA BAR
		barY += barHeight + spacing;
		drawBar(g2, barX, barY, barWidth, barHeight, player.getStamina(), player.getMaxStamina(), new Color(0, 200, 0),
				new Color(0, 60, 0), "STAMINA");

		// XP BAR
		barY += barHeight + spacing;
		drawBar(g2, barX, barY, barWidth, barHeight, player.getXP(), player.getMaxXP(), new Color(255, 200, 0),
				new Color(80, 60, 0), "XP");

		// LEVEL & GOLD
		g2.setColor(Color.WHITE);
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
		g2.drawString("Lv." + player.getLevel(), barX - 80, barY + 12);

		g2.setColor(new Color(255, 215, 0));
		g2.drawString("Gold: " + player.getGold(), barX + barWidth + 20, barY + 12);
	}

	private void drawBar(Graphics2D g2, int x, int y, int width, int height, int current, int max, Color fillColor,
			Color bgColor, String label) {

// Background
		g2.setColor(bgColor);
		g2.fillRoundRect(x, y, width, height, 8, 8);

// Fill
		double percent = Math.max(0, Math.min(1, (double) current / max));
		int fillWidth = (int) (width * percent);
		g2.setColor(fillColor);
		g2.fillRoundRect(x, y, fillWidth, height, 8, 8);

// Border
		g2.setColor(Color.WHITE);
		g2.drawRoundRect(x, y, width, height, 8, 8);

// Center Text
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
		String text = label + ": " + current + "/" + max;
		FontMetrics fm = g2.getFontMetrics();

		int textX = x + (width - fm.stringWidth(text)) / 2;
		int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

		g2.setColor(Color.BLACK);
		g2.drawString(text, textX + 1, textY + 1);
		g2.setColor(Color.WHITE);
		g2.drawString(text, textX, textY);
	}

	public void doWarp(int targetMapID) {

		if (targetMapID == currentMapId)
			return;
		currentMapId = targetMapID;

		if (currentMapId == 1) {
			currentMap = mapDungeon;

			int sx = 300;
			int sy = 200;
			player.setSpawnPoint(sx, sy);
			player.moveToSafeSpot();

			// ✅ เริ่ม Boss Fight ใหม่ทุกครั้งที่เข้าดันเจี้ยน
			bossFightActive = true;

			if (bossDefeated) {
				// ✅ ถ้าบอสเคยตายแล้ว → ไม่ต้องเกิดใหม่
				boss = null;
			} else if (boss == null) {
				boss = new Boss(this, fireballs);
				boss.worldX = 900;
				boss.worldY = 80;
			}

		}  else {
		    currentMap = mapVillage;
		    bossFightActive = false;

		    // ✅ Respawn Player ที่หมู่บ้าน
		    player.setSpawnPoint(warpReturnX, warpReturnY);
		    player.moveToSafeSpot();

		    // ✅ Reset Boss เมื่อออกจากดันเจี้ยน
		    if (boss != null) {
		        boss.resetBoss();
		        boss.fireballs.clear();
		        
		        // ✅ ส่งกลับตำแหน่งสปาวน์บอส
		        boss.worldX = 900;
		        boss.worldY = 80;
		    }
		}
	}

	public void startGameThread() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public void run() {

		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;

		while (gameThread != null) {

			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if (delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}

	public void update() {
		player.update();
		updateCamera();
		warpEffect.update();

		if (boss != null && boss.isAlive()) {
			boss.update();
		}
		if (boss != null) {
			for (int i = 0; i < boss.fireballs.size(); i++) {
				boss.fireballs.get(i).update();
				if (!boss.fireballs.get(i).alive) {
					boss.fireballs.remove(i);
					i--;
				}
				Fireball fb = boss.fireballs.get(i);
				fb.update();

				// ✅ Fireball hit player
				if (fb.hitPlayer(player)) {
					// Damage from Fireball handled in Player.takeDamageFrom()
					boss.fireballs.remove(i);
					i--;
					continue;
				}
			}
		}
	}

	public void updateCamera() {

		// คำนวณกลางตัว player
		cameraX = (int) (player.worldX + player.solidArea.x + player.solidArea.width / 2
				- screenWidth / (2 * zoomFactor));
		cameraY = (int) (player.worldY + player.solidArea.y + player.solidArea.height / 2
				- screenHeight / (2 * zoomFactor));

		if (currentMap != null) {

			int mapW = currentMap.getMapWidth() * currentMap.getTileWidth();
			int mapH = currentMap.getMapHeight() * currentMap.getTileHeight();

			int viewW = (int) (screenWidth / zoomFactor);
			int viewH = (int) (screenHeight / zoomFactor);

			// ✅ Clamp หลังคำนวณ Scale ของกล้อง
			cameraX = Math.max(0, Math.min(cameraX, mapW - viewW));
			cameraY = Math.max(0, Math.min(cameraY, mapH - viewH));
		}
	}

//	public void drawPauseUI(Graphics2D g2) {
//        g2.setColor(new Color(0, 0, 0, 200));
//        g2.fillRect(0, 0, screenWidth, screenHeight);
//
//        if (pauseBG != null) {
//            int imgW = pauseBG.getWidth();
//            int imgH = pauseBG.getHeight();
//            int x = (screenWidth - imgW) / 2;
//            int y = (screenHeight - imgH) / 2;
//            g2.drawImage(pauseBG, x, y, null);
//        }
//
//        g2.setColor(Color.WHITE);
//        g2.setFont(g2.getFont().deriveFont(40f));
//        g2.drawString("Press ENTER to Start",
//            screenWidth/2 - 180,
//            (screenHeight/2) - 160);
//    }

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		if (currentMap != null) {
			currentMap.render(g2, cameraX, cameraY, screenWidth, screenHeight, zoomFactor, player);
			// ทุกอย่างเกี่ยวกับ map / boss / fireball อยู่ในนี้

			// ✅ Debug warp tile for village only
			if (currentMapId == 0) {
				currentMap.drawDebugWarp(g2, cameraX, cameraY, zoomFactor, warpX, warpY);
			}

			// Warp กลับ (แสดงเฉพาะใน Dungeon)
			if (currentMapId == 1) {
				currentMap.drawDebugWarp(g2, cameraX, cameraY, zoomFactor, warpBackX, warpBackY);
			}

			// ✅ collision debug last (ถ้าต้องการเปิดใช้)
			// if (currentMapId == 1 && (boss == null || !boss.isAlive())) {
			// currentMap.drawCollisionDebug(g2,
			// cameraX, cameraY, screenWidth, screenHeight, zoomFactor);
			// }

			if (warpEffect.isWarping()) {
				warpEffect.draw(g2, screenWidth, screenHeight);
			}

			if (currentMapId == 1 && boss != null && boss.isAlive()) {
			    boss.draw(g2, cameraX, cameraY, zoomFactor);

			    for (Fireball f : boss.fireballs) {
			        f.draw(g2, cameraX, cameraY, zoomFactor);
			    }
			}

			// ✅ วาดบอส + กระสุนเฉพาะตอนสู้
			if (bossFightActive && boss != null && boss.isAlive()) {
				boss.draw(g2, cameraX, cameraY, zoomFactor);

				for (Fireball f : boss.fireballs) {
					f.draw(g2, cameraX, cameraY, zoomFactor);
				}
			}

			// ==== วาด UI สุดท้าย ====
			drawPlayerUI(g2);

			// ====== Boss HP Bar ======
			if (currentMapId == 1 && bossFightActive && boss != null && boss.isAlive()) {
			    int barWidth = screenWidth - 200;
			    int barHeight = 26;

				// Pixel Block Style Font
				g2.setFont(new Font("Monospaced", Font.BOLD, 24));


				int x = 100;
				int y = 20;

				// Fill percent
				double percent = Math.max(0, Math.min(1, boss.HP / (double) boss.maxHP));
				int fillWidth = (int) (barWidth * percent);

				// Background
				g2.setColor(new Color(80, 0, 0));
				g2.fillRoundRect(x, y, barWidth, barHeight, 12, 12);

				// Red fill
				g2.setColor(Color.RED);
				g2.fillRoundRect(x, y, fillWidth, barHeight, 12, 12);

				// White Border
				g2.setColor(Color.WHITE);
				g2.drawRoundRect(x, y, barWidth, barHeight, 12, 12);

				// Boss Name (center)
				String bossName = "KIT S U N A";
				FontMetrics fm = g2.getFontMetrics();
				int textX = x + (barWidth - fm.stringWidth(bossName)) / 2;
				int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();

				g2.setColor(Color.BLACK);
				g2.drawString(bossName, textX + 2, textY + 2);

				g2.setColor(Color.WHITE);
				g2.drawString(bossName, textX, textY);
			}
		}
	}
}