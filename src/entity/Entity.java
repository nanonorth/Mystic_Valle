package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {

	public int worldX, worldY;
	public int speed;
	
	public Rectangle solidArea;
	
	// WALK
	public BufferedImage r1, r2, r3, r4, r5, r6, r7;
	public BufferedImage l1, l2, l3, l4, l5, l6, l7;
	
	// IDEL
	public BufferedImage idr1, idr2, idr3, idr4, idr5, idr6, idr7, idr8;
	public BufferedImage idl1, idl2, idl3, idl4, idl5, idl6, idl7, idl8;
	public String direction;
	
	// RUN ANIMATION
	BufferedImage rr1, rr2, rr3, rr4, rr5, rr6, rr7, rr8;
	BufferedImage rl1, rl2, rl3, rl4, rl5, rl6, rl7, rl8;

	public boolean isRunning = false;
	
	public int spriteCounter = 0;
	public int spriteNum = 1;
	
	// Attack Flags
	public boolean isAttacking = false;
	private int attackCounter = 0;
	private int attackFrame = 1;

	// Attack Frames (8 frames)
	BufferedImage atkR1,atkR2,atkR3,atkR4,atkR5,atkR6,atkR7;
	BufferedImage atkL1,atkL2,atkL3,atkL4,atkL5,atkL6,atkL7;

	// Sprites
	BufferedImage deadR1, deadR2, deadR3, deadR4;
	BufferedImage deadL1, deadL2, deadL3, deadL4;
	
	// Hurt
	BufferedImage hurtR1, hurtR2, hurtR3, hurtR4;
	BufferedImage hurtL1, hurtL2, hurtL3, hurtL4;
	
}