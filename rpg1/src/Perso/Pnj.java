package Perso;
import java.util.ArrayList;

import Sprite.Animation;


public class Pnj {
	
	private String message;
	private long decalageMarche;	//timeMarche
	private String nom;
	private ArrayList<Animation> animation;
	private int index;
	private int x;
	private int y;
	private long intervalMarche;	//timeMarcheNpc
	private long tempsMarche;
	
	
	public Pnj (int x, int y, int imageParEtat, String[][] img, long intervalMarche,
				long decalageMarche, int index, String message, String nom, float UNIT) {
		this.x = x;
		this.y = y;
		this.intervalMarche = intervalMarche;
		this.decalageMarche = decalageMarche;
		this.index = index;
		this.message = message;
		this.nom = nom;
		this.setTempsMarche(System.currentTimeMillis() + decalageMarche);
		
		animation = new ArrayList<Animation>();
		for(int i = 0; i<4; i++) {
			animation.add(new Animation(img[i], 400/(imageParEtat), x*UNIT, y*UNIT, 0, 0));
		}
	}
	
	public String toString() {
		return "[" + x + "," + y + "] - " + nom + " - " + index;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getDecalageMarche() {
		return decalageMarche;
	}
	public void setDecalageMarche(long decalageMarche) {
		this.decalageMarche = decalageMarche;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public Animation getAnimation(int i) {
		return (Animation)( animation.get(i) );
	}
	public ArrayList<Animation> getAnimation() {
		return animation;
	}
	public void setAnimation(ArrayList<Animation> animation) {
		this.animation = animation;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public long getIntervalMarche() {
		return intervalMarche;
	}
	public void setIntervalMarche(long intervalMarche) {
		this.intervalMarche = intervalMarche;
	}

	public long getTempsMarche() {
		return tempsMarche;
	}

	public void setTempsMarche(long tempsMarche) {
		this.tempsMarche = tempsMarche;
	}
}
