/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class OpenCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(OpenCSVReader.class);

	private File file;
    private String[] nextLine;
    private char separator;
    private CSVReader csvReader;
    private int currentLineNumber = 0;
    private String[] header;

    public OpenCSVReader(final char separator){
    	this.separator = separator;
    }

    @Override
    public boolean hasNext(){
           return (nextLine != null ? true : false );
    }

	@Override
	public String[] next(){
		String[] currentLine = this.nextLine;
		currentLineNumber++;
		String[] ret=null;
		try {
			nextLine = csvReader.readNext();
		} catch (IOException e) {
			LOG.error("Failed reading next line, at {}:{}", getFileName(), getLineNumber(), e);
		}
		return currentLine;
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
		this.csvReader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)), separator, '\'');
		header = csvReader.readNext();
		this.nextLine = csvReader.readNext();
	}

	@Override
	public void open(String textBlob) {
		this.csvReader = new CSVReader(new StringReader(textBlob));
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
		
		OpenCSVReader reader = new OpenCSVReader(',');
		reader.open(new File(filename));
		
		for(int c=1; reader.hasNext(); c++){
			System.out.println(c+" " + Arrays.toString( reader.next() ));
		}
		reader.close();
		
		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}

}
