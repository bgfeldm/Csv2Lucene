
/**
 * 
 */
package us.brianfeldman.lucene.ui;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import us.brianfeldman.lucene.ui.listeners.AboutListener;
import us.brianfeldman.lucene.ui.listeners.ExitListener;
import us.brianfeldman.lucene.ui.listeners.ExternalOpenURLLisener;
import us.brianfeldman.lucene.ui.listeners.SearchListener;
import us.brianfeldman.lucene.ui.listeners.SearchListener.SearchType;

/**
 * Application Menu Bar
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class MainMenuBar extends JMenuBar {

	private static SearchWindow appBase;
	private static JMenu historyMenu;
	
	/**
	 * Constructor
	 * @param appBase	Application root
	 */
	public MainMenuBar(SearchWindow appBase){
		super();
		this.appBase = appBase;
		initUI();
	}

	private void initUI(){
		JMenu fileMenu  = new JMenu( "File" );
		fileMenu.setMnemonic(KeyEvent.VK_F);  // ALT-F.
		this.add(fileMenu);
		
		historyMenu  = new JMenu( "History" );
		historyMenu.add(new JMenuItem("--empty--"));
		this.add(historyMenu);
		
		JMenu helpMenu  = new JMenu( "Help" );
		helpMenu.setMnemonic(KeyEvent.VK_H); // ALT-H.
		this.add(helpMenu);

		/*
		 * @TODO implement index config pane.
		 * @TODO implement kicking off indexing. 
		 * 
		JMenuItem openItem  = new JMenuItem( "Open" );
		fileMenu.add( openItem );
		
		JMenuItem indexItem = new JMenuItem( "Create Index" );
		Icon indexIcon = new ImageIcon( this.getClass().getResource("images/database_add.png") );
		indexItem.setIcon(indexIcon);
		fileMenu.add( indexItem );
		*/
		
		JMenuItem tipsItem = new JMenuItem( "Search Query Syntax" );
		Icon helpIcon = new ImageIcon( this.getClass().getResource("images/help.png") );
		tipsItem.setIcon(helpIcon);
		tipsItem.setMnemonic(KeyEvent.VK_F1);
		tipsItem.addActionListener(new ExternalOpenURLLisener("https://lucene.apache.org/core/4_7_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html"));
		helpMenu.add(tipsItem);

		JMenuItem aboutItem = new JMenuItem( "About " + appBase.TITLE);
		aboutItem.addActionListener(new AboutListener(appBase.TITLE, appBase.VERSION, appBase.DEVELOPER));
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);

		JMenuItem exitItem  = new JMenuItem( "Exit" );
		exitItem.addActionListener(new ExitListener());
		fileMenu.addSeparator();
		fileMenu.add( exitItem ); 
	}

	/**
	 * Update History Menu List from SearchHistory object
	 * 
	 * @see SearchHistory
	 */
	public static void updateHistoryMenu(){
		List<String> history = SearchHistory.getList();
		historyMenu.removeAll();

		for(int i=SearchHistory.getList().size()-1; i >= 0; i--){ // newest on top.
		//for(int i=0; SearchHistory.getList().size() > i; i++){  // oldest on top.
			JMenuItem mitem = new JMenuItem( history.get(i) );
			mitem.addActionListener(new SearchListener(appBase, SearchType.FromSearchHistory));
			historyMenu.add(mitem);
		}
	}
}
