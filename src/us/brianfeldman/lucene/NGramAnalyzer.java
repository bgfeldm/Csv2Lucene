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
	
	private Version luceneVersion;
	private int minGramSize = 2;
	private int maxGramSize = 5;

	/**
	 * Constructor
	 * 
	 * @param luceneVersion
	 * @param minGramSize	Minimum nGram size
	 * @param maxGramSize	Maximum nGram size
	 */
	public NGramAnalyzer(Version luceneVersion, int minGramSize, int maxGramSize){
		super();
		this.luceneVersion = luceneVersion;
		this.minGramSize = minGramSize;
		this.maxGramSize = maxGramSize;
	}

	@Override
	protected TokenStreamComponents  createComponents(String fieldName, Reader reader) {
		final Tokenizer source = new StandardTokenizer(luceneVersion, reader);
		TokenStream result = new StandardFilter(luceneVersion, source);
		result = new ASCIIFoldingFilter(result);  //Convert UNICODE to ASCII.
		//result = new SynonymFilter(result, synonyms, false);
		result = new StopFilter(luceneVersion, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		result = new WordDelimiterFilter(result, WordDelimiterFilter.CATENATE_WORDS, null);
		result = new LowerCaseFilter(luceneVersion, result);

		result = new NGramTokenFilter(luceneVersion, result, minGramSize, maxGramSize);
		//result = new EdgeNGramTokenFilter(luceneVersion, result, minGramSize, maxGramSize);

		result = new RemoveDuplicatesTokenFilter(result);
		return new TokenStreamComponents(source, result);
	}
}
