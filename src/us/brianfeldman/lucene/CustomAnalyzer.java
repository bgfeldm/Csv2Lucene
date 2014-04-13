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
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class CustomAnalyzer extends Analyzer {
	
	private Version luceneVersion;

	public CustomAnalyzer(Version luceneVersion){
		super();
		this.luceneVersion = luceneVersion;
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
		result = new PorterStemFilter(result); // must be after LowerCaseFilter.
		result = new RemoveDuplicatesTokenFilter(result);
		return new TokenStreamComponents(source, result);
	}
}
