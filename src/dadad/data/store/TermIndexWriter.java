package dadad.data.store;

import dadad.data.model.DataId;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LegacyDoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.LegacyLongField;
import org.apache.lucene.document.TextField;

import dadad.data.DataContext;
import dadad.data.model.BlockInfo;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.data.store.backend.BlockStringField;

/**
 * Term index writer based on the text.
 */
public class TermIndexWriter extends TermIndex implements TermWriter {
	
	// ===============================================================================
	// = FIELDS
	
	// ===============================================================================
	// = INTERFACE
	
	public void submit(BlockInfo blockInfo, String tag, Term[] terms) {		
		Document doc = new Document();
		StringBuilder tagText = null;
		
		if (tag != null) tagText = new StringBuilder();
		
		for (Term term : terms) {
			
			if (term.element == null) {
				if (tag != null) {
					// Need to make this configurable.  I'm just assuming whitespace is breaking for tokanization,
					// but that won't always be true.
					tagText.append(" ").append(term.text);
				}
				add(doc, term.text, ElementType.TEXT, TAG__TEXT);
				
			} else {
				
				if (tag != null) {
					tagText.append(" ").append(term.element.text);
				}
				if (term.element.tag == null) {
					add(doc, term.element.text, ElementType.TEXT, TAG__TEXT);
				} else {
					add(doc, term.element.text, term.element.type, term.element.tag);			
				}				
				
			}
				
		}
		
		if (blockInfo != null) {
			if (blockInfo.type() != null) doc.add(new StringField(TAG__BLOCK_TYPE, blockInfo.type().name(), Store.NO));
			if ((indexOwnerId) && (! DataId.isNoId(blockInfo.ownerId()))) doc.add(new LegacyLongField(TAG__BLOCK_OWNER_ID,
					blockInfo.ownerId(), Store.NO));
			if (! DataId.isNoId(blockInfo.blockId()))  doc.add(new LegacyLongField(TAG__BLOCK_ID, blockInfo.blockId(), Store.NO));
		}	
			
		if (tag != null) {
			doc.add(new TextField(tag, tagText.toString(), Store.NO));
		}		
		
		getTokenStore().submit(doc);
	}
	
	// ===============================================================================
	// = METHODS
	
	public TermIndexWriter(DataContext context) {
		super(context);
	}

	private void add(Document doc, String text, ElementType elementType, String tag) {

		switch(elementType) {
		
		case STRING:
			if (text.length() > 0) {			
				if (normalizeToLowerCase)
					doc.add(new BlockStringField(tag, text.toLowerCase(), Store.NO));
				else 
					doc.add(new BlockStringField(tag, text, Store.NO));
			}
			break;
		
		case TEXT:
			// Indexer is responsible for normalization
			doc.add(new TextField(tag, text, Store.NO));
			break;
			
		case LONG:
			long value = 0;
			try {
				value = Long.parseLong(text.trim());
			} catch (NumberFormatException nfe) {
				throwException("Element text not a valid Long.", nfe, ElementType.LONG, text);
			}
			doc.add(new LegacyLongField(tag, value, Store.NO));
			break;
			
		case DOUBLE:
			double dvalue = 0;
			try {
				dvalue = Long.parseLong(text.trim());
			} catch (NumberFormatException nfe) {
				throwException("Element text not a valid Double.", nfe, ElementType.DOUBLE, text);
			}
			doc.add(new LegacyDoubleField(tag, dvalue, Store.NO));
			break;
			
		case TIMESTAMP:
		case BOOLEAN:
		case BREAKING:		
		default:
			throw new Error("UNHANDLED element type.  type=" + elementType.name());
		}
		
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	public synchronized void open() {
		getTokenStore().openForWrite();
	}
	
	public synchronized void close() {
		getTokenStore().closeForWrite();
	}
	
}