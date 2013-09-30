
public class Message {
	
	private String message;
	private long tempsAffichage;
	private long validite;
	private int coordX ;
	private int coordY;
	private String bg = null;
	private int bgCoordX;
	private int bgCoordY;
	
	public Message() {
		tempsAffichage = 2000;
		message = "";
		validite = 0;
		setCoordX(200);
		setCoordY(200);
	}
	
	public Message(String mess) {
		this.message = mess;
		tempsAffichage = 2000;
	}
	
	public Message(String mess, long time, int x, int y , String bg, int bgX, int bgY) {
		this.message = mess;
		this.tempsAffichage = time;
		coordX = x;
		coordY = y;
		this.bg = bg;
		bgCoordX = bgX;
		bgCoordY = bgY;
	}
	
	public Message(String mess, long time) {
		this.message = mess;
		this.tempsAffichage = time;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public long getTime() {
		return tempsAffichage;
	}

	public void setTime(long time) {
		this.tempsAffichage = time;
	}

	public long getValidite() {
		return validite;
	}

	public void setValidite(long tempsActuel) {
		this.validite = tempsActuel + tempsAffichage;
	}

	public int getCoordX() {
		return coordX;
	}

	public void setCoordX(int coordX) {
		this.coordX = coordX;
	}

	public int getCoordY() {
		return coordY;
	}

	public void setCoordY(int coordY) {
		this.coordY = coordY;
	}

	public String getBg() {
		return bg;
	}

	public void setBg(String bg) {
		this.bg = bg;
	}

	public int getBgCoordX() {
		return bgCoordX;
	}

	public void setBgCoordX(int bgCoordX) {
		this.bgCoordX = bgCoordX;
	}

	public int getBgCoordY() {
		return bgCoordY;
	}

	public void setBgCoordY(int bgCoordY) {
		this.bgCoordY = bgCoordY;
	}
}
