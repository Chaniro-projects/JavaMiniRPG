import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JPanel;


public abstract class Core extends JFrame{
	
	Graphics buffer;
	protected boolean running;
	BufferStrategy strategy;
	
	public void stop() {
		running = false;
	}
	
	public void run() {
		try{
			init();
			gameLoop();
		}finally {
			strategy.dispose();
			buffer.dispose();
			//dispose();
		}
	}
	
	public void init() {
		running = true;
	}
	
	public void gameLoop() {
		long startingTime = System.currentTimeMillis();
		long cumTime = startingTime;
		
		while(running) {
			long timePassed = System.currentTimeMillis() - cumTime;
			cumTime += timePassed;
			
			update(timePassed);
			
			draw(buffer);
			strategy.show();
			
			try{
				Thread.sleep(20);
			}catch(Exception e) {}
			
		}
	}
	
	public void update(long timePassed) {
		
	}
	
	public void draw(Graphics g) {
		
	}
	
}
