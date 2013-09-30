
/**
 * @author Bastien Baret
 */

package Principal;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Perso.Hero;
import Perso.Pnj;


@SuppressWarnings("serial")
public class Interface implements KeyListener{
	
	
	//Attributs
	private JFrame fen;
	private Graphics buffer;
	private boolean running;
	private boolean retourMenu;
	private BufferStrategy strategy;
	private static final String IMAGE_PATH = "image/";
	private static final String OTHER_PATH = "autre/";
	private String[] entrerMenu = {"Personnage", "Inventaire", "Retour", "Menu principal", "Quitter"};
	
	
	private static  ArrayList<Case> cases;
	private static final float UNIT = 16;
	private float VELOCITY_Y;
	private float VELOCITY_X;
	private static int DECALAGE_X = 3;
	private static int DECALAGE_Y = 25;
	private static int X_MAX;
	private static int Y_MAX;
	private Case[][] map;
	private Message mess;
	private Image bgMess;
	private ArrayList<Pnj> pnj;
	private Image sante;
	private boolean menu = false;
	private boolean reDraw;
	private int indexMenu;
	private ArrayList<Thread> thread;
	private int menuActuel;
	
	public Interface(JFrame fen, ArrayList<Thread> t) {
		this.fen = fen;
		this.thread = t;
		strategy = fen.getBufferStrategy(); 
	    buffer = strategy.getDrawGraphics();
	}
	
	public void run() {
		try{
			init();
			gameLoop();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			fen.getContentPane().removeAll();
			fen.removeKeyListener(this);
		}
	}
	
	
	//Initialise la fenêtre
	public void init() {
		
		setRetourMenu(false);
		running = true;
		indexMenu = 0;
		menuActuel = -1;
		reDraw = false;
		sante = new ImageIcon(IMAGE_PATH + "coeur.png").getImage();
		pnj = new ArrayList<Pnj>();
		cases = new ArrayList<Case>();
		chargerCaractereCase();
		mess = new Message();
		fen.setTitle("Jeu");
		fen.addKeyListener(this);
		
		String[][] imgs = {	{IMAGE_PATH + "up1.png", IMAGE_PATH + "up2.png"},
							{IMAGE_PATH + "right1.png", IMAGE_PATH + "right2.png"},
							{IMAGE_PATH + "down1.png", IMAGE_PATH + "down2.png"},
							{IMAGE_PATH + "left1.png", IMAGE_PATH + "left2.png"}};
		pnj.add(new Hero(19, 8, 2, imgs, 0, 0, 2, "", "Bastien", UNIT));
		
		chargerPnj();
		
		X_MAX = Math.abs(800/(int)UNIT)-1;
		Y_MAX = Math.abs(600/(int)UNIT)-1;
		
		
		
		
		//Charge la map
		loadMap();
		
		
		VELOCITY_X = UNIT / 400;
		VELOCITY_Y = UNIT / 400;
		
	}

	private void chargerPnj() {
		
		String line;
		int nbEtat, x, y;
		String [][]spriteName = null;
		try{
			
			FileReader fr = new FileReader(OTHER_PATH+"npc.txt");
			BufferedReader br = new BufferedReader(fr);
			
			while((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line,"\t");
				
				x = Integer.parseInt(st.nextToken());
				y = Integer.parseInt(st.nextToken());
				nbEtat = Integer.parseInt(st.nextToken());
				
				spriteName = new String[4][nbEtat];
				for(int i = 0; i<4; i++) {
					for(int etat = 0; etat<nbEtat; etat++) {
						spriteName[i][etat] = IMAGE_PATH + "npc/" + st.nextToken();
					}
				}
				
				pnj.add(new Pnj(	x,
									y,
									nbEtat,
									spriteName,
									Long.parseLong(st.nextToken()),
									Long.parseLong(st.nextToken()),
									Integer.parseInt(st.nextToken()),
									st.nextToken(),
									st.nextToken(),
									UNIT));
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private String[] chargerCaractereCase() {
		ArrayList<String> charger = new ArrayList<String>();
		String []rep = null;
		try{
			
			FileReader fr = new FileReader(OTHER_PATH+"cases.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			String fic = null;
			while((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line,"\t");
				fic = st.nextToken();
				charger.add(fic);
				charger.add(st.nextToken());
				
				cases.add(new Case(	IMAGE_PATH + fic,
									Boolean.parseBoolean(st.nextToken()),
									Boolean.parseBoolean(st.nextToken()),
									new Message(st.nextToken(), 2000, 200, 200, IMAGE_PATH+"fondChat.png", 190, 190)));

			}
			
			rep = new String[charger.size()];
			
			for(int i = 0; i<charger.size(); i++) {
				rep = charger.toArray(rep);
			}			

			for(int i = 0; i<rep.length; i++) {
				if((i%2) == 1)
					rep[i] = IMAGE_PATH + rep[i];
			}
			
			for(int i = 0; i<cases.size(); i++) {
				cases.get(i).setCaractere(rep[2*i]);
				cases.get(i).setFichier(rep[2*i+1]);
			}
			
			br.close();
			fr.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return rep;
	}

	//Charge la map
	private void loadMap() {
		map = new Case[X_MAX][Y_MAX];
		String ligne = null;
		char c = '.';
		FileReader fr = null;
		try {
			fr = new FileReader(OTHER_PATH+"map.txt");
		} catch (FileNotFoundException e) {}
		BufferedReader br = new BufferedReader(fr);
		
		for(int i = 0; i<Y_MAX; i++) {
			try {
				ligne = br.readLine();
			} catch (IOException e) {}
			for(int j = 0; j<X_MAX; j++) {
				c = ligne.charAt(j);
				
				for(int a = 0; a<cases.size(); a++) {
					if(c == cases.get(a).getCaractere()) {
						map[j][i] = new Case(cases.get(a));
					}
				}
				
				
			}

		}
		
	}
	
	//Met a jour les sprites
	public void update(long timePassed) {
		if(menu) {
			
		}
		else {
			for(int j = 0; j<pnj.size(); j++) {
				for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
					 pnj.get(j).getAnimation(i).update(timePassed);
				}
			}
		}
	}
	
	//Dessine
	public synchronized void draw(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		
		g.setColor(Color.BLACK);
		//Menu
		if(menu) {
			if(reDraw)
				menu = false;
			
			Image menu = null;
			FontMetrics fm;
			java.awt.geom.Rectangle2D rect;
			
			switch(menuActuel) {
				//menu principale
				case -1:
					menu = new ImageIcon(IMAGE_PATH+"menu.png").getImage();
					g.drawImage(menu, (fen.getWidth()/2)-menu.getWidth(null)/2, fen.getHeight()/2 - menu.getHeight(null)/2, null);
					
					
					
					
					
					for(int i = 0; i<entrerMenu.length; i++) {
						if(i == indexMenu) {
							g.setFont(new Font("Arial", Font.BOLD, 17));
							g.setColor(new Color(136, 152, 160));
						}
						else {
							g.setFont(new Font("Arial", Font.BOLD, 12));
							g.setColor(Color.black);
						}
						
						fm   = g.getFontMetrics(g.getFont());
						rect = fm.getStringBounds(entrerMenu[i], g);
						
						int pas = (menu.getHeight(null)-12) / (entrerMenu.length+1);
						int x = (int) (fen.getWidth()/2 - rect.getWidth()/2);
						int y = (int) (fen.getHeight()/2 - menu.getHeight(null)/2 + (i+1)*pas);
						
						g.drawString(entrerMenu[i], x, (int)(y+rect.getHeight()));
					}
					break;
				
				//Menu personnage
				case 0:
					g.setFont(new Font("Arial", Font.BOLD, 14));
					fm   = g.getFontMetrics(g.getFont());
					rect = fm.getStringBounds(entrerMenu[0], g);
					menu = new ImageIcon(IMAGE_PATH+"menuPerso.png").getImage();
					
					int x = (fen.getWidth()/2)-menu.getWidth(null)/2;
					int y = fen.getHeight()/2 - menu.getHeight(null)/2;
					
					g.drawImage(menu, x, y, null);
					
					g.drawString(entrerMenu[0], (int)(fen.getWidth()/2 - rect.getWidth()/2), (fen.getHeight()/2 - menu.getHeight(null)/2) + 20);
					g.drawLine(	(int)(fen.getWidth()/2 - rect.getWidth()/2) - 40,
								(fen.getHeight()/2 - menu.getHeight(null)/2) + 30,
								(int)(fen.getWidth()/2 + rect.getWidth()/2) + 40,
								(fen.getHeight()/2 - menu.getHeight(null)/2) + 30);
					
					int yMin = (fen.getHeight()/2 - menu.getHeight(null)/2) + 40;
					int yMax = (fen.getHeight()/2 + menu.getHeight(null)/2) - 10;
					
					g.drawImage(pnj.get(0).getAnimation(2).getSprite().getImage(), x+60, (yMax+yMin)/2, null);
					
					g.setFont(new Font("Arial", Font.BOLD, 12));
					g.setColor(Color.black);
					
					String[] infos = {	"Nom: " + pnj.get(0).getNom(),
										"Classe: novice",
										"Niveau: " + ((Hero) pnj.get(0)).getLevel(),
										"Sante:"};
					
					int pas = (yMax-yMin) / (infos.length+1);
					
					for(int i = 0; i<infos.length; i++) {
						g.drawString(infos[i], x + 150, pas*(i+1)+yMin);
						if(infos[i].equals("Sante:")){
							for(int j = 0; j<((Hero) pnj.get(0)).getSante();j++) {
								g.drawImage(sante, x + 200 + j*10, pas*(i+1)+ yMin- 8, null);
							}
						}
					}
					
					break;
					
				//Menu inventaire
				case 1:
					g.setFont(new Font("Arial", Font.BOLD, 14));
					fm   = g.getFontMetrics(g.getFont());
					rect = fm.getStringBounds(entrerMenu[1], g);
					
					menu = new ImageIcon(IMAGE_PATH+"menuPerso.png").getImage();
					g.drawImage(menu, (fen.getWidth()/2)-menu.getWidth(null)/2, fen.getHeight()/2 - menu.getHeight(null)/2, null);
					
					g.drawString(entrerMenu[1], (int)(fen.getWidth()/2 - rect.getWidth()/2), (fen.getHeight()/2 - menu.getHeight(null)/2) + 20);
					g.drawLine(	(int)(fen.getWidth()/2 - rect.getWidth()/2) - 40,
							(fen.getHeight()/2 - menu.getHeight(null)/2) + 30,
							(int)(fen.getWidth()/2 + rect.getWidth()/2) + 40,
							(fen.getHeight()/2 - menu.getHeight(null)/2) + 30);
					
					break;
			}
			
			
			
		}
		else {
			g.setFont(new Font("Arial", Font.BOLD, 11));
			//Jeu
			//Dessine la map case par case
			for(int i = 0; i<X_MAX; i++) {
				for(int j = 0; j<Y_MAX; j++) {
					g.drawImage(map[i][j].getImage(), (int)(i*UNIT)+DECALAGE_X, (int)(j*UNIT)+DECALAGE_Y, null);
				}
			}
			
			//Divers messages
			g.setColor(Color.black);
			if(System.currentTimeMillis() < mess.getValidite()) {
				if(mess.getBg() != null) {
					bgMess = new ImageIcon(mess.getBg()).getImage();
					g.drawImage(bgMess, (fen.getWidth()/2)-(bgMess.getWidth(null)/2), fen.getHeight()-bgMess.getHeight(null), null);
				}
				g.drawString(mess.getMessage(), (fen.getWidth()/2)-(bgMess.getWidth(null)/2)+10, (fen.getHeight()-bgMess.getHeight(null))+15);
			}
			
			//Dessine les persos
			for(int i = 0; i<pnj.size(); i++) {
				g.drawImage(pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getImage(), Math.round( pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getX()) + DECALAGE_X, Math.round( pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getY()) + DECALAGE_Y, null);
			}
			
			
			//Dessine le nom
			
			for(int i = 0; i<pnj.size(); i++) {
				FontMetrics fm   = g.getFontMetrics(g.getFont());
				java.awt.geom.Rectangle2D rect = fm.getStringBounds(pnj.get(i).getNom(), g);
				
				int textWidth  = (int)(rect.getWidth());
				int panelWidth = pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getWidth();
	
	
				int x = (panelWidth  - textWidth)  / 2;
	
	
				g.drawString(	pnj.get(i).getNom(),
								x + DECALAGE_X + pnj.get(i).getAnimation(pnj.get(i).getIndex()).getX(),
								pnj.get(i).getAnimation(pnj.get(i).getIndex()).getY() + 22);
			}
			
			
			if(System.currentTimeMillis() < mess.getValidite()) {
				if(mess.getBg() != null) {
					bgMess = new ImageIcon(mess.getBg()).getImage();
					g.drawImage(bgMess, (fen.getWidth()/2)-(bgMess.getWidth(null)/2), fen.getHeight()-bgMess.getHeight(null), null);
				}
				g.drawString(mess.getMessage(), (fen.getWidth()/2)-(bgMess.getWidth(null)/2)+10, (fen.getHeight()-bgMess.getHeight(null))+15);
			}
			
			bgMess = new ImageIcon(IMAGE_PATH+"bgMess.png").getImage();
			g.drawImage(bgMess, fen.getWidth()-bgMess.getWidth(null), 23, null);
			g.drawString("Nom: " + pnj.get(0).getNom() + "      lvl." + ((Hero) pnj.get(0)).getLevel(), 560, 42);
			g.drawString("Sante:", 560, 55);
			
			if(pnj.get(0).getClass().getName().equals("Perso.Hero")) {
				for(int i = 0; i<((Hero) pnj.get(0)).getSante(); i++) {
					g.drawImage(sante, (600 + i*9), 47, null);
				}
			}
			if(reDraw) {
				reDraw = false;
				menu = true;
			}
		}
	}

	
	//Boucle principale
	public void gameLoop() {
		long startingTime = System.currentTimeMillis();
		long cumTime = startingTime;
		while(running) {
			long timePassed = System.currentTimeMillis() - cumTime;
			cumTime += timePassed;
			
			
			//Met a jour les sprites avec le temps passé
			update(timePassed);
			
			//Redessine
			draw(buffer);
			strategy.show();
			
			//Deplacement des pnj
			for(int i = 1; i<pnj.size(); i++) {
				if(System.currentTimeMillis() > pnj.get(i).getTempsMarche()) {
					pnj.get(i).setTempsMarche(System.currentTimeMillis() + pnj.get(i).getIntervalMarche());
					deplacerPnj((int)(Math.random() *4), i);
				}
			}
			
			
			//Replace les sprites au centre de la case (deplacement d'un pixel parfois)
			for(int j = 0;
					j<pnj.size();j++) {
				switch(pnj.get(j).getIndex()) {
					case 0:
						
							if(pnj.get(j).getAnimation(pnj.get(j).getIndex()).getY() <= pnj.get(j).getY()*UNIT) {
								for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
									pnj.get(j).getAnimation(i).setY(pnj.get(j).getY()*UNIT);
									pnj.get(j).getAnimation(i).resetVelocity();
									pnj.get(j).getAnimation(i).setPaused(true);
								}
							}
						
						break;
					case 1:
						
							if( pnj.get(j).getAnimation(pnj.get(j).getIndex()).getX() >= pnj.get(j).getX()*UNIT) {
								for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
									pnj.get(j).getAnimation(i).setX(pnj.get(j).getX()*UNIT);
									pnj.get(j).getAnimation(i).resetVelocity();
									pnj.get(j).getAnimation(i).setPaused(true);
								}
							}
						
						break;
					case 2:
						
							if( pnj.get(j).getAnimation(pnj.get(j).getIndex()).getY() >= pnj.get(j).getY()*UNIT) {
								for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
									pnj.get(j).getAnimation(i).setY(pnj.get(j).getY()*UNIT);
									pnj.get(j).getAnimation(i).resetVelocity();
									pnj.get(j).getAnimation(i).setPaused(true);
								}
							}
						
						break;
					case 3:
						
							if( pnj.get(j).getAnimation(pnj.get(j).getIndex()).getX() <= pnj.get(j).getX()*UNIT) {
								for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
									pnj.get(j).getAnimation(i).setX(pnj.get(j).getX()*UNIT);
									pnj.get(j).getAnimation(i).resetVelocity();
									pnj.get(j).getAnimation(i).setPaused(true);
								}
							}
						
						break;
				}
			}
			
			
			//Sleep pour diminuer la charge du processeur
			try{
				Thread.sleep(20);
			}catch(Exception e) {}
			
		}
	}
	
	public void stop() {
		running = false;
	}
	
	//Gestion des touches du clavier
	@Override
	public void keyPressed(KeyEvent e) {
		boolean libre = true;
		int keyCode = e.getKeyCode();
		
		switch(keyCode) {
			//Echap : quitter
			case KeyEvent.VK_ESCAPE:
				if(menu) {
					switch(menuActuel) {
						case -1:
							menu = false;
							indexMenu = 0;
							menuActuel = -1;
							break;
							
						case 0:
							reDraw = true;
							indexMenu = 0;
							menuActuel = -1;
							break;
							
						case 1:
							indexMenu = 1;
							reDraw = true;
							menuActuel = -1;
							break;
					}
				}
				else
					menu = true;
				break;

			//Haut : monter
			case KeyEvent.VK_UP:
				if(menu) {
					indexMenu -= 1;
					if(indexMenu < 0)
						indexMenu = entrerMenu.length-1;
				}
				else {
					libre = true;
					for(int i = 1; i<pnj.size();i++) {
						if(pnj.get(i).getX() == pnj.get(0).getX() && pnj.get(i).getY() == pnj.get(0).getY()-1)
							libre = false;
					}
					
						if(pnj.get(0).getY()>0) {
							if(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getPaused()) {
								if(libre) {
									if(map[pnj.get(0).getX()][pnj.get(0).getY()-1].getMarche()){
									
										pnj.get(0).setY(pnj.get(0).getY()-1);
										for(int i = 0; i<pnj.get(0).getAnimation().size(); i++) {
											if(i!=0) {
												pnj.get(0).getAnimation(i).resetVelocity();
											}
											else {
												pnj.get(0).getAnimation(i).setVelocityY(-VELOCITY_Y);
												pnj.get(0).getAnimation(i).setX(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getX());
												pnj.get(0).getAnimation(i).setY(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getY());
												pnj.get(0).getAnimation(i).setPaused(false);
											}
										}
										pnj.get(0).setIndex(0);
									}
									else {
										pnj.get(0).setIndex(0);
										pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
									}
							}
							else {
								pnj.get(0).setIndex(0);
								pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
							}
							
						}
					}
					else {
						pnj.get(0).setIndex(0);
						pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
					}
				}
					e.consume();
				break;
				
			//Bas : descendre
			case KeyEvent.VK_DOWN:
				if(menu) {
					indexMenu += 1;
					if(indexMenu == entrerMenu.length)
						indexMenu = 0;
				}
				else {
					libre = true;
					for(int i = 1; i<pnj.size();i++) {
						if(pnj.get(i).getX() == pnj.get(0).getX() && pnj.get(i).getY() == pnj.get(0).getY()+1)
							libre = false;
					}
					
						if(pnj.get(0).getY()<Y_MAX-1) {
							if(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getPaused()) {
								if(libre) {
									if(map[pnj.get(0).getX()][pnj.get(0).getY()+1].getMarche()){
										pnj.get(0).setY(pnj.get(0).getY()+1);
										for(int i = 0; i<pnj.get(0).getAnimation().size(); i++) {
											if(i!=2) {
												pnj.get(0).getAnimation(i).resetVelocity();
											}
											else {
												pnj.get(0).getAnimation(i).setVelocityY(VELOCITY_Y);
												pnj.get(0).getAnimation(i).setX(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getX());
												pnj.get(0).getAnimation(i).setY(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getY());
												pnj.get(0).getAnimation(i).setPaused(false);
											}
										}
										pnj.get(0).setIndex(2);
									}
									else {
										pnj.get(0).setIndex(2);
										pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
									}
							}
							else {
								pnj.get(0).setIndex(2);
								pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
							}
							
						}
					}
					else {
						pnj.get(0).setIndex(2);
						pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
					}
				}
				e.consume();
				break;
				
			//Droite : aller a droite
			case KeyEvent.VK_RIGHT:
				if(menu) {
					
				}
				else {
					libre = true;
					for(int i = 1; i<pnj.size();i++) {
						if(pnj.get(i).getX() == pnj.get(0).getX()+1 && pnj.get(i).getY() == pnj.get(0).getY())
							libre = false;
					}
					
						if(pnj.get(0).getX()<X_MAX-1) {
							if(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getPaused()) {
								if(libre) {
									if(map[pnj.get(0).getX()+1][pnj.get(0).getY()].getMarche()){
										pnj.get(0).setX(pnj.get(0).getX()+1);
										for(int i = 0; i<pnj.get(0).getAnimation().size(); i++) {
											if(i!=1) {
												pnj.get(0).getAnimation(i).resetVelocity();
											}
											else {
												pnj.get(0).getAnimation(i).setVelocityX(VELOCITY_X);
												pnj.get(0).getAnimation(i).setX(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getX());
												pnj.get(0).getAnimation(i).setY(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getY());
												pnj.get(0).getAnimation(i).setPaused(false);
											}
										}
										pnj.get(0).setIndex(1);
									}
									else {
										pnj.get(0).setIndex(1);
										pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
									}
							}
							else {
								pnj.get(0).setIndex(1);
								pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
							}
							
						}
					}
					else {
						pnj.get(0).setIndex(1);
						pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
					}
				}
				e.consume();
				break;
				
			//Gauche : aller a gauche
			case KeyEvent.VK_LEFT:
				if(menu) {
					
				}
				else {
					libre = true;
					for(int i = 1; i<pnj.size();i++) {
						if(pnj.get(i).getX() == pnj.get(0).getX()-1 && pnj.get(i).getY() == pnj.get(0).getY())
							libre = false;
					}
					
						if(pnj.get(0).getX()>0) {
							if(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getPaused()) {
								if(libre) {
									if(map[pnj.get(0).getX()-1][pnj.get(0).getY()].getMarche()){
										pnj.get(0).setX(pnj.get(0).getX()-1);
										for(int i = 0; i<pnj.get(0).getAnimation().size(); i++) {
											if(i!=3) {
												pnj.get(0).getAnimation(i).resetVelocity();
											}
											else {
												pnj.get(0).getAnimation(i).setVelocityX(-VELOCITY_X);
												pnj.get(0).getAnimation(i).setX(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getX());
												pnj.get(0).getAnimation(i).setY(pnj.get(0).getAnimation(pnj.get(0).getIndex()).getY());
												pnj.get(0).getAnimation(i).setPaused(false);
											}
										}
										pnj.get(0).setIndex(3);
									}
									else {
										pnj.get(0).setIndex(3);
										pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
									}
							}
							else {
								pnj.get(0).setIndex(3);
								pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
							}
						}
					}
					else {
						pnj.get(0).setIndex(3);
						pnj.get(0).getAnimation(pnj.get(0).getIndex()).setPaused(true);
					}
				}
				e.consume();
				break;
				
				
				//Teleportation
			case KeyEvent.VK_T:
				if(menu) {
					
				}
				else {
					int rx = 0;
					int ry = 0;
					String repX = JOptionPane.showInputDialog(null, "Veuillez entrer X", "Teleportation", JOptionPane.QUESTION_MESSAGE);
					String repY = JOptionPane.showInputDialog(null, "Veuillez entrer Y", "Teleportation", JOptionPane.QUESTION_MESSAGE);
					if(repX != null) {
						try {
							rx = Integer.parseInt(repX);
							ry = Integer.parseInt(repY);
						}
						catch(Exception e1) {
							for(int i = 0; i<pnj.size(); i++) {
								System.out.println(repX + " - " + pnj.get(i).getNom());
								if(repX.equals(pnj.get(i).getNom())) {
									
									rx = pnj.get(i).getX();
									ry = pnj.get(i).getY();
								}
							}
						}
						if(rx < 0)
							rx = 0;
						if(rx > X_MAX-1)
							rx = X_MAX-1;
						if(ry < 0)
							ry = 0;
						if(ry > Y_MAX-1)
							ry = Y_MAX-1;
						pnj.get(0).setX(rx);
						pnj.get(0).setY(ry);
						
					}
					for(int i = 0; i<pnj.get(0).getAnimation().size(); i++) {
						pnj.get(0).getAnimation(i).setX(pnj.get(0).getX()*UNIT);
						pnj.get(0).getAnimation(i).setY(pnj.get(0).getY()*UNIT);
					}
				}
				e.consume();
				break;
				
			//Entrer : action
			case KeyEvent.VK_ENTER:
				if(menu) {
					switch(menuActuel) {
					
						case -1:
							switch(indexMenu) {
							case 0:
								menuActuel = 0;
								reDraw = true;
								break;
							case 1:
								menuActuel = 1;
								reDraw = true;
								break;
							case 2:
								menu = false;
								indexMenu = 0;
								break;
							case 3:
								setRetourMenu(true);
								stop();
								break;
							case 4:
								setRetourMenu(false);
								stop();
								break;
							}
							break;
							
						case 0:
							reDraw = true;
							menuActuel = -1;
							indexMenu = 0;
							break;
							
						case 1:
							reDraw = true;
							indexMenu = 1;
							menuActuel = -1;
							break;
					}
					
					
				}
				else {
					boolean npc;
					int n = 0;
					switch(pnj.get(0).getIndex()) {
						case 0:
							npc = false;
							for(int i = 1; i<pnj.size(); i++) {
								if(pnj.get(i).getX() == pnj.get(0).getX() && pnj.get(i).getY() == pnj.get(0).getY()-1) {
									npc = true;
									n = i;
								}
							}
							
							if(pnj.get(0).getY()>0) {
								if(map[pnj.get(0).getX()][pnj.get(0).getY()-1].getAction()){
									mess = map[pnj.get(0).getX()][pnj.get(0).getY()-1].getMess();
									mess.setValidite(System.currentTimeMillis());
								}
								if(npc) {
									mess = new Message(pnj.get(n).getMessage(), 2000, 200, 200, IMAGE_PATH+"fondChat.png", 190, 190);
									mess.setValidite(System.currentTimeMillis());
									pnj.get(n).setIndex(2);
								}
							}
							
							break;
						case 1:
							npc = false;
							for(int i = 1; i<pnj.size(); i++) {
								if(pnj.get(i).getX() == pnj.get(0).getX()+1 && pnj.get(i).getY() == pnj.get(0).getY()) {
									npc = true;
									n = i;
								}
							}
							
							if(pnj.get(0).getX()<=X_MAX) {
								if(map[pnj.get(0).getX()+1][pnj.get(0).getY()].getAction()){
									mess = map[pnj.get(0).getX()+1][pnj.get(0).getY()].getMess();
									mess.setValidite(System.currentTimeMillis());
								}
								if(npc) {
									mess = new Message(pnj.get(n).getMessage(), 2000, 200, 200, IMAGE_PATH+"fondChat.png", 190, 190);
									mess.setValidite(System.currentTimeMillis());
									pnj.get(n).setIndex(3);
								}
							}
							break;
						case 2:
							npc = false;
							for(int i = 1; i<pnj.size(); i++) {
								if(pnj.get(i).getX() == pnj.get(0).getX() && pnj.get(i).getY() == pnj.get(0).getY()+1) {
									npc = true;
									n = i;
								}
								
							}
							
							if(pnj.get(0).getY()<Y_MAX-1) {
								if(map[pnj.get(0).getX()][pnj.get(0).getY()+1].getAction()){
									mess = map[pnj.get(0).getX()][pnj.get(0).getY()+1].getMess();
									mess.setValidite(System.currentTimeMillis());
								}
								if(npc) {
									mess = new Message(pnj.get(n).getMessage(), 2000, 200, 200, IMAGE_PATH+"fondChat.png", 190, 190);
									mess.setValidite(System.currentTimeMillis());
									pnj.get(n).setIndex(0);
								}
							}
							break;
						case 3:
							npc = false;
							for(int i = 1; i<pnj.size(); i++) {
								if(pnj.get(i).getX() == pnj.get(0).getX()-1 && pnj.get(i).getY() == pnj.get(0).getY()) {
									npc = true;
									n = i;
								}
							}
							
							if(pnj.get(0).getX()>0) {
								if(map[pnj.get(0).getX()-1][pnj.get(0).getY()].getAction()){
									mess = map[pnj.get(0).getX()-1][pnj.get(0).getY()].getMess();
									mess.setValidite(System.currentTimeMillis());
								}
								if(npc) {
									mess = new Message(pnj.get(n).getMessage(), 2000, 200, 200, IMAGE_PATH+"fondChat.png", 190, 190);
									mess.setValidite(System.currentTimeMillis());
									pnj.get(n).setIndex(1);
								}
							}
							break;
					}
				}
				e.consume();
				break;
			
			
				
				
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}

	public void deplacerPnj(int index, int n) {
		boolean libre = true;
		
		switch(index){
		//Up
		case 0:
			libre = true;
			for(int i = 0; i<pnj.size();i++) {
				if(pnj.get(i).getX() == pnj.get(n).getX() && pnj.get(i).getY() == pnj.get(n).getY()-1)
					libre = false;
			}
			
				if(pnj.get(n).getY()>0) {
					if(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getPaused()) {
						if(libre) {
						if(map[pnj.get(n).getX()][pnj.get(n).getY()-1].getMarche()){
						
							pnj.get(n).setY(pnj.get(n).getY()-1);
							for(int i = 0; i<pnj.get(n).getAnimation().size(); i++) {
								if(i!=0) {
									pnj.get(n).getAnimation(i).resetVelocity();
								}
								else {
									pnj.get(n).getAnimation(i).setVelocityY(-VELOCITY_Y);
									pnj.get(n).getAnimation(i).setX(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getX());
									pnj.get(n).getAnimation(i).setY(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getY());
									pnj.get(n).getAnimation(i).setPaused(false);
								}
							}
							pnj.get(n).setIndex(0);
							//timeState = e.getWhen();
						}
						else {
							pnj.get(n).setIndex(0);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					}
					else {
						pnj.get(n).setIndex(0);
						pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
					}
					
				}
			}
			else {
				pnj.get(n).setIndex(0);
				pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
			}
			break;
			
		//Bas : descendre
		case 2:
			libre = true;
			for(int i = 0; i<pnj.size();i++) {
				if(pnj.get(i).getX() == pnj.get(n).getX() && pnj.get(i).getY() == pnj.get(n).getY()+1)
					libre = false;
			}
				if(pnj.get(n).getY()<Y_MAX-1) {
					if(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getPaused()) {
						if(libre) {
						if(map[pnj.get(n).getX()][pnj.get(n).getY()+1].getMarche()){
							pnj.get(n).setY(pnj.get(n).getY()+1);
							for(int i = 0; i<pnj.get(n).getAnimation().size(); i++) {
								if(i!=2) {
									pnj.get(n).getAnimation(i).resetVelocity();
								}
								else {
									pnj.get(n).getAnimation(i).setVelocityY(VELOCITY_Y);
									pnj.get(n).getAnimation(i).setX(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getX());
									pnj.get(n).getAnimation(i).setY(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getY());
									pnj.get(n).getAnimation(i).setPaused(false);
								}
							}
							pnj.get(n).setIndex(2);
							//timeState = e.getWhen();
						}
						else {
							pnj.get(n).setIndex(2);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					}
						else {
							pnj.get(n).setIndex(2);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					
				}
				
			}
			else {
				pnj.get(n).setIndex(2);
				pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
			}
			break;
			
		//Droite : aller a droite
		case 1:
			libre = true;
			for(int i = 0; i<pnj.size();i++) {
				if(pnj.get(i).getX() == pnj.get(n).getX()+1 && pnj.get(i).getY() == pnj.get(n).getY())
					libre = false;
			}
			
				if(pnj.get(n).getX()<X_MAX-1) {
					if(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getPaused()) {
						if(libre) {
						if(map[pnj.get(n).getX()+1][pnj.get(n).getY()].getMarche()){
							pnj.get(n).setX(pnj.get(n).getX()+1);
							for(int i = 0; i<pnj.get(n).getAnimation().size(); i++) {
								if(i!=1) {
									pnj.get(n).getAnimation(i).resetVelocity();
								}
								else {
									pnj.get(n).getAnimation(i).setVelocityX(VELOCITY_X);
									pnj.get(n).getAnimation(i).setX(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getX());
									pnj.get(n).getAnimation(i).setY(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getY());
									pnj.get(n).getAnimation(i).setPaused(false);
								}
							}
							pnj.get(n).setIndex(1);
							//timeState = e.getWhen();
						}
						else {
							pnj.get(n).setIndex(1);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					}
						else {
							pnj.get(n).setIndex(1);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					
				}
				
			}
			else {
				pnj.get(n).setIndex(1);
				pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
			}
			break;
			
		//Gauche : aller a gauche
		case 3:
			libre = true;
			for(int i = 0; i<pnj.size();i++) {
				if(pnj.get(i).getX() == pnj.get(n).getX()-1 && pnj.get(i).getY() == pnj.get(n).getY())
					libre = false;
			}
			
				if(pnj.get(n).getX()>0) {
					if(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getPaused()) {
						if(libre) {
						if(map[pnj.get(n).getX()-1][pnj.get(n).getY()].getMarche()){
							pnj.get(n).setX(pnj.get(n).getX()-1);
							for(int i = 0; i<pnj.get(n).getAnimation().size(); i++) {
								if(i!=3) {
									pnj.get(n).getAnimation(i).resetVelocity();
								}
								else {
									pnj.get(n).getAnimation(i).setVelocityX(-VELOCITY_X);
									pnj.get(n).getAnimation(i).setX(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getX());
									pnj.get(n).getAnimation(i).setY(pnj.get(n).getAnimation(pnj.get(n).getIndex()).getY());
									pnj.get(n).getAnimation(i).setPaused(false);
								}
							}
							pnj.get(n).setIndex(3);
							//timeState = e.getWhen();
						}
						else {
							pnj.get(n).setIndex(3);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
					}
						else {
							pnj.get(n).setIndex(3);
							pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
						}
				}
				
			}
			else {
				pnj.get(n).setIndex(3);
				pnj.get(n).getAnimation(pnj.get(n).getIndex()).setPaused(true);
			}
			break;
		}
	}

	public boolean isRetourMenu() {
		return retourMenu;
	}

	public void setRetourMenu(boolean retourMenu) {
		this.retourMenu = retourMenu;
	}
	
	public String getThread() {
		String str;
		str = "Thread:\n";
		for(int i = 0; i<thread.size(); i++) {
			str += "\n\t[" + thread.get(i).getName() + "] -> " + thread.get(0).getState();
		}
		return str;
	}
	
}
