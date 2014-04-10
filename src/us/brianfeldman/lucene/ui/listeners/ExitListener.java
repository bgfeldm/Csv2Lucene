package us.brianfeldman.lucene.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ExitListener
 * 
 * Exits the Application when triggered.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @TODO implement graceful shutdown.
 */
public class ExitListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}
}