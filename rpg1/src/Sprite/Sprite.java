package Sprite;
import java.awt.Image;


public class Sprite {

	private Images a;
	private float x;
	private float y;
	private float vx;
	private float vy;
	private boolean paused;
	
	public Sprite(Images a) {
		this.a = a;
		paused = true;
	}
	
	public void update(long timePassed) {
		x += vx * timePassed;
		y += vy * timePassed;
		a.update(timePassed);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public int getWidth() {
		return a.getImage().getWidth(null);
	}
	
	public int getHeight() {
		return a.getImage().getHeight(null);
	}
	
	public float getVelocityX() {
		return vx;
	}
	
	public float getVelocityY() {
		return vy;
	}
	
	public void setVelocityX(float vx) {
		this.vx = vx;
	}
	
	public void setVelocityY(float vy) {
		this.vy = vy;
	}
	
	
	public Image getImage() {
		if(!paused)
			return a.getImage();
		else
			return a.getImage(0);
	}

	public boolean getPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
}
