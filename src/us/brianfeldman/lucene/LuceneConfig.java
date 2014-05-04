/**
 * 
 */
package us.brianfeldman.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

/**
 * Common Lucene Configuration to be shared with the indexer and searcher.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class LuceneConfig {

	private static final String indexPath = "build/luceneIndex";
	
	private static final Version LUCENE_VERSION=Version.LUCENE_48;

	private static final Analyzer analyzer = new CustomAnalyzer(LUCENE_VERSION);
	//final Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	//final Analyzer analyzer = new NGramAnalyzer(LUCENE_VERSION, 2, 7);

	/**
	 * Get Index Path
	 * @return Directory to store index
	 */
	public static String getIndexPath(){
		return indexPath;
	}
	
	/**
	 * Get Lucene Version
	 * @return Lucene Version
	 */
	public static Version getLuceneVersion(){
		return LUCENE_VERSION;
	}

	/**
	 * Get Analyzer
	 * @return Analyzer
	 */
	public static Analyzer getAnalyzer(){
		return analyzer;
	}
}
