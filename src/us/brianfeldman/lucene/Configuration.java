/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.util.Version;

/**
 * Lucene Configuration
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class Configuration {

	private static Configuration instance;

	private final String PROPERTY_FILE = "config/csv2lucene.properties";

	private final Version LUCENE_VERSION = Version.LUCENE_48;

	private final Analyzer analyzer = new CustomAnalyzer(LUCENE_VERSION);
	//final Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	//final Analyzer analyzer = new NGramAnalyzer(LUCENE_VERSION, 2, 7);

	private String indexPath = "build/luceneIndex";

	private Operator defaultSearchOperator = Operator.OR;

	private double indexerCPUmultiplier = 2.0;

	private String[] indexerFilenameSuffixes = {"csv"};

	private String defaultSearchField = "_ALL";


	/**
	 * Configuration singleton.
	 */
	public Configuration(){
		// empty.
	}

	/**
	 * Get Instance.
	 * 
	 * @return
	 */
	public static Configuration getInstance(){
		if (instance == null){
			instance = new Configuration();
		}
		return instance;
	}

	/**
	 * Get Index Path
	 * @return Directory to store index
	 */
	public String getIndexPath(){
		return indexPath;
	}

	/**
	 * Get Lucene Version
	 * @return Lucene Version
	 */
	public Version getLuceneVersion(){
		return LUCENE_VERSION;
	}

	/**
	 * Get Analyzer
	 * @return Analyzer
	 */
	public Analyzer getAnalyzer(){
		return analyzer;
	}

	/**
	 * Get Default Search Operator
	 * 
	 * @return the defaultSearchOperator
	 */
	public Operator getDefaultSearchOperator() {
		return defaultSearchOperator;
	}

	/**
	 * @return the defaultSearchField
	 */
	public String getDefaultSearchField() {
		return defaultSearchField;
	}	

	/**
	 * Get IndexWriterConfig
	 * 
	 * @return IndexWriterConfig	Configuration for the IndexWriter.
	 */
	public IndexWriterConfig getIndexWriterConfig(){
		IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_VERSION, analyzer);
		
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

		// Optional: for better indexing performance, increase the RAM buffer.  
		// But if you do this, increase the max heap size to the JVM (eg add -Xmx512m or -Xmx1g):
		iwc.setRAMBufferSizeMB(128);   // lucene's default is 16 MB.
		iwc.setMaxThreadStates(16);  // default is 8.  Max threads communicating with the writer.
		iwc.setUseCompoundFile(false);
		iwc.setWriteLockTimeout(5000);
		
		return iwc;
	}

	/**
	 * @return the indexerCpuMultiplier
	 */
	public double getIndexerCpuMultiplier() {
		return indexerCPUmultiplier;
	}

	/**
	 * @return the indexFilenameSuffixes
	 */
	public String[] getIndexFilenameSuffixes() {
		return indexerFilenameSuffixes;
	}


	/**
	 * Load Property File
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void load() throws FileNotFoundException, IOException{
         Properties props = new Properties();
         props.load(new FileReader(PROPERTY_FILE));
         
         indexPath = props.getProperty("index.path", indexPath);
         indexerCPUmultiplier = Double.valueOf( props.getProperty("indexer.cpu.multiplier", String.valueOf(indexerCPUmultiplier) ) );
          
         String suffixes = props.getProperty("indexer.filename.suffixes");
         if (suffixes != null){
        	 indexerFilenameSuffixes = suffixes.split(",");
         }
         
         defaultSearchField = props.getProperty("search.default.field", defaultSearchField);
         String searchOperator = props.getProperty("search.default.operator");
         if (searchOperator.toUpperCase() == "AND"){
        	 defaultSearchOperator = Operator.AND;
         } else {
        	 defaultSearchOperator = Operator.OR;
         }
         
	}
}
