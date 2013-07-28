package us.brianfeldman.lucene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import us.brianfeldman.fileformat.csv.SimpleCSVReader;

/**
 * Lucene Indexer, multi-threaded on Record.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 *
 */
public class Indexer {
	private static final Logger logger = LoggerFactory.getLogger(Indexer.class);
	
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
	public void openWriter() throws IOException{
		File indexPathFile = new File(indexPath);
		Directory directory = NIOFSDirectory.open(indexPathFile);
		System.out.println(indexPathFile.getAbsolutePath());
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		// Optional: for better indexing performance, increase the RAM buffer.  
		// But if you do this, increase the max heap size to the JVM (eg add -Xmx512m or -Xmx1g):
		// iwc.setRAMBufferSizeMB(256.0);
		
		writer = new IndexWriter(directory, iwc);
	}

	/**
	 * Clode Lucene Index Writer.
	 * @throws IOException
	 */
	public void closeWriter() throws IOException{
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
	public void indexDir(File file) throws IOException{
		
		// Create List of Files.
		File[] files = null;
		if (file.isDirectory()){
		    files=file.listFiles();
		} else if (file.isFile()) {
		     files = new File[1];
		     files[0]=file;
		}

		
		recordQueue = new ArrayBlockingQueue<Runnable>(recordQueueCapacity, true);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, recordQueue, new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.prestartAllCoreThreads();

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
		
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String indexDir = args[0];
		File indexDirFile = new File(indexDir);
		if (! indexDirFile.canRead()){
			throw new FileNotFoundException(indexDirFile.getAbsolutePath());
		}

		Indexer indexer = new Indexer();
		indexer.indexDir(indexDirFile);
		indexer.closeWriter();
	}	
}
