package us.brianfeldman.lucene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.base.Stopwatch;

import us.brianfeldman.fileformat.csv.JCSVReader;
import us.brianfeldman.fileformat.csv.OpenCSVReader;
import us.brianfeldman.fileformat.csv.SuperCSVReader;
import us.brianfeldman.fileformat.csv.RecordIterator;

/**
 * Lucene Indexer, multi-threaded on Record.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 *
 */
public class Indexer {
	private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

	private static final int CPU_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private final String indexPath="build/luceneIndex";

	private IndexWriter writer;

	private final RecordIterator csvReader;

	private final String indexTime = String.valueOf(System.currentTimeMillis() / 1000l);

	//private BlockingQueue<Runnable> recordQueue;   // Record represents a line from a CSV file.
	private TransferQueue<Runnable> recordQueue;
	
	private int doneFileCount = 0;
	private int totalFileCount = 0;

	static {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("lib/log4j.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(props);
	}


	public Indexer(RecordIterator csvReader){
		this.csvReader = csvReader;
		openWriter();
	}

	/**
	 * Open Lucene Index Writer.
	 * 
	 * @throws IOException
	 */
	public void openWriter() {
		File indexPathFile = new File(indexPath);
		LOG.info("Opening index writer at: "+indexPathFile.getAbsolutePath());

		final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

		// Optional: for better indexing performance, increase the RAM buffer.  
		// But if you do this, increase the max heap size to the JVM (eg add -Xmx512m or -Xmx1g):
		iwc.setRAMBufferSizeMB(128);   // lucene's default is 16 MB.
		iwc.setMaxThreadStates(16);  // default is 8.  Max threads communicating with the writer.
		iwc.setUseCompoundFile(false);
		iwc.setWriteLockTimeout(5000);

		try {
			Directory directory = NIOFSDirectory.open(indexPathFile);
			if ( IndexWriter.isLocked(directory) ){
				LOG.error("Index Directory is Locked");
				//IndexWriter.unlock(directory);
			}
			writer = new IndexWriter(directory, iwc);
		} catch (IOException e) {
			LOG.error("Failed to open index writer", e);
		}

	}


	/**
	 * Close Lucene Index Writer
	 * 
	 * Commits all changes to an index, waits for pending merges to complete, and closes all associated files.
	 * 
	 * @throws IOException
	 */
	public void closeWriter() throws IOException{
		LOG.info("Closing index writer");
		if (writer != null){
			try{
				writer.close();
			} catch(OutOfMemoryError e){
				LOG.error("Out of Memory while closing writer. Trying again...", e);
				writer.close();
			}
		}
	}


	/**
	 * Index Directory of CSV files.
	 * 
	 * @param files array of files to index
	 * @param cpuMultiplier 
	 * @throws IOException
	 */
	public void index(final Collection<File> files, final int cpuMultiplier) throws IOException{
		final int maxThreads = CPU_PROCESSORS * cpuMultiplier;
		LOG.debug("maxthreads: {}", maxThreads);

		recordQueue = new LinkedTransferQueue<Runnable>();
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, recordQueue, new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.prestartAllCoreThreads();

		Iterator<File> fileIterator = files.iterator();
		totalFileCount = files.size();
		
		for(doneFileCount=1; fileIterator.hasNext(); doneFileCount++){
			File file = fileIterator.next();
			LOG.info("Indexing file {} of {} : {}", doneFileCount, totalFileCount, file.getAbsolutePath());

			csvReader.open(file);

			String[] header = csvReader.getHeader();
			Map<String, String> metadata = new LinkedHashMap<String, String>();

			while(csvReader.hasNext() ){
					String[] record = (String[]) csvReader.next();


					metadata.put("_doc_id", csvReader.getFileName() + ":" + csvReader.getLineNumber());
					metadata.put("_index_time", indexTime);

					if (recordQueue.size() < maxThreads*3){
						recordQueue.add(new RecordConsumer(writer, header, record, metadata));
					} else {
						try {
							recordQueue.transfer(new RecordConsumer(writer, header, record, metadata));
						} catch (InterruptedException e) {
							LOG.error("LinkedTransferQueue Interrupted", e);
						}
					}
			}

			csvReader.close();
		}

		executor.shutdown();

		while(! executor.isTerminated()){
			try{
				Thread.sleep(10);
			} catch (InterruptedException e){
				LOG.error("ThreadPoolExecutor Interrupted", e);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String indexDir = args[0];
		String[] fileNameSuffixes = {"csv"};
		int cpuMultiplyer = 2;

		File indexDirFile = new File(indexDir);

		Collection<File> files;
		if (indexDirFile.isDirectory()){
			files = FileUtils.listFiles(indexDirFile, fileNameSuffixes, true);
		} else {
			files = new ArrayList<File>();
			files.add(indexDirFile);
		}

		LOG.info("Indexing files; file count: {}", files.size());

		/*
		 * Five build-in csv parsers, listed from fastest to slowest.
		 */
		JCSVReader csvReader = new JCSVReader(',');
		//OpenCSVReader csvReader = new OpenCSVReader(',');
		//SuperCSVReader csvReader = new SuperCSVReader(',');
		//SimpleReader csvReader = new SimpleReader(',');

		Indexer indexer = new Indexer(csvReader);

		Stopwatch stopwatch = Stopwatch.createStarted();

		indexer.index(files, cpuMultiplyer);

		LOG.info("Finished adding records; closing index.");

		indexer.closeWriter();

		stopwatch.stop();

		LOG.info("Index finalizated; time: {}", stopwatch);
	}	
}
