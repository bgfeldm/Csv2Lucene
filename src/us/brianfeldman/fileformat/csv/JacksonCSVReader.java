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
import java.io.Reader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Stopwatch;

/**
 * JacksonReader
 * 
 * Handles more complex CSV which may contain quotes and comments.
 * A Map of strings is returned for each CSV row.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class JacksonCSVReader implements RecordIterator  {
	private static final Logger LOG = LoggerFactory.getLogger(JacksonCSVReader.class);

	private CsvSchema csvSchema;
	private MappingIterator<Map<String, String>> iterator;
	private File file;
    private int currentLineNumber = 0;
	
    public JacksonCSVReader(final String separator){
    	csvSchema = CsvSchema.emptySchema().withHeader().withLineSeparator("\n").withColumnSeparator(separator.charAt(0));
    }
 
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Object next() {
		currentLineNumber++;
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public String getFileName() {
		return file.getAbsolutePath();
	}

	@Override
	public int getLineNumber() {
		return this.currentLineNumber;
	}

	@Override
	public void open(File file) throws IOException {
		this.file = file;
		Reader reader = new BufferedReader(new FileReader( file ));
		iterator = new CsvMapper().reader(Map.class).with(csvSchema).readValues( reader );
	}

	@Override
	public void open(String textBlob) {
		try {
			iterator = new CsvMapper().reader(Map.class).with(csvSchema).readValues( textBlob );
		} catch (IOException e) {
			LOG.error("Failed opening file iterator", e);
		}
	}

	@Override
	public void close() throws IOException {
		iterator.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];

		Stopwatch stopwatch = Stopwatch.createStarted();
		
		JacksonCSVReader reader = new JacksonCSVReader(",");
		reader.open( new File(filename) );

		for(int c=1; reader.hasNext(); c++){
			System.out.println(c+" " + reader.next().toString());
		}
		reader.close();
		
		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}
}
