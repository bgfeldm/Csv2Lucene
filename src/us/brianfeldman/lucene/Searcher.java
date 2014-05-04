/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;







import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.base.Stopwatch;

/**
 * Search Lucene Index
 *
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 * 
 * @link https://lucene.apache.org/core/4_7_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html
 */


public class Searcher {

	private static Version LUCENE_VERSION=LuceneConfig.getLuceneVersion();

	private final String indexPath=LuceneConfig.getIndexPath();
	
	private static String DEFAULT_SEARCH_FIELD = "_ALL";
	
	private IndexReader reader;
	private IndexSearcher searcher;
	private Stopwatch stopwatch = Stopwatch.createUnstarted();
	private int indexDocumentCount;

	private static char[] COMPLETE_ESCAPE_CHARS = {'+','-','&','|','(',')','{','}','[',']','^','"','~','*','?',':','\\'};
	private static char[] SAFE_ESCAPE_CHARS = {'+','-','&','|','~','*','?'};
	
	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public Searcher() throws IOException {
		Directory directory = NIOFSDirectory.open(new File(indexPath));
		//reader = IndexReader.open(directory);
		reader = DirectoryReader.open(directory);
		indexDocumentCount = reader.numDocs();
		searcher = new IndexSearcher(reader);
	}
	
	public IndexSearcher getIndexSearcher(){
		return this.searcher;
	}

	public Set<String> getSearchableFields() throws IOException{
		Set<String> fields = new LinkedHashSet<String>();
		IndexReader reader = searcher.getIndexReader(); 

		for (AtomicReaderContext rc : reader.leaves()) { 
		AtomicReader ar = rc.reader(); 
		FieldInfos fis = ar.getFieldInfos(); 
		for (FieldInfo fi : fis)
			if (fi.isIndexed()){
				fields.add(fi.name);
			}
		}

		reader.close();
		
		return fields;
	}
	
	
	/**
	 * Find / Search Lucene
	 * 
	 * @param queryStr
	 * @param recordsPerPage 
	 * @param page 
	 * @return SearchResults
	 * @throws ParseException
	 * @throws IOException
	 * @throws ParseException
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 */
	public SearchResults find(String queryStr, int page, int pageSize) throws ParseException, IOException {
		
		final Analyzer analyzer = LuceneConfig.getAnalyzer();

		int offset = page * pageSize;
		TopScoreDocCollector collector = TopScoreDocCollector.create(offset+pageSize, true);
		
		QueryParser qparser = new QueryParser(LUCENE_VERSION, DEFAULT_SEARCH_FIELD, analyzer);
		qparser.setAllowLeadingWildcard(true);
		qparser.setDefaultOperator(Operator.OR);

		Query query = qparser.parse(queryStr);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int totalHits = collector.getTotalHits();

		SearchResults results = new SearchResults(queryStr, totalHits, page);
		int count = Math.min(hits.length - offset, pageSize);
		for (int i = 0; i < count; ++i) {
			int docId = hits[offset+i].doc;
			Document doc = searcher.doc(docId);
			results.addDocument(doc);
		}

		return results;
	}
	


	/**
	 * Close
	 * @throws IOException
	 */
	public void close() throws IOException{
		if (reader != null){
			//searcher.close();
			reader.close();
		}
	}

	/**
	 * Escape Query string characters
	 *   + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	 * 
	 * @param query
	 * @return
	 */
	public static String escapeQuery(String query){
		for(int i=0; i < SAFE_ESCAPE_CHARS.length; i++){
			query = query.replace(""+SAFE_ESCAPE_CHARS[i], ""+'\\'+SAFE_ESCAPE_CHARS[i]);
		}
		return query;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		String queryStr=args[0];
		Searcher search = new Searcher();
		SearchResults results = search.find(queryStr, 25, 0);
		results.stdout();

		// Searchable Fields.
		Set<String> fields = search.getSearchableFields();
		System.out.println(fields);

		search.close();
	}

}

