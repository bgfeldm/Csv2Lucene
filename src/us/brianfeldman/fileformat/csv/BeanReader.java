/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.csveed.api.CsvReader;
import org.csveed.api.CsvReaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.brianfeldman.beans.SampleCSVBean;

/**
 * Reader which returns Java Bean for each CSV row.
 * 
 * @author Brian Feldman <bgfeldm@yahoo.com>
 * 
 * @link http://javadocs.csveed.org/
 *
 */
public class BeanReader implements ReadIterator {
	private static final Logger LOG = LoggerFactory.getLogger(BeanReader.class);

	private File file;
	private int lineNumber;
	private CsvReader<?> csvReader;
	private Class<?> beanClass;

	public BeanReader(Class beanClass){
		this.beanClass = beanClass;
	}

	@Override
	public void open(File file) throws IOException {
		this.file = file;
		Reader reader = new FileReader(file);
		csvReader = new CsvReaderImpl(reader, beanClass);
	}

	@Override
	public void open(String textBlob){
		this.file = null;
		Reader reader = new StringReader(textBlob);
		csvReader = new CsvReaderImpl(reader, beanClass);
	}

	@Override
	public void close() throws IOException {
		if (csvReader != null){
			csvReader=null;
		}
	}

	@Override
	public boolean hasNext() {
		return csvReader.isFinished();
	}

	@Override
	public Object next() {
		lineNumber++;
		return csvReader.readBean();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getFileName() {
		return this.file.getAbsolutePath();
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
	    String textBlob =
                "name,number,date\n"+
                "\"Alpha\",1900,\"13-07-1922\"\n"+
                "\"Beta\",1901,\"22-01-1943\"\n"+
                "\"Gamma\",1902,\"30-09-1978\""
	    ;
	    
	    BeanReader reader = new BeanReader(SampleCSVBean.class);
	    reader.open(textBlob);
	    	    
	    while(! reader.hasNext()){
		    SampleCSVBean bean = (SampleCSVBean) reader.next();
		    System.out.println( bean.getName() );
	    }
	}

}
