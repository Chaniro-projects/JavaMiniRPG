/**
 * @author Bastien Baret
 */
package Principal;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Game {
	
	private JFrame fen;
	private Graphics buffer;
	private BufferStrategy strategy;
	private static int X_MAX;
	private static int Y_MAX;
	private float VELOCITY_Y;
	private float VELOCITY_X;
	private static final float UNIT = 16;
	private static int DECALAGE_X = 3;
	private static int DECALAGE_Y = 25;
	private ArrayList<Thread> thread;
	
	public static void main(String[] args) {
		Game g = new Game();
		g.run();
	}
	
	public Game() {
		thread = new ArrayList<Thread>();
		thread.add(Thread.currentThread());
		
		fen = new JFrame("Test");
		
		X_MAX = Math.abs(800/(int)UNIT)-1;
		Y_MAX = Math.abs(600/(int)UNIT)-1;
		fen.setSize((int)(X_MAX*UNIT+DECALAGE_X), (int)(Y_MAX*UNIT+DECALAGE_Y)+DECALAGE_X);
		
		fen.setTitle("Jeu");
		fen.setLocationRelativeTo(null);
		fen.setIgnoreRepaint( true ); 
		fen.setResizable(false);
		fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fen.setFocusTraversalKeysEnabled(false);
		fen.setVisible(true);
		
		//Double buffering
		fen.createBufferStrategy(2);		
		strategy = fen.getBufferStrategy(); 
	    buffer = strategy.getDrawGraphics();
	}
	
	public void run() {
		boolean running = true;
		
		while(running) {
			Menu m = new Menu(fen, thread);
			m.run();
			if(m.isGo()) {
				Interface i = new Interface(fen, thread);
				i.run();
				if(i.isRetourMenu()) {
					
				}
				else {
					strategy.dispose();
					buffer.dispose();
					running = false;
					fen.dispose();
				}
			}
			else {
				buffer.dispose();
				strategy.dispose();
				fen.dispose();
				running = false;
			}
		}
	}
}
