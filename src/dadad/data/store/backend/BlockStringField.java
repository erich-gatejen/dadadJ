package dadad.data.store.backend;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

public class BlockStringField extends Field {

	public static final FieldType TYPE_NOT_STORED = new FieldType();
	public static final FieldType TYPE_STORED = new FieldType();

	static {
		TYPE_NOT_STORED.setOmitNorms(true);
		TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		TYPE_NOT_STORED.setTokenized(false);
		TYPE_NOT_STORED.freeze();
		
		TYPE_STORED.setOmitNorms(true);
		TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setTokenized(false);
		TYPE_STORED.freeze();
	}

	public BlockStringField(String name, String value, Store stored) {
		super(name, value, stored == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
	}

	public BlockStringField(String name, BytesRef value, Store stored) {
	     super(name, value, stored == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
}
