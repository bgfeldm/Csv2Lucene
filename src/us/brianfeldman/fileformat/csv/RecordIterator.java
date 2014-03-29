/**
 * 
 */
package us.brianfeldman.fileformat.csv;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Common Interface for use across all CSV readers.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 *
 */
public interface RecordIterator extends Iterator<Object> {

	/**
	 * Get File Name
	 * 
	 * @return file name
	 */
	public String getFileName();

	/**
	 * Get current line number.
	 * 
	 * @return line number
	 */
	public int getLineNumber();

	/**
	 * Get header
	 * 
	 * @return line number
	 */
	public String[] getHeader();

	/**
	 * Open reading on a File.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void open(File file) throws IOException;

	/**
	 * Open reading on a text blob.
	 * 
	 * @param textBlob
	 */
	public void open(String textBlob);

	/**
	 * Close reading of File or Text blob.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
}
