/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.brianfeldman.lucene.Indexer;

/**
 * Simple CSV File Reader, iterate over CSV lines returned a Map. 
 * 
 * @author Brian G. Feldman (bgfeldm@yahoo.com)
 *
 */
public class SimpleCSVReader implements Iterator<Map<String, String>> {
	private static final Logger logger = LoggerFactory.getLogger(SimpleCSVReader.class);
	
    private static final String separator = "~";
    private File file;
    private BufferedReader reader;
    private String[] header;
    private Map<String, String> nextLine;

    /**
     * Constructor
     * 
     * @param file
     * @throws IOException
     */
    public SimpleCSVReader(File file) throws IOException {
         this.file=file;
         FileInputStream fstream = new FileInputStream(file);
         fstream.skip(8); // trim off magic header.

          this.reader = new BufferedReader(new InputStreamReader(fstream));
          this.header = reader.readLine().split(separator);
          this.nextLine = nextMap();
    }

    /**
     * Close File Reader.
     * 
     * @throws IOException
     */
    public void close() throws IOException{
    	if (reader != null){
        	reader.close();
    	}
    }

    /**
     * Get Map of Next CSV Record Line.
     * 
     * @return Map
     * @throws IOException
     */
    public Map<String, String> nextMap() throws IOException{
          String line = reader.readLine();
          if (line==null){ return null; }

          Scanner tokens = new Scanner(line).useDelimiter(separator);
          Map<String, String> retMap = new HashMap<String, String>();
          int c=0;
          while(tokens.hasNext()){
               retMap.put(header[c], tokens.next());
               c++;
          }

        return retMap;
    }

   @Override
   public boolean hasNext(){
          return (nextLine != null ? true : false );
   }

   @Override
   public Map<String, String> next(){
        Map<String, String> currentLine = nextLine;

        try {
			this.nextLine=nextMap();
		} catch (IOException e) {
			e.printStackTrace();
		}

        return currentLine;
   }

	@Override
	public void remove() {
		// un-used.
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		
		SimpleCSVReader reader = new SimpleCSVReader(new File(filename));
		while(reader.hasNext()){
			System.out.println(reader.next());
		}
		reader.close();
		
	}	
}

