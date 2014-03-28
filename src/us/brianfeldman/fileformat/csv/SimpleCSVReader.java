/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Reader returns Map for each CSV row.
 *  
 * Note this reader does not handle more complex csv which use comments or quotes.
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 */
public class SimpleCSVReader implements RecordIterator {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleCSVReader.class);
	
    final private String separator;
    private File file;
    private BufferedReader reader;
    private String[] header;
    private String[] nextLine;
    private int currentLineNumber = 0;

    /**
     * Constructor
     * 
     * @param file
     * @param separator 
     * @throws IOException
     */
    public SimpleCSVReader(final char separator) {
         this.separator = Character.toString(separator);
    }

	@Override
	public void open(final File file) throws IOException {
		 this.file = file;
		 FileInputStream fstream = new FileInputStream(file);
         //fstream.skip(8); // trim off magic header.
		 
         this.reader = new BufferedReader(new InputStreamReader(fstream));
         this.header = reader.readLine().split(separator);
         currentLineNumber++; // counting header as a line.
         this.nextLine = readLine();
	}

	@Override
	public void open(final String textBlob) {
		this.file = null;
        InputStream is = new ByteArrayInputStream(textBlob.getBytes());
        this.reader = new BufferedReader(new InputStreamReader(is));
	}
	
	@Override
    public int getLineNumber(){
		return currentLineNumber;
    }

	@Override
    public String getFileName(){
		return file.getName();
    }
    
	@Override
    public void close() throws IOException{
    	if (reader != null){
        	reader.close();
    	}
    }

	private String[] readLine(){
	     String line = null;
	     try {
	    	 line = reader.readLine();
	     } catch (IOException e) {
	    	 LOG.error("Failed reading next line, at {}:{}", getFileName(), getLineNumber(), e);
	     }
	     
	     if (line==null){ return null; }
	     
	     Scanner tokens = new Scanner( line ).useDelimiter(separator);
	     List<String> retList = new ArrayList<String>();
	     for(int c=0; tokens.hasNext(); c++){
	    	 retList.add(tokens.next());
	     }
	     
	     return retList.toArray(new String[retList.size()]);
	}

   @Override
   public boolean hasNext(){
	   return (nextLine != null ? true : false );
   }
 
   @Override
   public String[] next(){
		String[] currentLine = this.nextLine;
		currentLineNumber++;
		this.nextLine = readLine();
		return currentLine;
   }

	@Override
	public String[] getHeader() {
		return this.header;
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
		
		SimpleCSVReader reader = new SimpleCSVReader(',');
		reader.open(new File(filename));
		
		for(int c=1; reader.hasNext(); c++){
			System.out.println(c+" " + Arrays.toString( reader.next() ));
		}
		reader.close();
		
		stopwatch.stop();
		System.out.println("time: "+stopwatch);
	}

}

