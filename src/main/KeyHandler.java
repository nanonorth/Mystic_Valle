package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

	GamePanel gp;

	public boolean upPressed, downPressed, leftPressed, rightPressed;
	public boolean shiftPressed; // ✅ เพิ่มคำสั่งกดวิ่ง

	public KeyHandler(GamePanel gp) {
		this.gp = gp;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {

		int code = e.getKeyCode();

		if (code == KeyEvent.VK_W) {
			upPressed = true;
		}
		if (code == KeyEvent.VK_S) {
			downPressed = true;
		}
		if (code == KeyEvent.VK_A) {
			leftPressed = true;
		}
		if (code == KeyEvent.VK_D) {
			rightPressed = true;
		}
		if (code == KeyEvent.VK_SHIFT) {
			shiftPressed = true;
		}
		if (code == KeyEvent.VK_J) {
			gp.player.takeDamage(20);
		}
		if (code == KeyEvent.VK_ENTER) {
		    if (gp.gameState == GamePanel.STATE_PAUSE) {
		        gp.gameState = GamePanel.STATE_PLAY;
		        System.out.println("ENTER -> STATE_PLAY");
		    }
		}

		if (code == KeyEvent.VK_ESCAPE) {
		    if (gp.gameState == GamePanel.STATE_PLAY) {
		        gp.gameState = GamePanel.STATE_PAUSE;
		        System.out.println("ESC -> STATE_PAUSE");
		    } 
		    else if (gp.gameState == GamePanel.STATE_PAUSE) {
		        gp.gameState = GamePanel.STATE_PLAY;
		        System.out.println("ESC -> STATE_PLAY");
		    }
		}

	}
	

	@Override
	public void keyReleased(KeyEvent e) {

		int code = e.getKeyCode();

		if (code == KeyEvent.VK_W) {
			upPressed = false;
		}
		if (code == KeyEvent.VK_S) {
			downPressed = false;
		}
		if (code == KeyEvent.VK_A) {
			leftPressed = false;
		}
		if (code == KeyEvent.VK_D) {
			rightPressed = false;
		}
		if (code == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}
	}
}
