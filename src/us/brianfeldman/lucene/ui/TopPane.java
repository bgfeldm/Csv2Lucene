package us.brianfeldman.lucene.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TopPane extends JPanel {

	private SearchWindow appBase;
	public SearchPane searchPane;

	public TopPane(SearchWindow appBase){
		super();
		this.appBase = appBase;
		initUI();
	}
	
	private void initUI(){
		this.setMinimumSize(new Dimension(800, 100));
		this.setMaximumSize(new Dimension(1500, 100));
		this.setLayout(new GridLayout());

		searchPane = new SearchPane(appBase);
		this.add(searchPane);
	}

}
