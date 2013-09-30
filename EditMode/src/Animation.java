import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * Classe principale donnant un exemple d'animation
 */
public class Animation {
	
	private Sprite sprite;		//sprite gerant le deplacement de l'animation
	private Images a;		//animation
	
	
	//initialisation de l'ecran et des composants necessaire
	public void init(String[] fichiers, long frameSpeed, float x, float y, float vx, float vy) {
		try{
			loadImages(fichiers, frameSpeed, x, y, vx, vy);
		}
		catch(Exception e) {}
	}
	
	public Animation(String[] fichiers, long frameSpeed, float x, float y, float vx, float vy) {
		init(fichiers, frameSpeed, x, y, vx, vy);
		
	}
	
	//charge toutes les images nécessaire (bg + images de l'animation)
	//et initialise l'animation et le sprite
	public void loadImages(String[] fichiers, long frameSpeed, float x, float y, float vx, float vy) {
		//chargement des images
		ArrayList<Image> img = new ArrayList<Image>();
		for(int i = 0; i<fichiers.length; i++){
			img.add(new ImageIcon(fichiers[i]).getImage());
		}
		
		//initialisation de l'animation
		a = new Images();
		for(int i = 0; i<img.size(); i++) {
			a.addScene(img.get(i), frameSpeed);
		}
		
		//initialisation du sprite
		sprite = new Sprite(a);
		sprite.setVelocityX(vx);
		sprite.setVelocityY(vy);
		sprite.setX(x);
		sprite.setY(y);
		
	}
	
	//mise a jour de la velocite (velocite negative => sens inverse)
	public void update(long timePassed) {
		//si on depace de l'ecran, faire le chemin inverse
		 /*if(sprite.getX() < 0) {
			 sprite.setVelocityX(Math.abs(sprite.getVelocityX()));
		 }
		 else if(sprite.getX() + sprite.getWidth() >= s.getWidth()) {
			 sprite.setVelocityX(-Math.abs(sprite.getVelocityX()));
		 }
		 
		 if(sprite.getY() < 0) {
			 sprite.setVelocityY(Math.abs(sprite.getVelocityY()));
		 }
		 else if(sprite.getY() + sprite.getHeight() >= s.getHeight()) {
			 sprite.setVelocityY(-Math.abs(sprite.getVelocityY()));
		 }
		 */
		 //mise a jour du sprite
		 sprite.update(timePassed);
	}
	
	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}
	
	public void setVelocityX(float vx) {
		sprite.setVelocityX(vx);
	}
	
	public void setVelocityY(float vy) {
		sprite.setVelocityY(vy);
	}
	
	public float getVelocityX() {
		return sprite.getVelocityX();
	}
	
	public float getVelocityY() {
		return sprite.getVelocityY();
	}
	
	public void setX(float x) {
		sprite.setX(x);
	}
	
	public void setY(float y) {
		sprite.setY(y);
	}
	
	public void resetVelocity() {
		sprite.setVelocityX(0);
		sprite.setVelocityY(0);
	}

	public float getX() {
		return sprite.getX();
	}
	
	public float getY() {
		return sprite.getY();
	}

	public boolean getPaused() {
		return sprite.getPaused();
	}

	public void setPaused(boolean paused) {
		sprite.setPaused(paused);
	}
	
}
