/**
 * 
 */
package us.brianfeldman.lucene.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Search History
 * 
 * Holds and retrieves search history.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 * @TODO persist search history
 */
public class SearchHistory {

	private static List<String> searchQueries = new ArrayList<String>();

	private static int MAX_HISTORY_SIZE = 15;

	public static void addQuery(String query){
		if (searchQueries.size() >= MAX_HISTORY_SIZE){
			searchQueries.remove(0);
		}
		int previousMatch = searchQueries.indexOf(query);
		if (previousMatch != -1){
			searchQueries.remove(previousMatch);
		}

		searchQueries.add(query);
		MainMenuBar.updateHistoryMenu();
	}

	public static String previousQuery(){
		return searchQueries.get(searchQueries.size()-1);
	}

	public static List<String> getList(){
		return searchQueries;
	}

}
