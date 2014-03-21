package us.brianfeldman.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;

/**
 * DocumentFlyweightPool
 */
public class DocumentFlyweightPool {

	private transient List<Document> pool;
	private final int poolSize;
	private transient int lastEl;

	/**
	 * DocumentFlyweightPool
	 * should be as large or larger then the amount of threads, using this pool.
	 * @param size
	 */
	public DocumentFlyweightPool(final int size){
		 this.poolSize=size;
		 this.pool = new ArrayList<Document>(size);
	}

	/**
	 * Create Blank Lucene Document.
	 * @param record
	 * @return
	 */
	private Document create(final Map<String, String> record){
		Document doc = new Document();
		for(String key: record.keySet()){
			StringField field = new StringField(key, "", Store.YES);
			doc.add(field);
		}
		return doc;
	}

	/**
	 * Populate Pool with flyweight Lucene documents to the pool size given.
	 * 
	 * @param record
	 */
	private void populate(final Map<String, String> record){
		for(int i=0; i < poolSize; i++){
			Document doc = create(record);
			pool.add(doc);
		}
	}

	/**
	 * Get Flyweight Lucene Document.
	 * 
	 * Through round robin fashion get a Flyweight Lucene Document.
	 * 
	 * @param record 
	 * @return Document
	 */
	public synchronized Document getDocument(final Map<String, String> record){
		if (pool.size() < poolSize){
			populate(record);
		}
				
		if (lastEl == poolSize-1){
			lastEl=0;
		} else {
			lastEl++;
		}
		
		return pool.get(lastEl);
	}

}
