/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/**
 * RecordThread
 * 
 * Record thread builds the Lucene Document from the record,
 * then adds the document to Lucene writer.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 */
public class RecordThread implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(RecordThread.class);

	private Map<String, String> record;
	private IndexWriter writer;
		
    private static final ThreadLocal<Document> tlocal = new ThreadLocal<Document>();
	private Document document;
		
	//private RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();

	/**
	 * Constructor 
	 * 
	 * @param record
	 * @param writer
	 */
	public RecordThread(Map<String, String> record, IndexWriter writer){
		this.record=record;
		this.writer=writer;
	}
	
	@Override
	public void run() {

        //record.put("_uuid", generateUUID());
		
		this.document = tlocal.get();
		
		if (document==null){
			document = new Document();
			LOG.debug("Initializing document.");
			for(String key: record.keySet()){
				document.add(new StringField(key, record.get(key).toLowerCase().trim(), Store.YES));
			}
			tlocal.set(document);
		} else {
			for(String key: record.keySet()){
				StringField field = (StringField) document.getField(key);
				field.setStringValue( record.get(key).toLowerCase().trim() );
			}
		}


		try {
			writer.addDocument(document);
			LOG.debug("completed: {}", record.get("_doc_id"));
		} catch (IOException e) {
			LOG.error("Failed writing document {}",  record.get("_doc_id"), e);
		}

	}
	
	/*
	private String generateUUID(){
		return uuidGenerator.generate().toString();
	}
	*/

}
