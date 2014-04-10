/**
 * 
 */
package us.brianfeldman.lucene.ui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;

import us.brianfeldman.lucene.ui.listeners.ExitListener;
import us.brianfeldman.lucene.ui.listeners.SearchListener;
import us.brianfeldman.lucene.ui.listeners.SearchListener.SearchType;
import us.brianfeldman.lucene.ui.listeners.WindowStateTray;

/**
 * SearchWindow
 * 
 * The main UI application class.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class SearchWindow extends JFrame {

	public final static String TITLE = "CSV2Lucene Search";
	public final static String DEVELOPER = "Brian Feldman\n          bgfeldm@yahoo.com";
	public final static String VERSION = "0.0.1";

	public final static SystemTray tray = SystemTray.getSystemTray();
	public TrayIcon trayIcon;
	public MainMenuBar mainMenuBar;
	public TopPane topPane;
	public HtmlPane contentPane;
	public JScrollPane contentScrollPane;
	public FooterPane footerPanel;
	private Image appIcon;

	public SearchWindow(){

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("Menu.margin", new Insets(0, 2, 0, 2));
		} catch (Exception e) {
			//ignore.
		}
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle(TITLE);
		this.setSize(900, 500);
		this.setResizable(true);

		appIcon = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("images/report.png"));
		this.setIconImage(appIcon);
		
		try {
			sysTray();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		// Find Center.
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension dim = tk.getScreenSize();
		int xPos = (dim.width / 2) - (this.getWidth() / 2);
		int yPos = (dim.height / 2) - (this.getHeight() / 2);
		this.setLocation(xPos, yPos);

		// Menu.
		mainMenuBar = new MainMenuBar(this);
		this.setJMenuBar(mainMenuBar);

		// Side Panel.
		//JPanel sidePanel = new JPanel();
		//sidePanel.setPreferredSize(new Dimension(200, 300));

		topPane = new TopPane(this);

		//contentPane = new WebPanel();
		contentPane = new HtmlPane(this);
		contentScrollPane = contentPane.getScrollPane();
		
		footerPanel = new FooterPane(this);
		
		// Layout.
		JPanel rightPanels = new JPanel();
		rightPanels.setLayout(new BoxLayout(rightPanels, BoxLayout.Y_AXIS));
		rightPanels.add(topPane);
		rightPanels.add(contentScrollPane);
		rightPanels.add(footerPanel);
		this.add(rightPanels);

		/*
		JSplitPane leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, searchPanels);
		leftRightSplit.setDividerLocation(150);
		this.add(leftRightSplit);
		*/

		/*
		JFileChooser jfc = new javax.swing.JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		jfc.showOpenDialog(null);
		File docDir = jfc.getSelectedFile();
		
		if (!docDir.exists() || !docDir.canRead()) {
			doc = new JLabel("Index directory:"+docDir.getAbsolutePath());
			panelLabel.add(doc);
		}
		
		 //Word documents with Tika parser.
			    	    ContentHandler contenthandler = new BodyContentHandler();
			      	    Metadata metadata = new Metadata();
	    				metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
	    				Parser parser = new AutoDetectParser();
	    				parser.parse(fis, contenthandler, metadata, new ParseContext());
		*/


		pack();
		this.setVisible(true);
	}
	
	
	private void sysTray() throws AWTException
	{
		if ( ! SystemTray.isSupported() ){ 
			return; 
		}

		// Tray icon menu.
		PopupMenu trayMenu = new PopupMenu();

        ActionListener showWindow = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            }
        };

        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(showWindow);
        trayMenu.add(openItem);
        
        MenuItem searchItem = new MenuItem("Search using copied text");
        searchItem.addActionListener(new SearchListener(this, SearchType.FromSystemClipboard));
        searchItem.addActionListener(showWindow);
        trayMenu.add(searchItem);

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ExitListener());
        trayMenu.add(exitItem);
        
		trayIcon = new TrayIcon(appIcon, this.getTitle());
		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(trayMenu);
		
		final JFrame frame = this;
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					frame.setVisible(true);
					frame.setExtendedState(JFrame.NORMAL);
				}
			}
		});
		//tray.add(trayIcon);

		this.addWindowStateListener(new WindowStateTray(this));
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	SearchWindow appWindow = new SearchWindow();
	        }
	    });
	}
}
