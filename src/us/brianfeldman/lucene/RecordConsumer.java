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
 * RecordConsumer
 * 
 * Record thread builds the Lucene Document from the record,
 * then adds the document to Lucene writer.
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 */
public class RecordConsumer implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(RecordConsumer.class);

	private IndexWriter writer;
	private String[] header;
	private String[] record;
	private Map<String, String> metadata;

	private static final ThreadLocal<Document> tlocal = new ThreadLocal<Document>();
	private Document document;

	//private RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();

	/**
	 * Constructor
	 * 
	 * @param writer
	 * @param header 
	 * @param record
	 * @param metdata
	 */
	public RecordConsumer(IndexWriter writer, String[] header, String[] record, Map<String, String> metadata){
		this.header=header;
		this.record=record;
		this.writer=writer;
		this.metadata=metadata;
	}

	/**
	 * Build Lucene Document.
	 * 
	 * @return Document
	 */
	public Document buildLuceneDocument(){
		this.document = tlocal.get();

		if (document==null){
			document = new Document();
			LOG.debug("Initializing document.");
			for(int i=0; i < header.length; i++){
				document.add(new StringField(header[i], record[i].toLowerCase().trim(), Store.YES));
			}
			
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				document.add(new StringField(entry.getKey(), entry.getValue(), Store.YES));
			}
			
			tlocal.set(document);
		} else {
			for(int i=0; i < header.length; i++){
				StringField field = (StringField) document.getField( header[i] );
				field.setStringValue( record[i].toLowerCase().trim() );
			}
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				StringField field = (StringField) document.getField( entry.getKey() );
				field.setStringValue( entry.getValue() );				
			}
		}

		return document;
	}


	/**
	 * Add Document to Index.
	 */
	public void addToIndex(){
		try {
			writer.addDocument(document);
			//LOG.debug("completed: {}", record.get("_doc_id"));
		} catch (IOException e) {
			//LOG.error("Failed writing document {}",  record.get("_doc_id"), e);
		} catch(OutOfMemoryError e){
			LOG.error("Due to Out of Memory Error, Closing Index Writer.", e);  
			try {
				writer.close();   // This will effect all threads.
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		//record.put("_uuid", generateUUID());

		buildLuceneDocument();

		addToIndex();			
	}

	/*
	private String generateUUID(){
		return uuidGenerator.generate().toString();
	}
	 */

}