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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader returns Map for each CSV row.
 *  
 * Note this reader does not handle more complex csv which use comments or quotes.
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 */
public class SimpleReader implements ReadIterator {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleReader.class);
	
    private String separator;
    private File file;
    private BufferedReader reader;
    private String[] header;
    private Map<String, String> nextLine;
    private int currentLineNumber = 0;

    /**
     * Constructor
     * 
     * @param file
     * @param separator 
     * @throws IOException
     */
    public SimpleReader(String separator) {
         this.separator = separator;
    }

	@Override
	public void open(File file) throws IOException {
		 this.file = file;
		 FileInputStream fstream = new FileInputStream(file);
         //fstream.skip(8); // trim off magic header.
		 
         this.reader = new BufferedReader(new InputStreamReader(fstream));
         this.header = reader.readLine().split(separator);
         currentLineNumber++; // counting header as a line.
         this.nextLine = nextMap();
	}

	@Override
	public void open(String textBlob) {
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
         String line = reader.readLine();
         if (line==null){ return null; }

         Scanner tokens = new Scanner(line).useDelimiter(separator);
         Map<String, String> retMap = new HashMap<String, String>();
         for(int c=0; tokens.hasNext(); c++){
        	 retMap.put(header[c], tokens.next());
         }

       return retMap;
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
		
		SimpleReader reader = new SimpleReader(",");
		reader.open( new File(filename) );
		
		while(reader.hasNext()){
			System.out.println(reader.next());
		}
		reader.close();
		
	}

}

