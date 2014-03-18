package us.brianfeldman.lucene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.base.Stopwatch;

import us.brianfeldman.fileformat.csv.SimpleCSVReader;

/**
 * Lucene Indexer, multi-threaded on Record.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 *
 */
public class Indexer {
	private static final Logger logger = LoggerFactory.getLogger(Indexer.class);
	
	static {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("lib/log4j.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(props);
	}
	
	private String indexPath="build/luceneIndex";
	private IndexWriter writer;
	
	private BlockingQueue<Runnable> recordQueue;   // Record represents a line from a CSV file.

	private long startTime;
	private long endTime;
	
    private final int maxThreads = 2;
    private final int recordQueueCapacity = maxThreads * 2;

	public Indexer() throws IOException{
		
		openWriter();
	}

	/**
	 * Open Lucene Index Writer.
	 * @throws IOException
	 */
	public void openWriter() throws IOException {
		File indexPathFile = new File(indexPath);
		logger.info("Opening index writer at: "+indexPathFile.getAbsolutePath());
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		// Optional: for better indexing performance, increase the RAM buffer.  
		// But if you do this, increase the max heap size to the JVM (eg add -Xmx512m or -Xmx1g):
		// iwc.setRAMBufferSizeMB(256.0);
		
		try {
			Directory directory = NIOFSDirectory.open(indexPathFile);
			writer = new IndexWriter(directory, iwc);
		} catch (IOException e) {
			logger.error("Failed to open index writer", e);
		}
	}

	/**
	 * Close Lucene Index Writer.
	 * @throws IOException
	 */
	public void closeWriter() throws IOException{
		logger.info("Closing index writer");
		if (writer != null){
			writer.close();
		}
	}

	/**
	 * Index Directory of CSV files.
	 * 
	 * @param dirToIndex
	 * @throws IOException
	 */
	public void indexDir(File dirToIndex) throws IOException{		
		// Create List of Files.
		logger.info("Building list of files to index");
		File[] files = null;
		if (dirToIndex.isDirectory()){
		    files=dirToIndex.listFiles();
		} else if (dirToIndex.isFile()) {
		     files = new File[1];
		     files[0]=dirToIndex;
		}

		recordQueue = new ArrayBlockingQueue<Runnable>(recordQueueCapacity, true);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, recordQueue, new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.prestartAllCoreThreads();
		
	    Stopwatch stopwatch = Stopwatch.createStarted();

		if (files != null){
		     for (int i = 0; i < files.length; i++){
		    	DocumentFlyweightPool docFlyweightPool = new DocumentFlyweightPool(maxThreads);
		        SimpleCSVReader reader = new SimpleCSVReader(files[i]);
		        while(reader.hasNext() ){
		             while(reader.hasNext() && recordQueue.remainingCapacity() != 0){
		                  Map<String, String> record = reader.next();
		                  recordQueue.add(new RecordThread(record, docFlyweightPool, writer));
		             }

		             try{
		                Thread.sleep(20);
		             } catch (InterruptedException e){
		                      // ignore.
		             }

		        }
		     }
		}

		executor.shutdown();

		while(! executor.isTerminated()){
		     try{
		          Thread.sleep(20);
		     } catch (InterruptedException e){
		          // ignore.
		     }
		}

		stopwatch.stop();
		logger.info("Index time: "+stopwatch);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String indexDir = args[0];
		File indexDirFile = new File(indexDir);
		if (! indexDirFile.canRead()){
			logger.error("Can not read directory to index: "+indexDir);
		}

		Indexer indexer = new Indexer();		
		indexer.indexDir(indexDirFile);
		indexer.closeWriter();
	}	
}
