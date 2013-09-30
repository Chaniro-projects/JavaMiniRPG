/**
 * @author Bastien Baret
 */
package Principal;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/*
 * Class gerant un case de la map
 */
public class Case {
		private boolean marche;
		private boolean action;
		private Message mess;
		private Image image;
		private String fichier;
		private String caractere;

		
		public Case(Case c) {
			this.marche = c.marche;
			this.action = c.action;
			this.mess = c.mess;
			try {
				this.image = ImageIO.read(new File(c.fichier));
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.fichier = c.fichier;
			this.caractere = c.caractere;
		}
		
		public Case(String fichier, boolean marche, boolean action, String mess) {
			this.mess = new Message();
			this.marche = marche;
			this.action = action;
			this.mess.setMessage(mess);
			this.setFichier(fichier);
			try {
				this.image = ImageIO.read(new File(fichier));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Case(String fichier, boolean marche, boolean action, Message mess) {
			this.mess = mess;
			this.marche = marche;
			this.action = action;
			try {
				this.image = ImageIO.read(new File(fichier));
			} catch (IOException e) {}
		}
			
		public String getMessageString() {
			return mess.getMessage();
		}
		
		public void setMessageString(String mess) {
			this.mess.setMessage(mess);
		}
		
		public Message getMess() {
			return mess;
		}

		public void setMess(Message mess) {
			this.mess = mess;
		}
		
		public Image getImage() {
			return image;
		}

		public void setImage(Image image) {
			this.image = image;
		}
		
		public boolean getMarche() {
			return marche;
		}
		public void setMarche(boolean marche) {
			this.marche = marche;
		}
		public boolean getAction() {
			return action;
		}
		public void setAction(boolean action) {
			this.action = action;
		}

		public String getFichier() {
			return fichier;
		}

		public void setFichier(String fichier) {
			this.fichier = fichier;
		}

		public char getCaractere() {
			return caractere.charAt(0);
		}

		public void setCaractere(String caractere) {
			this.caractere = caractere;
		}
	}