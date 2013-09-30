/**
 * @author Bastien Baret
 */
package Principal;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;

import Perso.Hero;
import Perso.Pnj;

public class Menu implements KeyListener {
	
	private JFrame fen;
	private Graphics buffer;
	private boolean running;
	private ArrayList<Pnj> pnj;
	private BufferStrategy strategy;
	private static final String IMAGE_PATH = "image/";
	private static final String OTHER_PATH = "autre/";
	private static int DECALAGE_X = 3;
	private static int DECALAGE_Y = 25;
	private static int X_MAX;
	private static int Y_MAX;
	private float VELOCITY_Y;
	private float VELOCITY_X;
	private boolean reDraw;
	private int indexMenu;
	private int itemActuel;
	private static final float UNIT = 16;
	private Case[][] map;
	private static  ArrayList<Case> cases;
	private boolean go;
	private RescaleOp sombre = new RescaleOp(0.1f, 0, null);
	private RescaleOp clair = new RescaleOp(1.1f, 0, null);
	private static Player player;
	private InputStream is;
	private ArrayList<Thread> thread;
	
	private String[] entrerMenu = {"Nouveau", "Charger", "Quitter"};
	
	
	public Menu(JFrame fen, ArrayList<Thread> t) {
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
			//strategy.dispose();
			//buffer.dispose();
			fen.removeKeyListener(this);
		}
	}
	
	
	public void init() {
		
		//TODO Son
		thread.add(new Thread(new MP3Player("swain.mp3"), "SoundThread"));
		thread.get(1).start();
		
		setGo(true);
		running = true;
		indexMenu = 0;
		reDraw = false;
		pnj = new ArrayList<Pnj>();
		cases = new ArrayList<Case>();
		chargerCaractereCase();
		fen.addKeyListener(this);
		
		chargerPnj();
		
		X_MAX = Math.abs(800/(int)UNIT)-1;
		Y_MAX = Math.abs(600/(int)UNIT)-1;
		VELOCITY_X = UNIT / 200;
		VELOCITY_Y = UNIT / 200;
		
		System.out.println(X_MAX + ":" + Y_MAX);
		
	    loadMap();
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
			for(int i = 0; i<pnj.size(); i++) {
				if(System.currentTimeMillis() > pnj.get(i).getTempsMarche()) {
					pnj.get(i).setTempsMarche(System.currentTimeMillis() + pnj.get(i).getIntervalMarche());
					switch(pnj.get(i).getIndex()){
						case 0:
							if(pnj.get(i).getY() > 1)
								deplacerPnj(0, i);
							else {
								deplacerPnj(1, i);
							}
							break;
						case 1:
							if(pnj.get(i).getX() < 47)
								deplacerPnj(1, i);
							else {
								deplacerPnj(2, i);
							}
							break;
						case 2:
							if(pnj.get(i).getY() < 34)
								deplacerPnj(2, i);
							else if(pnj.get(i).getY() == 34) {
								deplacerPnj(3, i);
							}
							break;
						case 3:
							if(pnj.get(i).getX() > 1)
								deplacerPnj(3, i);
							else {
								deplacerPnj(0, i);
							}
							break;
					}
					
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
				Thread.sleep(10);
			}catch(Exception e) {}
			
		}
		
		
		fen.removeKeyListener(this);
		fen.getContentPane().removeAll();
	}
	
	
	public void update(long timePassed) {
		for(int j = 0; j<pnj.size(); j++) {
			for(int i = 0; i<pnj.get(j).getAnimation().size(); i++) {
				 pnj.get(j).getAnimation(i).update(timePassed);
			}
		}
	}
	
	public synchronized void draw(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		
		
		for(int i = 0; i<X_MAX; i++) {
			for(int j = 0; j<Y_MAX; j++) {
				g.drawImage(map[i][j].getImage(), (int)(i*UNIT)+DECALAGE_X, (int)(j*UNIT)+DECALAGE_Y, null);
			}
		}
		
		//Dessine les persos
		for(int i = 0; i<pnj.size(); i++) {
			g.drawImage(pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getImage(), Math.round( pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getX()) + DECALAGE_X, Math.round( pnj.get(i).getAnimation(pnj.get(i).getIndex()).getSprite().getY()) + DECALAGE_Y, null);
		}
		
		
		//Menu
		Image menu = null;
		FontMetrics fm;
		java.awt.geom.Rectangle2D rect;
		
		
		
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
		
		
	}

	
	private void loadMap() {
		map = new Case[X_MAX][Y_MAX];
		String ligne = null;
		char c = '.';
		FileReader fr = null;
		try {
			fr = new FileReader(OTHER_PATH+"mapMenu.txt");
		} catch (Exception e) {System.out.println(e);}
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

	
	private void chargerPnj() {
		
		String line;
		int nbEtat, x, y;
		String [][]spriteName = null;
		try{
			
			FileReader fr = new FileReader(OTHER_PATH+"npcMenu.txt");
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


	@Override
	public void keyPressed(KeyEvent arg0) {
		int keyCode = arg0.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_A:
			try {
				player.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				break;
			case KeyEvent.VK_ESCAPE:
				running = false;
				setGo(false);
				break;
			case KeyEvent.VK_UP:
				indexMenu -= 1;
				if(indexMenu < 0)
					indexMenu = entrerMenu.length-1;
				break;
			case KeyEvent.VK_DOWN:
				indexMenu += 1;
				if(indexMenu == entrerMenu.length)
					indexMenu = 0;
				break;
			case KeyEvent.VK_ENTER:
				switch(indexMenu) {
					case 0:
						running = false;
						break;
					case 1:
						running = false;
						break;
					case 2:
						running = false;
						go = false;
						break;
				}
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
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

	public boolean isGo() {
		return go;
	}

	public void setGo(boolean go) {
		this.go = go;
	}
	
	public String getThread() {
		String str;
		str = "Thread:\n";
		for(int i = 0; i<thread.size(); i++) {
			str += "\n\t[" + thread.get(i).getName() + "] -> " + thread.get(0).getState();
		}
		return str;
	}
	
	/******INNER CLASS*****/
	public class MP3Player implements Runnable{
	 
		public MP3Player(String file) {
			try {
				is = new FileInputStream(file);
				player = new Player(is);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			
			try {
				player.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
