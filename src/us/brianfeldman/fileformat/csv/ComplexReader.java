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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * ComplexCSVReader 
 * 
 * Handles more complex CSV which may contain quotes and comments.
 * A Map of strings is returned for each CSV row.
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 * @link http://supercsv.sourceforge.net/apidocs/
 */
public class ComplexReader implements ReadIterator {
	private static final Logger LOG = LoggerFactory.getLogger(ComplexReader.class);

	private File file;
    private Map<String, String> currentLine;
    private ICsvMapReader csvReader;
    private String[]      header;
    private CsvPreference csvPreference;

    public ComplexReader(int separator){
    	csvPreference = new CsvPreference.Builder('"', separator, "\n").build();
    }
    
    @Override
	public void open(File file) throws IOException{
		 this.file=file;
		 InputStream inputStream = new FileInputStream(file);
		 // inputStream.skip(8); // Skip over first couple byes of file.
		 this.csvReader = new CsvMapReader(new BufferedReader(new InputStreamReader(inputStream)), csvPreference);
		 this.header = csvReader.getHeader(false);
	}

	@Override
	public void open(String textBlob) {
		Reader reader = new StringReader(textBlob);
		this.csvReader = new CsvMapReader(reader, csvPreference);
		try {
			this.header = csvReader.getHeader(false);
		} catch (IOException e) {
        	LOG.error("Failed reading header line, {}", getFileName(), e);
		}
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

	@Override
    public boolean hasNext() {
        try {
        	if (this.currentLine == null){
        		this.currentLine = csvReader.read(header);
        	}
        } catch (IOException e) {
        	LOG.error("Failed reading next line, at {}:{}", getFileName(), getLineNumber(), e);
        }
        return currentLine != null;
    }

	@Override
	public Map<String, String> next() {
		Map<String,String> nextLine = this.currentLine;
		this.currentLine = null;
		return nextLine;
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
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		
		ComplexReader reader = new ComplexReader(',');
		reader.open(new File(filename));
		
		while( reader.hasNext() ){
			System.out.println(reader.next().toString());
		}
		reader.close();
		
	}


}
