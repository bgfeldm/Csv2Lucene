/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.google.common.base.Stopwatch;
import com.googlecode.jcsv.reader.CSVReader;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class JavaCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(JavaCSVReader.class);

	private File file;
	private Map<String, String> nextLine;
	private CsvReader csvReader;
	private int currentLineNumber = 0;
	private String[] header;
	private char separator = ',';
	private char quote = '"';
	private char comment = '#';
	private boolean ignoreEmptyLines = true;
	private char recordDelimiter = '\n';

	public JavaCSVReader(final char separator){
		this(separator, '"');
	}

	public JavaCSVReader(final char separator, final char quote){
		this.separator = separator;
		this.quote = quote;
	}

	@Override
	public boolean hasNext() {
		boolean bool = false;
		try {
			 bool = csvReader.readRecord();
		} catch (IOException e) {
			LOG.error("Failed to find next record.", e);
		}
		return bool;
	}

	@Override
	public  String[] next() {
		currentLineNumber++;
		String[] row = null;
		try {
			 row = csvReader.getValues();
		} catch (IOException e) {
			LOG.error("Failed reading line, at {}:{}", getFileName(), getLineNumber(), e);
		}
		return row;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("CsvIterator does not support remove operation");
	}

	@Override
	public String getFileName() {
		return this.file.getAbsolutePath();
	}

	@Override
	public int getLineNumber() {
		return this.currentLineNumber;
	}

	@Override
	public String[] getHeader() {
		return this.header;
	}

	@Override
	public void open(File file) throws IOException {
		this.file = file;
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new BufferedReader(new InputStreamReader(inputStream));
		csvReader = new CsvReader(reader, this.separator);
		csvReader.setTrimWhitespace(true);
		csvReader.setTextQualifier(this.quote);
		csvReader.setSkipEmptyRecords(this.ignoreEmptyLines);
		csvReader.setComment(this.comment);
		csvReader.setRecordDelimiter(this.recordDelimiter);
		this.header = csvReader.getHeaders();
	}

	@Override
	public void open(String textBlob) {
		try {
			csvReader = new CsvReader(textBlob);
		} catch (FileNotFoundException e) {
			LOG.error("Failed to read record.", e);
		}
	}

	@Override
	public void close() throws IOException {
		csvReader.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];

		Stopwatch stopwatch = Stopwatch.createStarted();

		JavaCSVReader reader = new JavaCSVReader(',');
		reader.open(new File(filename));

		for(int c=1; reader.hasNext(); c++){
			System.out.println(c+" " + Arrays.toString( reader.next() ));
		}
		reader.close();

		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}	
	
}
