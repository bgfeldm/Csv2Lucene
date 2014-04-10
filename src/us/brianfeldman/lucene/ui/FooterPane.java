package us.brianfeldman.lucene.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * Footer pane, the pane at the bottom of the screen.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class FooterPane extends JPanel {

	private SearchWindow appBase;
	
	/**
	 * Constructor
	 * 
	 * @param appBase	Application Root
	 */
	public FooterPane(SearchWindow appBase){
		super();
		this.appBase = appBase;
		initUI();
	}

	private void initUI(){
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.setMinimumSize(new Dimension(800, 200));
		this.setMaximumSize(new Dimension(1900, 200));
		this.setLayout(new BorderLayout());
		
		JPanel metricPane = new JPanel();
		metricPane.setMaximumSize(new Dimension(100, 200));

		/*
		 * @TODO implement showing of search counts.
		 */
		JLabel count1 = new JLabel("");
		metricPane.add(count1);
		metricPane.add(new JSeparator(SwingConstants.VERTICAL));
		JLabel count2 = new JLabel("");
		metricPane.add(count2);
		this.add(metricPane, BorderLayout.CENTER);
	}

}
