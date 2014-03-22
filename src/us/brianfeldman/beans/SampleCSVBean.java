/**
 * 
 */
package us.brianfeldman.beans;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import org.csveed.annotations.CsvCell;
import org.csveed.annotations.CsvDate;
import org.csveed.annotations.CsvFile;
import org.csveed.api.CsvReader;
import org.csveed.api.CsvReaderImpl;

/**
 * Annotations
 * 
 * @CsvFile(comment = '%', quote='\'', escape='\\', separator=',')
 * @CsvFile(useHeader = false, skipCommentLines = false, skipEmptyLines = false, startRow = 3)
 * 
 * @CsvCell(columnName = "name")
 * @CsvCell(columnIndex = 1, required = true)
 * @CsvDate(format = "dd-MM-yyyy")
 * @CsvIgnore
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @see http://csveed.org/annotations.html
 *
 */
@CsvFile(comment = '%', quote='\'', escape='\\', separator=',')
public class SampleCSVBean {

	@CsvCell(columnName = "name")
	private String name;

	@CsvCell(columnName = "numer")
	private int number;

	public void setName(String name){
		this.name=name;
	}

	public String getName(){ 
		return this.name;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    Reader reader = new StringReader(
                "name,number,date\n"+
                "\"Alpha\",1900,\"13-07-1922\"\n"+
                "\"Beta\",1901,\"22-01-1943\"\n"+
                "\"Gamma\",1902,\"30-09-1978\""
	    );
	    
	    //Reader reader = new FileReader(filename);
	    
	    CsvReader<SampleCSVBean> csvReader = new CsvReaderImpl<SampleCSVBean>(reader, SampleCSVBean.class);
	    
	    while(! csvReader.isFinished()){
		    SampleCSVBean bean = csvReader.readBean();
		    System.out.println( bean.getName() );
	    }

	}
	
}
