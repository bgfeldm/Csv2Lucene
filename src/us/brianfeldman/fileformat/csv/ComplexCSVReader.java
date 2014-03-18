/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * ComplexCSVReader
 * 
 * This reader uses SuperCsv which is helpful for more complex CSV files.
 * 
 * 
 * @link http://supercsv.sourceforge.net/preferences.html
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 */
public class ComplexCSVReader implements Iterator {
	private static final Logger logger = LoggerFactory.getLogger(ComplexCSVReader.class);

	//private static final CsvPreference PIPE_DELIMITED = new CsvPreference.Builder('"', '|', "\n").build();
	//private static final CsvPreference TILDA_DELIMITED = new CsvPreference.Builder('"', '~', "\n").build();
	//private static final CsvPreference TAB_DELIMITED = CsvPreference.TAB_PREFERENCE;
	private static final CsvPreference COMMA_DELIMITED = CsvPreference.STANDARD_PREFERENCE;
	
	private File file;
    private Map<String, String> currentLine;
    private ICsvMapReader reader;
    private String[]      header;
	
	/**
	 * CSVFileReader
	 * 
	 * @param file
	 * @throws IOException
	 */
	public ComplexCSVReader(File file) throws IOException{
		 this.file=file;
		 InputStream inputStream = new FileInputStream(file);
		 // inputStream.skip(8); // Skip over first couple byes of file.
		 this.reader = new CsvMapReader(new InputStreamReader(inputStream), COMMA_DELIMITED);
		 this.header = reader.getHeader(false);
	}
	
	/**
	 * Get Line Number
	 * @return current line number
	 */
	public int getLineNumber(){
		return reader.getLineNumber();
	}
	
	/**
	 * Close CSVFileReader
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (reader!=null){
			reader.close();
		}
	}

	@Override
    public boolean hasNext() {
        try {
            currentLine = reader.read(header);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return currentLine != null;
    }

	@Override
	public Object next() {
		return currentLine;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("CsvIterator does not support remove operation");
	}
	
	/**
	 * Main method used for testing of CSV file or function.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		
		ComplexCSVReader reader = new ComplexCSVReader(new File(filename));
		
		while( reader.hasNext() ){
			System.out.println(reader.next().toString());
		}
		reader.close();
		
	}
}
