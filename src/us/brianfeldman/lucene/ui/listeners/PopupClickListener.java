package us.brianfeldman.lucene.ui.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import us.brianfeldman.lucene.ui.RightClickMenu;
import us.brianfeldman.lucene.ui.SearchWindow;

/**
 * PopupClickListener
 * 
 * Opens the Right Click Menu when triggered.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 */
public class PopupClickListener extends MouseAdapter {

	private SearchWindow appBase;

	public PopupClickListener(SearchWindow appBase){
		super();
		this.appBase = appBase;
	}

    public void mousePressed(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    public void mouseReleased(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    private void doPop(MouseEvent e){
    	RightClickMenu menu = new RightClickMenu(appBase);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}