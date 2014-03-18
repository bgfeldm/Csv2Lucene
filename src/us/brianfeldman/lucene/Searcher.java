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
	 * @throws ParseException
	 * @throws IOException
	 * @throws ParseException
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 */
	public void find(String queryStr) throws IOException, ParseException {
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
	    Query query = new QueryParser(Version.LUCENE_47, null, analyzer).parse(queryStr);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);

	    Stopwatch stopwatch = Stopwatch.createStarted();
	    
	    searcher.search(query, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    stopwatch.stop();
	    long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
	    
	    System.out.println("Matching Results: "+ hits.length+" ("+stopwatch+")");
	    
	    for (int i=0; i<hits.length;++i){
	       int docId = hits[i].doc;
	       
	       System.out.println("---- Document "+(i+1));
	       
	       Document doc = searcher.doc(docId);
	       List<IndexableField> fields = doc.getFields();
	       for (IndexableField field: fields){
	             System.out.println(field.name() + ": "+ field.stringValue());
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
		search.find(queryStr);
		search.close();
	}	
	
}

