package Perso;


public class Hero extends Pnj {

	private int sante;
	private int santeMax;
	private Inventaire i;
	private int level;
	
	public Hero(int x, int y, int imageParEtat, String[][] img,long intervalMarche, long decalageMarche, int index,String message, String nom, float UNIT) {

		super(x, y, imageParEtat, img, intervalMarche, decalageMarche, index, message, nom, UNIT);
		
		santeMax = 3;
		setSante(3);
		setLevel(1);
		
		i = new Inventaire(4, 3);
	}

	
	public int getSante() {
		return sante;
	}

	public void setSante(int sante) {
		this.sante= sante;
		if(this.sante < 0)
			this.sante = 0;
		if(this.sante > santeMax)
			this.sante = santeMax;
			
	}

	public void enleverSanter(int nb) {
		setSante(sante - nb);
	}

	public void ajouterSanter(int nb) {
		setSante(sante + nb);
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}

}
