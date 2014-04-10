package us.brianfeldman.lucene.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import us.brianfeldman.lucene.ui.listeners.SearchListener;
import us.brianfeldman.lucene.ui.listeners.SearchListener.SearchType;

/**
 * Right Click Context Menu
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class RightClickMenu extends JPopupMenu {

	private SearchWindow appBase;
	
	/**
	 * Constructor
	 * 
	 * @param appBase	Application root
	 */
	public RightClickMenu(SearchWindow appBase){
		super();
		this.appBase = appBase;
		initUI();
	}

	private void initUI(){
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener( new ActionListener(){	
			public void actionPerformed(ActionEvent e) {
				appBase.contentPane.copy();
			}
		});
		this.add(copyItem);

		JMenuItem searchItem = new JMenuItem("Search");
		searchItem.addActionListener(new SearchListener(appBase, SearchType.Selection));
		this.add(searchItem);
	}

}