/**
 * 
 */
package us.brianfeldman.lucene.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

/**
 * AboutListener
 * 
 * Opens the About Dialog when triggered.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 */
public class AboutListener implements ActionListener {
	
	private String title;
	private String version;
	private String developedBy;

	/**
	 * Settings for About Dialog.
	 * 
	 * @param title			Application title
	 * @param version		Application version
	 * @param developedBy	Application developer
	 */
	public AboutListener(String title, String version, String developedBy){
		this.title = title;
		this.version = version;
		this.developedBy = developedBy;
	}

    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, title+"\nVersion "+version+"\nDeveloped by "+developedBy);
    }
}
