package us.brianfeldman.lucene.ui.listeners;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * ExternalOpenURLLisener
 * 
 * Opens a website url in the default system web browser.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @TODO replace with an internal search help guide. 
 */
public class ExternalOpenURLLisener implements ActionListener {

	private String urlString;

	public ExternalOpenURLLisener(String urlString){
		this.urlString = urlString;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		openWebpage(this.urlString);
	}
	
	public static void openWebpage(String urlString) {
	    try {
	        Desktop.getDesktop().browse(new URL(urlString).toURI());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
}
