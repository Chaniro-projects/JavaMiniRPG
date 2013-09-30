/**
 * @author Bastien Baret
 */
package Principal;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;


public class ScreenManager {

	private GraphicsDevice vc;
	
	public ScreenManager() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		vc = e.getDefaultScreenDevice();
		
	}
	
	public DisplayMode[] getCompatibleDisplayMode() {
		return vc.getDisplayModes();
	}
	
	public DisplayMode findFirstCompatibleMode(DisplayMode[] modes) {
		DisplayMode goodModes[] = vc.getDisplayModes();
		for(int i = 0;i<modes.length;i++) {
			for(int j = 0; j<goodModes.length; j++) {
				if(displayModesMatch(modes[i], goodModes[j])) {
					return modes[i];
				}
			}
		}
		return null;
	}
	
	public DisplayMode getCurretnDisplayMode() {
		return vc.getDisplayMode();
	}
	
	public boolean displayModesMatch(DisplayMode m1, DisplayMode m2) {
		if(m1.getWidth() != m2.getWidth() || m1.getHeight() != m2.getHeight()) {
			return false;
		}
		if(m1.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI && m2.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI && m1.getBitDepth() != m2.getBitDepth()) {
			return false;
		}
		if(m1.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN && m2.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN && m1.getRefreshRate() != m2.getRefreshRate()) {
			return false;
		}
		
		return true;
	}
	
	public void setFullScreen(DisplayMode dm) {
		JFrame f = new JFrame();
		f.setUndecorated(true);
		f.setIgnoreRepaint(true);
		f.setResizable(false);
		vc.setFullScreenWindow(f);
		
		if(dm != null && vc.isDisplayChangeSupported()) {
			try {
				vc.setDisplayMode(dm);
			}catch(Exception e) {}
		}
		
		f.createBufferStrategy(4); 
	}
	
	public Graphics2D getGraphics() {
		Window w = vc.getFullScreenWindow();
		if(w != null) {
			BufferStrategy s = w.getBufferStrategy();
			return (Graphics2D)s.getDrawGraphics();
		}
		else
			return null;
	}
	
	public void update() {
		Window w = vc.getFullScreenWindow();
		if(w != null) {
			BufferStrategy s = w.getBufferStrategy();
			if(!s.contentsLost()) {
				s.show();
			}
		}
	}
	
	public Window getFullScreenWindow() {
		return vc.getFullScreenWindow();
	}
	
	public int getWidth() {
		Window w = vc.getFullScreenWindow();
		if(w != null)
			return w.getWidth();
		else
			return 0;
	}
	
	public int getHeight() {
		Window w = vc.getFullScreenWindow();
		if(w != null)
			return w.getHeight();
		else
			return 0;
	}
	
	public void restaureScreen() {
		Window w = vc.getFullScreenWindow();
		if(w != null)
			w.dispose();
		vc.setFullScreenWindow(null);
	}
	
	public BufferedImage createCompatibleImage(int w, int h, int t) {
		Window win = vc.getFullScreenWindow();
		if(win != null) {
			GraphicsConfiguration gc = win.getGraphicsConfiguration();
			return gc.createCompatibleImage(w, h, t);
		}
		return null;
	}
	
}
