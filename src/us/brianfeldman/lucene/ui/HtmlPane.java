package us.brianfeldman.lucene.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

//import javafx.scene.web.WebView;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import us.brianfeldman.lucene.ui.listeners.PopupClickListener;

/**
 * Html Content Pane
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 * @TODO switch to Java Pane with supports full HTML/CSS.
 */
public class HtmlPane extends JEditorPane {

	private JScrollPane htmlScrollPane;	
	private SearchWindow appBase;
	
	/**
	 * Constructor
	 * @param appBase	Application Root
	 */
	public HtmlPane(SearchWindow appBase){
		super();
		this.appBase = appBase;
		initUI();
	}

	private void initUI(){		
		this.setEditable(false);
		this.setContentType("text/html");
		this.setOpaque(true);
		htmlScrollPane = new JScrollPane(this);
		htmlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		htmlScrollPane.setPreferredSize(new Dimension(800, 500));
		htmlScrollPane.setMinimumSize(new Dimension(800, 500));
		htmlScrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		this.setMargin(new Insets(15, 15, 15, 15));
		
		// Add right click context menu.
		this.addMouseListener(new PopupClickListener(appBase)); 
		
		// Hack to allow text selection.
		this.addFocusListener(new FocusListener() {
	        @Override
	        public void focusLost(FocusEvent e) {
	        	((JTextComponent) e.getSource()).setEditable(true);
	        }

	        @Override
	        public void focusGained(FocusEvent e) {
	        	((JTextComponent) e.getSource()).setEditable(true);
	        }
	    });

		// Disable scrolling down to text bottom after insert.
		DefaultCaret caret = (DefaultCaret) this.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}

	/**
	 * Get Outer Scroll Pane
	 * 
	 * @return JSCrollPane
	 */
	public JScrollPane getScrollPane(){
		return htmlScrollPane;
	}

	/**
	 * Set Busy
	 * 
	 * @param bool	Boolean True/Fase
	 */
	public void setBusy(boolean bool){
		if (bool){
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}
}
