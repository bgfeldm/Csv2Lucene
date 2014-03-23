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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class OpenCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(OpenCSVReader.class);

	private File file;
    private Map<String, String> nextLine;
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
	public Map<String, String> next(){
		Map<String, String> currentLine = nextLine;
		currentLineNumber++;

		try {
			this.nextLine=nextMap();
		} catch (IOException e) {
			LOG.error("Failed reading next line, at {}:{}", getFileName(), getLineNumber(), e);
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
		String[] line = csvReader.readNext();
		if (line==null){ return null; }

		Map<String, String> retMap = new HashMap<String, String>();
		for(int c=0; c < line.length; c++){
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
		this.csvReader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)), separator, '\'');
		header = csvReader.readNext();
		this.nextLine = nextMap();
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
		
		OpenCSVReader reader = new OpenCSVReader(',');
		reader.open(new File(filename));

		while( reader.hasNext() ){
			System.out.println(reader.next().toString());
		}
		reader.close();
	}

}
