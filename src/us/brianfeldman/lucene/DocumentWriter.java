/**
 * 
 */
package us.brianfeldman.lucene;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DocumentWriter pulls from queue, populated by DocumentBuilder, 
 * and writes the Documents taken from the queue to the Lucene Index.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 *
 */
public class DocumentWriter implements Callable {

	private static final Logger logger = LoggerFactory.getLogger(DocumentWriter.class);
	
	public DocumentWriter(){
		
	}
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
