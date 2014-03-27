/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.CSVTokenizer;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.CSVTokenizerImpl;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;

/**
 * 
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class JCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(JCSVReader.class);

	private File file;
    private Map<String, String> nextLine;
    private char separator;
    private CSVReader<String[]> csvReader;
    private int currentLineNumber = 0;
    private String[] header;
    private Iterator<String[]> csvIterator;
    
    public JCSVReader(final char separator){
    	this.separator = separator;
    }

    @Override
    public boolean hasNext(){
    	return csvIterator.hasNext();
    }

	@Override
	public Map<String, String> next(){
		Map<String, String> currentLine = null;;
		currentLineNumber++;
		try {
			currentLine = nextMap();
		} catch (IOException e) {
			LOG.error("Failed reading line {}:{}", this.file.getAbsolutePath(), currentLineNumber, e);
		}
		return currentLine;
	}

	/**
	 * Get Map of Next CSV Record Line.
	 * 
	 * @return Map
	 * @throws IOException
	 */
	private Map<String, String> nextMap() throws IOException{
		String[] line = csvIterator.next();
		if (line==null){ return null; }

		Map<String, String> retMap = new HashMap<String, String>();
		for(int c=0; c < header.length; c++){
			retMap.put(header[c], line[c]);
		}

		return retMap;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("CsvIterator does not support remove operation");
	}

	@Override
	public String getFileName() {
		return file.getAbsolutePath();
	}

	@Override
	public int getLineNumber() {
		return currentLineNumber;
	}

	@Override
	public void open(File file) throws IOException {
		this.file = file;
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new BufferedReader(new InputStreamReader(inputStream));
		CSVStrategy strategy = new CSVStrategy(separator, '"', '#', false, true);
		csvReader = new CSVReaderBuilder<String[]>(reader).entryParser(new DefaultCSVEntryParser()).strategy(strategy).build();
		csvIterator = csvReader.iterator();
		header = csvIterator.next();
	}

	@Override
	public void open(String textBlob) {
		this.csvReader = CSVReaderBuilder.newDefaultReader(new StringReader(textBlob));
		csvIterator = csvReader.iterator();
		header = csvIterator.next();
	}

	@Override
	public void close() throws IOException {
		if (csvReader != null){
			csvReader.close();
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		JCSVReader reader = new JCSVReader(',');
		reader.open(new File(filename));

		for(int c=1; reader.hasNext(); c++){
			reader.next();
			//System.out.println(c+" " + reader.next().toString());
		}
		reader.close();

		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}

}
