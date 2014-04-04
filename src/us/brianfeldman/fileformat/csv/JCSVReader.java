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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;

/**
 * OpenCSVReader uses the csv parser from jCSV project. 
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @link https://code.google.com/p/jcsv/
 *
 */
public class JCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(JCSVReader.class);

	private File file;
	private Map<String, String> nextLine;
	private CSVReader<String[]> csvReader;
	private int currentLineNumber = 0;
	private String[] header;
	private Iterator<String[]> csvIterator;
	private char separator = ',';
	private char quote = '"';
	private char comment = '#';
	private boolean ignoreEmptyLines = true;
	private CSVStrategy strategy;


	public JCSVReader(final char separator){
		this(separator, '"');
	}

	public JCSVReader(final char separator, final char quote){
		this.separator = separator;
		this.quote = quote;

		this.strategy = new CSVStrategy(this.separator, this.quote, this.comment, false, this.ignoreEmptyLines);
	}

	public JCSVReader(final char separator, final char quote, final char comment){
		this.separator = separator;
		this.quote = quote;
		this.comment = comment;
		
		this.strategy = new CSVStrategy(this.separator, this.quote, this.comment, false, this.ignoreEmptyLines);
	}
	
	@Override
	public boolean hasNext(){
		return csvIterator.hasNext();
	}

	@Override
	public String[] next(){
		currentLineNumber++;
		return csvIterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("CsvIterator does not support remove operation");
	}

	@Override
	public String[] getHeader() {
		return this.header;
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

		for(int c=1; c < 10; c++){
		//for(int c=1; reader.hasNext(); c++){
			System.out.println(c+" " + Arrays.toString( reader.next() ));
		}
		reader.close();

		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}

}
