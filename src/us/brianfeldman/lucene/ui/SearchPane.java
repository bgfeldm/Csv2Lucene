package us.brianfeldman.lucene.ui;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.brianfeldman.lucene.ui.listeners.SearchListener;
import us.brianfeldman.lucene.ui.listeners.SearchListener.SearchType;

/**
 * Search Pane
 * 
 * Pane contains text field, search status and paging of search results.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class SearchPane extends JPanel {

	private JTextField textField;
	private JComboBox<Integer> resultsPerPage;
	private JButton searchBtn;
	private JButton previousBtn;
	private JButton nextBtn;
	private SearchWindow appBase;
	private int page = 0;
	private int totalResults = 0;
	private Integer[] pageSizes = {10, 25, 50, 100};
	private JLabel busyLabel;
	
	private SearchListener actionSearch;
	private int pageSize;
	private int pageCount;
	private String lastQueryString;

	/**
	 * Constructor
	 * 
	 * @param appBase	Application root
	 */
	public SearchPane(SearchWindow appBase){
		super();
		this.appBase = appBase;
		this.actionSearch = new SearchListener(appBase, SearchType.General);
		initUI();
	}

	private void initUI(){
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 45, 2));

		// Wrap search field with a Label so we can add the busy icon.
		Icon busyIcon = new ImageIcon( this.getClass().getResource("images/loading.gif") );
		busyLabel = new JLabel();
		busyLabel.setIcon(busyIcon);
		busyLabel.setToolTipText("Searching... Please Wait!");
		busyLabel.setVisible(false);
		this.add(busyLabel);

		JPanel searchBox = new JPanel();
		this.add(searchBox);

		textField = new JTextField("", 30);
		textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField.addActionListener(actionSearch);
		textField.setFocusable(true);
		textField.requestFocus();
		textField.setMargin(new Insets(2, 4, 2, 2));
		searchBox.add(textField);

		searchBtn = new JButton("Search");
		searchBtn.setToolTipText("Search");
		searchBtn.addActionListener(actionSearch);
		searchBtn.setMultiClickThreshhold(200);
		Icon searchIcon = new ImageIcon( this.getClass().getResource("images/zoom.png") );
		searchBtn.setIcon(searchIcon);
		searchBox.add(searchBtn);

		JPanel pagingBox = new JPanel();
		this.add(pagingBox);

		resultsPerPage = new JComboBox<Integer>(pageSizes);
		resultsPerPage.setToolTipText("Results per page");
		resultsPerPage.setEnabled(true);
		resultsPerPage.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pagingBox.add(resultsPerPage);

		previousBtn = new JButton();
		previousBtn.setToolTipText("Previous");
		Icon previousIcon = new ImageIcon( this.getClass().getResource("images/resultset_previous.png") );
		previousBtn.setIcon(previousIcon);
		previousBtn.addActionListener( new SearchListener(appBase, SearchType.PageBackward) );
		previousBtn.setMultiClickThreshhold(200);
		previousBtn.setEnabled(false);
		pagingBox.add(previousBtn);

		nextBtn = new JButton();
		nextBtn.setToolTipText("Next");
		Icon nextIcon = new ImageIcon( this.getClass().getResource("images/resultset_next.png") );
		nextBtn.setIcon(nextIcon);
		nextBtn.addActionListener( new SearchListener(appBase, SearchType.PageForward) );
		nextBtn.setMultiClickThreshhold(200);
		nextBtn.setEnabled(false);
		pagingBox.add(nextBtn);
	}

	/**
	 * Get Result Count
	 * @return  Number of Results
	 */
	public int getTotalResults(){
		return this.totalResults;
	}
	
	
	/**
	 * Get Search Result Page Number
	 * @return	Page Number for search results when paging
	 */
	public int getSearchPage(){
		return this.page;
	}

	/**
	 * Get current query for displayed results, mainly utilized for paging
	 *    
	 * @return		Query String
	 */
	public String getPageQuery(){
		return lastQueryString;
	}
	
	
	/**
	 * Get Selected page size
	 * @return		Page Size
	 */
	public int getPageSize(){
		return (int) resultsPerPage.getSelectedItem();
	}
	
	
	/**
	 * Get Search Query from text box.
	 * 
	 * @return		Text from Search Box
	 */
	public String getSearchQuery(){
		return textField.getText();
	}

	/**
	 * Set Search Text Box
	 * 
	 * @param query		Text for search box
	 */
	public void setSearchField(String query){
		textField.setText(query);
	}

	/**
	 * Enable or Disable Search Button
	 * @param bool	Boolean true/false
	 */
	public void enableSearchBtn(Boolean bool){
		searchBtn.setEnabled(bool);
	}

	/**
	 * Update values after performing a search.
	 * 
	 * @param query					Original Search String
	 * @param totalResultCount 		Total Returned Results	
	 * @param pageIndex				Page number when paging
	 */
	public void update(String query, int totalResultCount, int pageIndex){
		textField.setText(query);
		this.lastQueryString = query;
		this.page = pageIndex;
		this.totalResults = totalResultCount;
		this.pageSize = this.getPageSize();

		//int offset = page * pageSize;
		int totalPages = totalResults / pageSize;

		if (page < totalPages){
			nextBtn.setEnabled(true);
		} else {
			nextBtn.setEnabled(false);
		}

		if (page == 0){
			previousBtn.setEnabled(false);
		} else {
			previousBtn.setEnabled(true);
		}

	}

	/**
	 * setBusy
	 * 
	 * Set Busy to true when performing a search, then false when finished.
	 * 
	 * @param bool	Boolean true/false
	 */
	public void setBusy(Boolean bool){
		if (bool){
			//textField.setEnabled(false);
			searchBtn.setEnabled(false);
			busyLabel.setVisible(true);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			//textField.setEnabled(true);
			busyLabel.setVisible(false);
			searchBtn.setEnabled(true);
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

}
