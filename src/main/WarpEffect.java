package main;

import java.awt.Color;
import java.awt.Graphics2D;

public class WarpEffect {

    private GamePanel gp;

    private float alpha = 0f;
    private boolean fadingOut = false;
    private boolean fadingIn = false;
    private int nextMapID;

    public WarpEffect(GamePanel gp) {
        this.gp = gp;
    }
    
    public boolean teleporting = false;

    public void triggerWarp(int targetMapID) {
        if (teleporting) return; // ✅ กัน warp ซ้ำ
        teleporting = true;
        nextMapID = targetMapID;
        fadingOut = true;
        alpha = 0f;
    }

    public void update() {
    	
    	float speed = 0.02f; //

        if (fadingOut) {
            alpha += speed;
            if (alpha >= 1f) {
                alpha = 1f;
                fadingOut = false;
                gp.doWarp(nextMapID);

                fadingIn = true;
            }
        } else if (fadingIn) {
            alpha -= speed;
            if (alpha <= 0f) {
                alpha = 0f;
                fadingIn = false;
            }
        }
        
        if (!fadingOut && !fadingIn) {
            teleporting = false; // ✅ reset เมื่อ fade เสร็จ
        }
    }

    public void draw(Graphics2D g, int screenW, int screenH) {
        if (!fadingOut && !fadingIn) return;

        g.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, alpha));

        g.setColor(Color.black);
        g.fillRect(0, 0, screenW, screenH);

        g.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 1f));
    }

    public boolean isWarping() {
        return fadingOut || fadingIn;
    }
}
