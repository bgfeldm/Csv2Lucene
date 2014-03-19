/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
 */
public class Searcher {

	private final String indexPath="build/luceneIndex";
	private IndexReader reader;
	private IndexSearcher searcher;
	private Stopwatch stopwatch = Stopwatch.createUnstarted();

	/**
	 * @throws IOException
	 */
	public Searcher() throws IOException {
	    Directory directory = NIOFSDirectory.open(new File(indexPath));
	    //reader = IndexReader.open(directory);
	    reader = DirectoryReader.open(directory);
	    searcher = new IndexSearcher(reader);
	}

	/**
	 * Find / Search Lucene
	 * 
	 * @param queryStr
	 * @return 
	 * @throws ParseException
	 * @throws IOException
	 * @throws ParseException
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 */
	public ScoreDoc[] find(String queryStr) {
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);
	    
	    ScoreDoc[] hits = null;
		try {
			Query query = new QueryParser(Version.LUCENE_47, null, analyzer).parse(queryStr);
	    	stopwatch.start();
			searcher.search(query, collector);
		    hits = collector.topDocs().scoreDocs;
		    stopwatch.stop();
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return hits;
	}

	/**
	 * Display Search Results.
	 * @param hits
	 */
	public void displayResults(ScoreDoc[] hits){
	    stopwatch.elapsed(TimeUnit.MILLISECONDS);
	    
	    System.out.println("Matching Results: "+ hits.length+" ("+stopwatch+")");
		
	    for (int i=0; i<hits.length;++i){
		       int docId = hits[i].doc;
		       
		       System.out.println("---- Document "+(i+1));
		       
				try {
					Document doc = searcher.doc(docId);
				    List<IndexableField> fields = doc.getFields();
				    for (IndexableField field: fields){
				          System.out.println(field.name() + ": "+ field.stringValue());
				    }
				} catch (IOException e) {
					e.printStackTrace();
				}

		    }
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
	 * @param args
	 * @throws IOException 
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		String queryStr=args[0];
		Searcher search = new Searcher();
		ScoreDoc[] hits = search.find(queryStr);
		search.displayResults(hits);
		search.close();
	}	
	
}

