package dadad.data.store;

import dadad.data.DataContext;
import dadad.data.model.DataId;
import dadad.data.model.Document;
import dadad.data.model.SourceInjector;
import dadad.data.store.backend.KVStore;
import dadad.platform.AnnotatedException;

/**
 * Document catalog store.
 */
public class DocumentCatalogStore implements SourceInjector {
	
	// ===============================================================================
	// = FIELDS
	
	public final static String RESERVED_KEY__DOCUMENT_ID_KEY = "RSV.document.";
	public final static String RESERVED_KEY__NEXT_DOCUMENT_ID = "RSV.document.nextSourceId";

	private KVStore kvStore;	
	
	// ===============================================================================
	// = METHODS
	
	public DocumentCatalogStore(DataContext context) {
		kvStore = context.getKVStore();
		kvStore.openForReadWrite();
	}
	
	public Document create(String uri) {
		
		// Get the next id
		
		/***
		 * 
		 * WARNING WARNING WARNING!!!!  THIS IS NOT THREAD OR PROCESS SAFE!!!!
		 * 
		 * I'll need to fix this for sure.
		 * 
		 */
		Long id = (Long) kvStore.get(RESERVED_KEY__NEXT_DOCUMENT_ID);
		if (id == null) {
			id = DataId.STARTING_DOC_ID;
		}
		id++;
		kvStore.put(RESERVED_KEY__NEXT_DOCUMENT_ID, id);
		
		Document document = new Document(uri, id);

		// Double store it for now.  Optimize if necessary.
		kvStore.put(mangleIdKey(id), document);
		kvStore.put(mangleUriKey(uri), document);
		
		return document;
		
	}
	
	public Document get(String uri) {
		
		try {
			return (Document) kvStore.get(mangleUriKey(uri));
 		
		} catch (ClassCastException e) {
			throw new AnnotatedException(
					"URI found but it wasn't a Soure object.", AnnotatedException.Catagory.ERROR, e)
			.annotate("uri", uri);			
		} catch (Exception e) {
			throw new AnnotatedException("Soure not found.", AnnotatedException.Catagory.ERROR, e)
					.annotate("uri", uri);
		}
		
	}
	
	public void put(Document document) {
		kvStore.put(mangleUriKey(document.uri), document);
	}
	
	private String mangleIdKey(long id) {
		return RESERVED_KEY__DOCUMENT_ID_KEY + id;
	}
	
	private String mangleUriKey(String uri) {
		return RESERVED_KEY__DOCUMENT_ID_KEY + uri;
	}
	
	public synchronized void close() {
		if (kvStore != null) {
			kvStore.closeForReadWrite();
			kvStore = null;
		}
	}
	
	protected void finalize() throws Throwable {
	     try {
	         close();
	     } finally {
	         super.finalize();
	     }
	 }

}