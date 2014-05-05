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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.base.Stopwatch;

import us.brianfeldman.fileformat.csv.JCSVReader;
import us.brianfeldman.fileformat.csv.JavaCSVReader;
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

	private static final Configuration config = Configuration.getInstance();
	
	private static final int CPU_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private IndexWriter writer;
	private final RecordIterator csvReader;

	private final String startTime = String.valueOf(System.currentTimeMillis() / 1000l);

	//private BlockingQueue<Runnable> recordQueue;   // Record represents a line from a CSV file.
	private TransferQueue<Runnable> recordQueue;
	
	private int doneFileCount = 0;
	private int totalFileCount = 0;

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
		File indexPathFile = new File( config.getIndexPath() );
		LOG.info("Opening index writer at: "+indexPathFile.getAbsolutePath());

		IndexWriterConfig iwc = config.getIndexWriterConfig();

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
	 * Get Start Time.
	 * @return
	 */
	public String getStartTime() { return startTime; };

	/**
	 * Index Directory of CSV files.
	 * 
	 * @param directory as String
	 * @throws IOException 
	 */
	public void index(String directory) throws IOException{
		String[] fileNameSuffixes = config.getIndexFilenameSuffixes();

		File indexDirFile = new File( directory );

		Collection<File> files;
		if (indexDirFile.isDirectory()){
			files = FileUtils.listFiles(indexDirFile, fileNameSuffixes, true);
		} else {
			files = new ArrayList<File>();
			files.add(indexDirFile);
		}
		
		this.index( files );
	}
	
	
	/**
	 * Index Directory of CSV files.
	 * 
	 * @param files array of files to index
	 * @param cpuMultiplier 
	 * @throws IOException
	 */
	public void index(final Collection<File> files) throws IOException{
		LOG.info("Indexing files; file count: {}", files.size());
		
		final int maxThreads = (int) (CPU_PROCESSORS * config.getIndexerCpuMultiplier());
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


			while(csvReader.hasNext() ){
					String[] record = (String[]) csvReader.next();

					Map<String, String> metadata = new LinkedHashMap<String, String>();
					metadata.put("_index_time", startTime);
					metadata.put("_doc_id", csvReader.getFileName()+":"+String.valueOf(csvReader.getLineNumber()));

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

		/*
		 * Five build-in csv parsers, listed from fastest to slowest.
		 * @TODO find limitations for each csv parser implementation.
		 */
		JCSVReader csvReader = new JCSVReader(',', '"', '#');
		//SuperCSVReader csvReader = new SuperCSVReader(',', '"', '#'); // fastest on larger files; slow on small files.
		//JavaCSVReader csvReader = new JavaCSVReader(',', '"', '#');
		//OpenCSVReader csvReader = new OpenCSVReader(',', '"');
		//SimpleReader csvReader = new SimpleReader(',');  // non-complex csv, slowest but simplest to customize the single class.

		Indexer indexer = new Indexer(csvReader);

		Stopwatch stopwatch = Stopwatch.createStarted();
		
		indexer.index(indexDir);

		LOG.info("Finished adding records; closing index.");

		indexer.closeWriter();

		stopwatch.stop();

		LOG.info("Index finalizated; time: {}", stopwatch);
	}	
}
