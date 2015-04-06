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
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.Version;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class CustomAnalyzer extends Analyzer {
	
	private static final Configuration config = Configuration.getInstance();

	@Override
	protected TokenStreamComponents  createComponents(String fieldName, Reader reader) {
		final Tokenizer source = new StandardTokenizer(reader);
		TokenStream result = new StandardFilter(source);
		
		result = new ASCIIFoldingFilter(result);  //Convert UNICODE to ASCII.
		result = new SynonymFilter(result, config.getSynonymMap(), true);
		result = new StopFilter(result, config.getStopWords());
		result = new WordDelimiterFilter(result, WordDelimiterFilter.CATENATE_WORDS, null);
		result = new LowerCaseFilter(result);
		result = new PorterStemFilter(result); // must be after LowerCaseFilter.
		
		result = new RemoveDuplicatesTokenFilter(result);
		return new TokenStreamComponents(source, result);
	}
}
