/**
 * 
 */
package us.brianfeldman.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * @author Brian Feldman <bgfeldm@yahoo.com>
 *
 */
public class SearchResults {

	private final int total;
	private final int page;
	private final List<Document> docs = new ArrayList<Document>();
	private final String query;

	/**
	 * SeachResult
	 * 
	 * @param query		Original Search Query String
	 * @param total		Total result count
	 * @param page		Result page number when paging search results
	 */
	public SearchResults(String query, int total, int page){
		this.query = query;
		this.total = total;
		this.page = page;
	}

	/**
	 * Add document to Search Results.
	 * @param doc
	 */
	public void addDocument(Document doc){
		docs.add(doc);
	}

	/**
	 * Get document from Search Results at number index.
	 * 
	 * @param index
	 * @return	Document
	 */
	public Document getDocument(int index){
		return docs.get(index);
	}
	
	/**
	 * @return Total result count
	 */
	public int getTotalCount(){
		return this.total; 
	}

	/**
	 * @return	Result page number
	 */
	public int getPage(){
		return this.page;
	}
	
	
	/**
	 * @return Original Search Query String
	 */
	public String getQuery(){
		return this.query;
	}

	/**
	 * Size of search results included.
	 * 
	 * @return	size of search results
	 */
	public int size(){
		return docs.size();
	}
	
	/**
	 * Search
	 * 
	 * @param query			Search Query
	 * @param page			Pagination page number
	 * @param pageSize		Pagination page size
	 * @return Html string
	 * @throws ParseException
	 * @throws IOException
	 */
	public String toHtml(){
		StringBuilder html = new StringBuilder();
		
		for (int i=0; i< this.size();++i){
			Document doc = this.getDocument(i);

			html.append("<div>");
			List<IndexableField> fields = doc.getFields();
			for (IndexableField field: fields){
				html.append("<b><font color='blue'>"+field.name() + "</font></b> : "+ field.stringValue()+"<br/>");
			}
			html.append("</div><br/><hr/><br/>");
		}

		return html.toString();
	}

	/**
	 * Display Search Results.
	 * @param hits
	 */
	public void stdout(){
		for (int i=0; i < this.size();++i){
			System.out.println("---- Document "+(i+1));

			Document doc = this.getDocument(i);
			List<IndexableField> fields = doc.getFields();
			for (IndexableField field: fields){
				System.out.println(field.name() + ": "+ field.stringValue());
			}

		}
	}

}
