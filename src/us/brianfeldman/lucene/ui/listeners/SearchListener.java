package us.brianfeldman.lucene.ui.listeners;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

import us.brianfeldman.lucene.SearchResults;
import us.brianfeldman.lucene.Searcher;
import us.brianfeldman.lucene.ui.SearchHistory;
import us.brianfeldman.lucene.ui.SearchWindow;

/**
 * Search Action Listener
 * 
 * Performs the search action when triggered.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class SearchListener implements ActionListener{

	private SearchWindow appBase;
	
	private SearchType searchType;
	public enum SearchType {
		General, PageForward, PageBackward, Selection, FromSearchHistory, FromSystemClipboard
	}

	/**
	 * Search Action Listener
	 * 
	 * @param appBase		Application Root
	 * @param searchType    SearchType: General, PageForward, PageBackward, Selection, FromSearchHistory, FromSystemClipboard
	 */
	public SearchListener(SearchWindow appBase, SearchType searchType){
		super();
		this.appBase = appBase;
		this.searchType = searchType;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		int pageSize = appBase.topPane.searchPane.getPageSize();
		
		String query = "";
		int page = 0;
		
		switch (searchType) {
			case General:
				query = appBase.topPane.searchPane.getSearchQuery();				
				break;

			case PageForward:
				query = appBase.topPane.searchPane.getPageQuery();
				page = appBase.topPane.searchPane.getSearchPage() + 1;
				break;

			case PageBackward:
				query = appBase.topPane.searchPane.getPageQuery();
				page = appBase.topPane.searchPane.getSearchPage() - 1;
				break;
				
			case Selection:
				query = appBase.contentPane.getSelectedText();
				break;
				
			case FromSearchHistory:
				query = ((JMenuItem) e.getSource()).getText(); // appBase.mainMenuBar.
				break;
				
			case FromSystemClipboard:
				query = this.getClipboardContents();
				break;
		}

		// Return if Empty Query.
		if (query.trim().length() < 1){
			return;
		}

		appBase.contentPane.setBusy(true);
		appBase.topPane.searchPane.setBusy(true);

		String html = null;
		try {
			Searcher search = new Searcher();
			SearchResults results = search.find(query, page, pageSize);
			html = results.toHtml();
			SearchHistory.addQuery(query);
			appBase.topPane.searchPane.update(results.getQuery(), results.getTotalCount(), results.getPage());
			search.close();
		}
		catch (ParseException | IOException exception) {
			JOptionPane.showMessageDialog(appBase, "Error searching due to "+exception.getLocalizedMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
			exception.printStackTrace();
		}
		
		if (html != null){
			appBase.contentPane.setText(html.toString());
		}

		appBase.contentPane.setBusy(false);
		appBase.topPane.searchPane.setBusy(false);
	}


	private String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException | IOException ex){
				ex.printStackTrace();
			}
		}
		return result;
	}
}
