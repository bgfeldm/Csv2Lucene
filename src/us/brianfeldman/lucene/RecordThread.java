/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;

/**
 * 
 * 
 * @author Brian G. Feldman <bgfeldm@yahoo.com>
 */
public class RecordThread implements Runnable {

	private Map<String, String> record;
	private IndexWriter writer;
	private DocumentFlyweightPool docPool;
	
	/**
	 * Constructor 
	 * 
	 * @param record
	 * @param writer
	 */
	public RecordThread(Map<String, String> record, DocumentFlyweightPool docPool, IndexWriter writer){
		this.record=record;
		this.writer=writer;
		this.docPool=docPool;
	}
	

	@Override
	public void run() {

		
		/*
		 * Document doc = new Document();
		for(String key: record.keySet()){
			String value = record.get(key).toLowerCase().trim();
			StringField field = new StringField(key, value, Store.YES);
			doc.add(field);
		}
		*/
		
		Document doc = docPool.getDocument(record);
		for(String key: record.keySet()){
			StringField field = (StringField) doc.getField(key);
			field.setStringValue( record.get(key).toLowerCase().trim() );
		}
		
		
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
