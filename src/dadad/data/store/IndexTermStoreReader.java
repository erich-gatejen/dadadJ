package dadad.data.store;

import java.util.logging.Logger;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;

import dadad.data.DataContext;

/**
 * Term reader from index.
 */
public class IndexTermStoreReader extends TermIndex {
	
	// ===============================================================================
	// = FIELDS
	
	private Logger sysLogger = Logger.getLogger(DataContext.LOGGER__SYSTEM);
	private IndexSearcher indexSearcher;
	
	
	// ===============================================================================
	// = METHODS
	
	public IndexTermStoreReader(DataContext context) {
		super(context);
	}
	
	public TermsEnum getTerms(final String field) {
		if(indexSearcher == null) throw new Error("You must open the reader before using it.");
		TermsEnum result = null;
		sysLogger.fine("Getting all terms for a field.  field=[" + field + "]");
		
		try {
			Fields fields = MultiFields.getFields(indexSearcher.getIndexReader());
			Terms terms = fields.terms(field);
			if (terms != null) {
				result = terms.iterator();
			}	
			
		} catch (Exception e) {
			throwException("Failed getting terms.", e);
		}
		
		return result;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	public synchronized void open() {
		if (indexSearcher == null) {
			indexSearcher = getTokenStore().getSearcher();
		}
	}
	
	public synchronized void close() {
		if (indexSearcher != null) {
			try {
				indexSearcher.getIndexReader().close();
				indexSearcher = null;
			} catch (Exception e) {
				throwException("Could not close index reader.", e);
			}
		}
	}
	
}