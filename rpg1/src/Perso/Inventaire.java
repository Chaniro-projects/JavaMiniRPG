package Perso;
import java.util.ArrayList;

import Objet.Objet;



public class Inventaire {
	
	private ArrayList<Objet> o;
	private int x;
	private int y;
	
	
	public Inventaire(int x, int y) {
		this.x = x;
		this.y = y;
		o = new ArrayList<Objet>();
		
		for(int i = 0; i<x; i++) {
			for(int j = 0; j<y; j++) {
				o.add(new Objet(""));
				
			}
		}
	}

	
}
