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

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
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
	
	private String indexTime = String.valueOf(System.currentTimeMillis() / 1000l);
	private TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

	private BlockingQueue<Runnable> recordQueue;   // Record represents a line from a CSV file.
	
    private final int CPUs = Runtime.getRuntime().availableProcessors();

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
		iwc.setRAMBufferSizeMB(256.0);

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
	 * Generate File list from Direcory.
	 * @param directory
	 * @return 
	 */
	public File[] generateFileList(File dir){
		logger.info("Building list of files");
		File[] files = null;
		if (dir.isDirectory()){
		    files=dir.listFiles();
		} else if (dir.isFile()) {
		     files = new File[1];
		     files[0]=dir;
		}
		return files;
	}
	
	/**
	 * Index Directory of CSV files.
	 * @param files array of files to index
	 * @throws IOException
	 */
	public void index(File[] files, int CPUmultiplier) throws IOException{
		int maxThreads = (CPUs * CPUmultiplier);

		recordQueue = new ArrayBlockingQueue<Runnable>(maxThreads*2, true);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, recordQueue, new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.prestartAllCoreThreads();
		if (files != null){

		     for (int i = 0; i < files.length; i++){
		    	DocumentFlyweightPool docFlyweightPool = new DocumentFlyweightPool(maxThreads);
		        SimpleCSVReader reader = new SimpleCSVReader(files[i]);
		        while(reader.hasNext() ){
		             while(reader.hasNext() && recordQueue.remainingCapacity() != 0){
		                  Map<String, String> record = reader.next();
		                  record.put("_uuid", uuidGenerator.generate().toString());
		                  record.put("_doc_id", reader.getFileName()+":"+reader.getLineNumber());
		                  record.put("_index_time", indexTime);
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
		          Thread.sleep(10);
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
			logger.error("Can not read directory to index: "+indexDir);
		}

		Indexer indexer = new Indexer();
		File[] files = indexer.generateFileList(indexDirFile);
		logger.info("Indexing files; file count: "+files.length);
		Stopwatch stopwatch = Stopwatch.createStarted();
		indexer.index(files, 2);
		logger.info("Finished adding records; closing index.");
		indexer.closeWriter();
		stopwatch.stop();
		logger.info("Index finalizated; time: "+stopwatch);
	}	
}
