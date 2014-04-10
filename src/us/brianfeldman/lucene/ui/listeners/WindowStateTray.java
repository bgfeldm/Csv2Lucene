package us.brianfeldman.lucene.ui.listeners;

import java.awt.AWTException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import us.brianfeldman.lucene.ui.SearchWindow;

/**
 * WindowStateTray
 * 
 * Instead of minimizing to the Taskbar move to the System Tray.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class WindowStateTray implements WindowStateListener {
	
	private SearchWindow appBase;
	
	public WindowStateTray(SearchWindow appBase){
		this.appBase = appBase;
	}
	
	public void windowStateChanged(WindowEvent e) {
		if(e.getNewState() == appBase.ICONIFIED){
			try {
				appBase.tray.add(appBase.trayIcon);
				appBase.setVisible(false);
			} catch (AWTException ex) {
				System.out.println("unable to add to tray");
			}
		}
		if(e.getNewState() == 7){
			try{
				appBase.tray.add(appBase.trayIcon);
				appBase.setVisible(false);
			}catch(AWTException ex){
				System.out.println("unable to add to system tray");
			}
		}
		if(e.getNewState() == appBase.MAXIMIZED_BOTH){
			appBase.tray.remove(appBase.trayIcon);
			appBase.setVisible(true);
		}
		if(e.getNewState() == appBase.NORMAL){
			appBase.tray.remove(appBase.trayIcon);
			appBase.setVisible(true);
		}
	}
}
