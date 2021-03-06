/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Ngram will create a larger index but allow for better partial word searches than Wildcard searches.
 * - Ngrams provide the functionality needed for Search as you type.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class NGramAnalyzer extends Analyzer {
	
	private static final Configuration config = Configuration.getInstance();
	
	private int minGramSize = 2;
	private int maxGramSize = 5;

	/**
	 * Constructor
	 * 
	 * @param luceneVersion
	 * @param minGramSize	Minimum nGram size
	 * @param maxGramSize	Maximum nGram size
	 */
	public NGramAnalyzer(int minGramSize, int maxGramSize){
		super();
		this.minGramSize = minGramSize;
		this.maxGramSize = maxGramSize;
	}

	@Override
	protected TokenStreamComponents  createComponents(String fieldName, Reader reader) {
		final Tokenizer source = new StandardTokenizer(reader);
		TokenStream result = new StandardFilter(source);
		result = new ASCIIFoldingFilter(result);  //Convert UNICODE to ASCII.
		//result = new SynonymFilter(result, synonyms, false);
		result = new StopFilter(result, config.getStopWords());		
		result = new WordDelimiterFilter(result, WordDelimiterFilter.CATENATE_WORDS, null);
		result = new LowerCaseFilter(result);

		result = new NGramTokenFilter(result, minGramSize, maxGramSize);
		//result = new EdgeNGramTokenFilter(result, minGramSize, maxGramSize);

		result = new RemoveDuplicatesTokenFilter(result);
		return new TokenStreamComponents(source, result);
	}
}
