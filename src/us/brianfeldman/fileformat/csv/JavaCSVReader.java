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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.google.common.base.Stopwatch;
import com.googlecode.jcsv.reader.CSVReader;

/**
 * JavaCSVReader uses the csv parser from JavaCSV project. 
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @link http://javacsv.sourceforge.net/
 */
public class JavaCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(JavaCSVReader.class);

	private File file;
	private CsvReader csvReader;
	private int currentLineNumber = 0;
	private String[] header;
	private String[] currentLine;
	private char separator = ',';
	private char quote = '"';
	private char comment = '#';
	private boolean ignoreEmptyLines = true;
	private char recordDelimiter = '\n';

	/**
	 * @param separator		field separator character.  usually ',' in North America, ';' in Europe and sometimes '\t' for tab.
	 */
	public JavaCSVReader(final char separator){
		this(separator, '"');
	}

	/**
	 * @param separator		field separator character.  usually ',' in North America, ';' in Europe and sometimes '\t' for tab.
	 * @param quote			character use to enclose fields containing a separator. usually '"'
	 */
	public JavaCSVReader(final char separator, final char quote){
		this.separator = separator;
		this.quote = quote;
	}

	/**
	 * @param separator		field separator character.  usually ',' in North America, ';' in Europe and sometimes '\t' for tab.
	 * @param quote			character use to enclose fields containing a separator. usually '"'
	 * @param comment		leading character used on comment lines. Comment lines are ignored.
	 */
	public JavaCSVReader(final char separator, final char quote, final char comment){
		this.separator = separator;
		this.quote = quote;
		this.comment = comment;
	}
	
	@Override
	public boolean hasNext() {
		return (this.currentLine != null ? true : false );
	}

	@Override
	public  String[] next() {
		String[] currentRow = this.currentLine;
		currentLineNumber++;

		try {
			 if ( csvReader.readRecord() ){
				 this.currentLine = csvReader.getValues();
			 } else {
				 this.currentLine = null;
			 }
		} catch (IOException e) {
			LOG.error("Failed reading line, at {}:{}", getFileName(), getLineNumber(), e);
		}

		return currentRow;
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
	public void open(final File file) throws IOException {
		this.file = file;
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new BufferedReader(new InputStreamReader(inputStream), 32768);
		csvReader = new CsvReader(reader, this.separator);
		//csvReader.setSafetySwitch(false);
		csvReader.setTrimWhitespace(true);
		csvReader.setTextQualifier(this.quote);
		csvReader.setUseTextQualifier(true);
		csvReader.setSkipEmptyRecords(this.ignoreEmptyLines);
		csvReader.setComment(this.comment);
		//csvReader.setRecordDelimiter(this.recordDelimiter); // @BUG when set the newline character is not always stripped off last value.
		
		currentLineNumber++;
		if (csvReader.readRecord()){
			this.header = csvReader.getValues();
		}
		if (csvReader.readRecord()){
			this.currentLine = csvReader.getValues();
		}
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
	public static void main(final String[] args) throws IOException {
		String filename = args[0];

		Stopwatch stopwatch = Stopwatch.createStarted();

		JavaCSVReader reader = new JavaCSVReader(',');
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
