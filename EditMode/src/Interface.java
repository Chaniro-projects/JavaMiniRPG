import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class Interface extends Core implements KeyListener, MouseWheelListener, MouseMotionListener, MouseListener{
	
	//Main
	public static void main(String[] args) {
		Interface i = new Interface();
		i.run();
	}
	
	
	//Attributs
	//Ajouter un element de decor (+ modifier switch dans loadMap )
	private static final String IMAGE_PATH = "image/";
	private static final String OTHER_PATH = "autre/";
	private static  ArrayList<Case> cases;
	private static String[] caractereImage;
	private Image bg;
	private ArrayList<Animation> a;
	private int index;
	private long timeState;
	private static final float UNIT = 16;
	private float VELOCITY_Y;
	private float VELOCITY_X;
	private static long FRAME_SPEED = 400;
	private static int DECALAGE_X = 3;
	private static int DECALAGE_Y = 25;
	private static int X_MAX;
	private static int Y_MAX;
	private int x = 2;
	private int y = 2;
	private Case[][] map;
	private Message mess;
	private Image bgMess;
	private int dernierDeplacement;
	private boolean afficherInfo;
	private boolean mouseMove;
	private boolean continu;
	
	//Initialise la fenêtre
	public void init() {
		super.init();
		
		cases = new ArrayList<Case>();
		continu = false;
		caractereImage = chargerCaractereCase();
		mouseMove = false;
		dernierDeplacement = 0;
		afficherInfo = true;
		mess = new Message();
		setTitle("Mode edition");
		setLocationRelativeTo(null);
		setIgnoreRepaint( true ); 
		setResizable(false);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addKeyListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		setFocusTraversalKeysEnabled(false);
		setLocation(100, 100);
		setVisible(true);
		
	    //Charge les images
		loadImages();
		a = new ArrayList<Animation>();
		
		//Charge les ressources
		for(int i = 0; i<(caractereImage.length/2); i++) {
			String[] up = {caractereImage[i*2+1]};
			a.add(new Animation(up, FRAME_SPEED, x*UNIT, y*UNIT, 0, 0));
		}
		
		index = 0;
		timeState = 0;
		
		X_MAX = Math.abs(800/(int)UNIT)-1;
		Y_MAX = Math.abs(600/(int)UNIT)-1;
		System.out.println(X_MAX + "|" + Y_MAX);
		
		setSize((int)(X_MAX*UNIT+DECALAGE_X)+3, (int)(Y_MAX*UNIT+DECALAGE_Y)+DECALAGE_X);
		
		//Double buffering
		createBufferStrategy(2);
		strategy = getBufferStrategy(); 
	    buffer = strategy.getDrawGraphics();
		
		//Charge la map
		loadMap();
		
		
		VELOCITY_X = 1f;
		VELOCITY_Y = 1f;
		
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
									new Message(st.nextToken(), 2000, 200, 200, IMAGE_PATH+"bgMess.png", 190, 190)));

			}
			
			rep = new String[charger.size()];
			
			for(String s:charger) {
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
		String fichier = null;
		char c = '.';
		FileReader fr = null;
		try {
			fr = new FileReader(OTHER_PATH + "map.txt");
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

	//Charge le fond
	public void loadImages() {
		bg = new ImageIcon(IMAGE_PATH + "buisson.png").getImage();
	}
	
	//Met a jour les sprites
	public void update(long timePassed) {
		for(int i = 0; i<a.size(); i++) {
			a.get(i).update(timePassed);
		}
	}
	
	//Dessine
	public synchronized void draw(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;

		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 600);
		
		//Dessine la map case par case
		for(int i = 0; i<X_MAX; i++) {
			for(int j = 0; j<Y_MAX; j++) {

				g.drawImage(map[i][j].getImage(), (int)(i*UNIT)+DECALAGE_X, (int)(j*UNIT)+DECALAGE_Y, null);

			}
		}
		
		
		//info
		if(afficherInfo) {
			bgMess = new ImageIcon(IMAGE_PATH + "bgMess.png").getImage();
			g.drawImage(bgMess, getWidth()-bgMess.getWidth(null), 23, null);
			g.drawString("x : " + x + "/" + X_MAX + "                    " + (index+1) + "/" + a.size(), 560, 42);
			g.drawString("y : " + y + "/" + Y_MAX + "                    " + caractereImage[index*2+1], 560, 53);
		}
		
		//Dessine la case
		g.drawImage(a.get(index).getSprite().getImage(), Math.round(a.get(index).getSprite().getX()) + DECALAGE_X, Math.round(a.get(index).getSprite().getY()) + DECALAGE_Y, null);
		
		//Dessine le rectangle
		g.setColor(Color.red);
		g.drawRect(Math.round(a.get(index).getSprite().getX()) + DECALAGE_X, Math.round(a.get(index).getSprite().getY()) + DECALAGE_Y, (int)UNIT, (int)UNIT);
				
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
			
			//Replace le sprite au centre de la case (deplacement d'un pixel parfois)
			switch(dernierDeplacement) {
				case 0:
					if(a.get(index).getY() <= y*UNIT) {
						for(int i = 0; i<a.size(); i++) {
							a.get(i).setY(y*UNIT);
							a.get(i).resetVelocity();
							a.get(i).setPaused(true);
						}
					}
					break;
				case 1:
					if(a.get(index).getX() >= x*UNIT) {
						for(int i = 0; i<a.size(); i++) {
							a.get(i).setX(x*UNIT);
							a.get(i).resetVelocity();
							a.get(i).setPaused(true);
						}
					}
					break;
				case 2:
					if(a.get(index).getY() >= y*UNIT) {
						for(int i = 0; i<a.size(); i++) {
							a.get(i).setY(y*UNIT);
							a.get(i).resetVelocity();
							a.get(i).setPaused(true);
						}
					}
					break;
				case 3:
					if(a.get(index).getX() <= x*UNIT) {
						for(int i = 0; i<a.size(); i++) {
							a.get(i).setX(x*UNIT);
							a.get(i).resetVelocity();
							a.get(i).setPaused(true);
						}
					}
					break;
			}
			
			
			//Sleep pour diminuer la charge du processeur
			try{
				Thread.sleep(20);
			}catch(Exception e) {}
			
		}
		buffer.dispose();
		strategy.dispose();
		dispose();
	}
	
	
	//Gestion des touches du clavier
	@Override
	public void keyPressed(KeyEvent e) {
		
		int keyCode = e.getKeyCode();
		
		switch(keyCode) {
			//Echap : quitter
			case KeyEvent.VK_ESCAPE:
				Boolean quit = true;
				JOptionPane jop1 = new JOptionPane();			
				int option = jop1.showConfirmDialog(null, "Voulez-vous sauvegarder la map ?", "Quitter", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							
				if(option == JOptionPane.OK_OPTION)
				{
					String nom = jop1.showInputDialog(null, "Veuillez saisir un emplacement de fichier", "Enregistrer", JOptionPane.QUESTION_MESSAGE);
					
					
					
					try {
						PrintWriter out  = new PrintWriter(new FileWriter(nom));
						
						for(int i = 0; i<Y_MAX; i++) {
							for(int j = 0; j<X_MAX; j++) {
								String nomFichier = map[j][i].getFichier();
								for(int a = 1; a<caractereImage.length; a+=2) {
									if(caractereImage[a] == nomFichier) {
										out.print(caractereImage[a-1].charAt(0));
										
									}
								}
							}
							out.println("");
						}
						
						out.close();
					}
					catch(Exception e1) {
						e1.printStackTrace();
					}
					
				}
				if(quit)
					stop();
				break;
				
			//Haut : monter
			case KeyEvent.VK_UP:
				if(y>0) {
					if(a.get(index).getPaused()) {
							y -= 1;
							for(int i = 0; i<a.size(); i++) {
								a.get(i).setVelocityY(-VELOCITY_Y);
								a.get(i).setX(a.get(index).getX());
								a.get(i).setY(a.get(index).getY());
								a.get(i).setPaused(false);
							}
							timeState = e.getWhen();
							dernierDeplacement = 0;
					}
					
				}
				e.consume();
				break;
				
			//Bas : descendre
			case KeyEvent.VK_DOWN:
				if(y<Y_MAX-1) {
					if(a.get(index).getPaused()) {
							y += 1;
							for(int i = 0; i<a.size(); i++) {
								a.get(i).setVelocityY(VELOCITY_Y);
								a.get(i).setX(a.get(index).getX());
								a.get(i).setY(a.get(index).getY());
								a.get(i).setPaused(false);
								
							}
							timeState = e.getWhen();
							dernierDeplacement = 2;
					}
					
				}
				e.consume();
				break;
				
			//Droite : aller a droite
			case KeyEvent.VK_RIGHT:
				if(x<X_MAX-1) {
					if(a.get(index).getPaused()) {
							x += 1;
							for(int i = 0; i<a.size(); i++) {
								a.get(i).setVelocityX(VELOCITY_X);
								a.get(i).setX(a.get(index).getX());
								a.get(i).setY(a.get(index).getY());
								a.get(i).setPaused(false);
								
							}
							timeState = e.getWhen();
							dernierDeplacement = 1;
					}
					
				}
				e.consume();
				break;
				
			//Gauche : aller a gauche
			case KeyEvent.VK_LEFT:
				if(x>0) {
					if(a.get(index).getPaused()) {
							x -= 1;
							for(int i = 0; i<a.size(); i++) {
								a.get(i).setVelocityX(-VELOCITY_X);
								a.get(i).setX(a.get(index).getX());
								a.get(i).setY(a.get(index).getY());
								a.get(i).setPaused(false);
							}
							timeState = e.getWhen();
							dernierDeplacement = 3;
					}
					
				}
				e.consume();
				break;
				
			//Entrer : placer la texture
			case KeyEvent.VK_ENTER:
				map[x][y] = new Case(caractereImage[index*2+1], true, false, "");
				e.consume();
				break;
			
			//Teleportation
			case KeyEvent.VK_T:
				JOptionPane jop = new JOptionPane();
				String repX = jop.showInputDialog(null, "Veuillez entrer X", "Teleportation", JOptionPane.QUESTION_MESSAGE);
				String repY = jop.showInputDialog(null, "Veuillez entrer Y", "Teleportation", JOptionPane.QUESTION_MESSAGE);
				if(repX != null) {
					int rx = Integer.parseInt(repX);
					int ry = Integer.parseInt(repY);
					if(rx < 0)
						rx = 0;
					if(rx > X_MAX-1)
						rx = X_MAX-1;
					if(ry < 0)
						ry = 0;
					if(ry > Y_MAX-1)
						ry = Y_MAX-1;
					this.x = rx;
					this.y = ry;
					
				}
				for(int i = 0; i<a.size(); i++) {
					a.get(i).setX(x*UNIT);
					a.get(i).setY(y*UNIT);
				}
				e.consume();
				break;
				
			//Activer/desactiver les infos
			case KeyEvent.VK_A:
				if(afficherInfo)
					afficherInfo = false;
				else
					afficherInfo = true;
				break;
			
			//Texture suivante
			case KeyEvent.VK_ADD:
				if(index < a.size()-1)
					index ++;
				break;
				
			//Texture precedente
			case KeyEvent.VK_SUBTRACT:
				if(index > 0)
					index --;
				break;
			
			//Sauvegarder
			case KeyEvent.VK_S:
				int option2 = JOptionPane.showConfirmDialog(null, "Voulez-vous sauvegarder la map ?", "Sauvegarde", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(option2 == JOptionPane.OK_OPTION)
				{
					String nom = JOptionPane.showInputDialog(null, "Veuillez saisir un emplacement de fichier", "Enregistrer", JOptionPane.QUESTION_MESSAGE);
					
					
					
					try {
						PrintWriter out  = new PrintWriter(new FileWriter(nom));
						
						for(int i = 0; i<Y_MAX; i++) {
							for(int j = 0; j<X_MAX; j++) {
								String nomFichier = map[j][i].getFichier();
								for(int a = 1; a<caractereImage.length; a+=2) {
									if(caractereImage[a] == nomFichier) {
										out.print(caractereImage[a-1].charAt(0));
										
									}
								}
							}
							out.println("");
						}
						
						out.close();
					}
					catch(Exception e1) {
						e1.printStackTrace();
					}
					
				}
				break;
			
			//Charger
			case KeyEvent.VK_C:
				String fic = null;
				fic = JOptionPane.showInputDialog(null, "Veuillez selectionner un fichier a charger", "Charger", JOptionPane.QUESTION_MESSAGE);
				
				if(fic != null && !fic.isEmpty()) {
					String ligne = null;
					String fichier = null;
					char c = '.';
					FileReader fr = null;
					try {
						fr = new FileReader(fic);
					} catch (FileNotFoundException e1) {}
					BufferedReader br = new BufferedReader(fr);
					
					for(int i = 0; i<Y_MAX; i++) {
						try {
							ligne = br.readLine();
						} catch (IOException e1) {}
						for(int j = 0; j<X_MAX; j++) {
							c = ligne.charAt(j);
							for(int a = 0; a<caractereImage.length; a+=2) {
								if(c == caractereImage[a].charAt(0))
									fichier = caractereImage[a+1];
							}
							
							map[j][i] = new Case(fichier, true, false, "");
							if(map[j][i].getImage() == null)
								System.out.println("Erreur");
							
						}
					}
				}
				break;
				
			//Actier/desactiver la souris
			case KeyEvent.VK_M:
				if(mouseMove)
					mouseMove = false;
				else
					mouseMove = true;
				break;
			
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int at = e.getWheelRotation();
		if(at < 0) {
			if(index < a.size()-1)
				index ++;
		}
		if(at > 0) {
			if(index > 0)
				index --;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		if(mouseMove) {
			int mouseX = e.getX() + DECALAGE_X - 6;
			int mouseY = e.getY() + DECALAGE_Y - 3;
			
			x = (int) (Math.abs(mouseX / UNIT));
			y = (int) (Math.abs(mouseY / UNIT)-3);
			
			for(int i = 0; i<a.size(); i++) {
				a.get(i).resetVelocity();
				a.get(i).setX(x*UNIT);
				a.get(i).setY(y*UNIT);
				a.get(i).setPaused(true);
				
			}
			
			
			if(continu) {
				map[x][y] = new Case(caractereImage[index*2+1], true, false, "");
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(mouseMove) {
			int mouseX = e.getX() + DECALAGE_X - 6;
			int mouseY = e.getY() + DECALAGE_Y - 3;
			
			x = (int) (Math.abs(mouseX / UNIT));
			y = (int) (Math.abs(mouseY / UNIT)-3);
			
			for(int i = 0; i<a.size(); i++) {
				a.get(i).resetVelocity();
				a.get(i).setX(x*UNIT);
				a.get(i).setY(y*UNIT);
				a.get(i).setPaused(true);
				
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		map[x][y] = new Case(caractereImage[index*2+1], true, false, "");
		e.consume();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if(mouseMove) {
			continu = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(mouseMove) {
			continu = false;
		}
	}
	
}
