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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.comment.CommentMatcher;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.base.Stopwatch;
import com.googlecode.jcsv.annotations.processors.StringProcessor;

/**
 * SuperCSVReader uses the csv parser from SuperCSV project. 
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 * @link http://supercsv.sourceforge.net/apidocs/
 * 
 */
public class SuperCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(SuperCSVReader.class);

	private File file;
	private String[] currentLine;
	private ICsvListReader csvReader;
	private String[]      header;
	private CsvPreference csvPreference;
	private char separator = ',';
	private char quote = '"';
	private String comment = "#";
	private String recordDelimiter = "\n";

	public SuperCSVReader(final char separator){
		this(separator, '"');
	}

	public SuperCSVReader(final char separator, final char quote){
		this.separator = separator;
		this.quote = quote;
		// surroundingSpacesNeedQuotes when true will trim spaces unless within quotes.
		csvPreference = new CsvPreference.Builder(this.quote, this.separator, this.recordDelimiter).surroundingSpacesNeedQuotes(true).build();
	}
	
	public SuperCSVReader(final char separator, final char quote, final char comment){
		this.separator = separator;
		this.quote = quote;
		this.comment = String.valueOf(comment);
		// surroundingSpacesNeedQuotes when true will trim spaces unless within quotes.
		csvPreference = new CsvPreference.Builder(this.quote, this.separator, this.recordDelimiter).skipComments(new CommentStartsWith(this.comment)).surroundingSpacesNeedQuotes(true).build();
	}

	@Override
	public void open(final File file) throws IOException{
		this.file=file;
		InputStream inputStream = new FileInputStream(file);
		// inputStream.skip(8); // Skip over first couple byes of file.
		this.csvReader = new CsvListReader(new BufferedReader(new InputStreamReader(inputStream)), csvPreference);
		try {
			this.header = csvReader.getHeader(false);
			List<String> nextLine = csvReader.read();
			this.currentLine = convertNullsToString(nextLine.toArray(new String[nextLine.size()]));
		} catch (IOException e) {
			LOG.error("Failed reading line, at {}:{}", getFileName(), getLineNumber(), e);
			throw(e);
		}
	}

	@Override
	public void open(final String textBlob) {
		Reader reader = new StringReader(textBlob);
		this.csvReader = new CsvListReader(reader, csvPreference);
		try {
			this.header = csvReader.getHeader(false);
			List<String> nextLine = csvReader.read();
			this.currentLine = convertNullsToString(nextLine.toArray(new String[nextLine.size()]));
		} catch (IOException e) {
			LOG.error("Failed reading line, at {}:{}", getFileName(), getLineNumber(), e);
		}
	}

	@Override
	public String[] getHeader() {
		return this.header;
	}

	@Override
	public int getLineNumber(){
		return csvReader.getLineNumber();
	}

	@Override
	public String getFileName(){
		return this.file.getAbsolutePath();
	}

	@Override
	public void close() throws IOException {
		if (csvReader!=null){
			csvReader.close();
		}
	}

	private String[] convertNullsToString(String[] in){
		for(int i=0; i < in.length; i++){
			if (in[i] == null){
				in[i]="";
			}
		}
		return in;
	}
	
	@Override
	public boolean hasNext() {
		return (this.currentLine != null ? true : false );
	}

	@Override
	public String[] next() {
		String[] currentRow = this.currentLine;

		try {
			List<String> nextLine = csvReader.read();
			if (nextLine != null){
				this.currentLine = convertNullsToString(nextLine.toArray(new String[nextLine.size()]));
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

	/**
	 * Main method used for testing.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(final String[] args) throws IOException {
		String filename = args[0];

		Stopwatch stopwatch = Stopwatch.createStarted();

		SuperCSVReader reader = new SuperCSVReader(',', '"');
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
