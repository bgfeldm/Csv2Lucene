/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene Configuration
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class Configuration {

	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
	
	private static Configuration instance;

	private final static String PROPERTY_FILE = "config/csv2lucene.properties";
	
	private String synonymFile ="config/synonyms.txt";

	private String stopWordFile ="config/stopwords.txt";

	private final Version LUCENE_VERSION = Version.LUCENE_48;

	private final Analyzer analyzer = new CustomAnalyzer(LUCENE_VERSION);
	//final Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	//final Analyzer analyzer = new NGramAnalyzer(LUCENE_VERSION, 2, 7);

	private String indexPath = "build/luceneIndex";

	private Operator defaultSearchOperator = Operator.OR;

	private double indexerCPUmultiplier = 2.0;

	private String[] indexerFilenameSuffixes = {"csv"};

	private String defaultSearchField = "_ALL";
	
	private SynonymMap synonyms;
	


	/*
	 * Configure Logging.
	 */
	static {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("config/log4j.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(props);
	}


	private CharArraySet STOP_WORDS;

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
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static Configuration getInstance(){
		if (instance == null){
			instance = new Configuration();
			try {
				instance.load();
			} catch (IOException e) {
				LOG.error("Failed to load property file '{}'", PROPERTY_FILE, e);
			}
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
         if (searchOperator.equalsIgnoreCase("AND")){
        	 defaultSearchOperator = Operator.AND;
         } else {
        	 defaultSearchOperator = Operator.OR;
         }
         
	}

	/**
	 * Get StopWords
	 * 
	 * @return Stop Words
	 * @throws FileNotFoundException 
	 */
	public CharArraySet getStopWords(){
		if (STOP_WORDS == null){
			buildStopWords();
		}
		return STOP_WORDS;
	}

	/**
	 * Build Stop Words.
	 * @throws FileNotFoundException 
	 */
	private void buildStopWords(){
		STOP_WORDS = new CharArraySet(LUCENE_VERSION, 100, true);
		
		STOP_WORDS.addAll(StandardAnalyzer.STOP_WORDS_SET);
		
		STOP_WORDS.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);

        //for (int i = 0; i < 10; i++) {
        //	STOP_WORDS.add(String.valueOf(i));
        //}
        
		try {
			BufferedReader buffreader = new BufferedReader(
				    new InputStreamReader(
				        new FileInputStream(
				             new File(stopWordFile)
				        )
				    )
				);
			
			String line;
			while((line = buffreader.readLine()) != null){
				STOP_WORDS.add(line.trim());
			}
		} catch (IOException e) {
			LOG.error("Failed to read Sysnonym File: '{}'", synonymFile , e);
		}
	}

	/**
	 * Get StopWords
	 * 
	 * @return Stop Words
	 */
	public SynonymMap getSynonymMap() {
		if (synonyms == null){
			try {
				buildSynonymMap();
			} catch (IOException | ParseException e) {
				LOG.error("Failed to build synonymns", e);
			}
		}
		return synonyms;
	}

	/**
	 * Build Synonym Map.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	private void buildSynonymMap() throws IOException, ParseException{
		BufferedReader buffreader = new BufferedReader(
		    new InputStreamReader(
		        new FileInputStream(
		             new File(synonymFile)
		        )
		    )
		);

		SolrSynonymParser parser = new SolrSynonymParser(true, true, new SimpleAnalyzer(LUCENE_VERSION));
		parser.parse(buffreader);
		synonyms = parser.build();
	}
	
}
