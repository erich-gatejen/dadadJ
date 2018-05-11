package dadad.data;

import java.io.File;

import dadad.data.model.Document;
import dadad.data.store.DocumentCatalogStore;
import dadad.data.store.backend.KVStore;
import dadad.data.store.backend.TokenIndex;
import dadad.platform.Context;
import dadad.platform.ContextBasic;
import dadad.platform.PropertyView;

/**
 * A data context.
 * 
 */
public class DataContext extends ContextBasic {
	
	// ===============================================================================
	// = FIELDS
	
	public final static String SUBDIR__TOKEN_INDEX = "tokens";
	public final static String FILE__KV_STORE = "kv.db";
	
	public final static String LOGGER__SYSTEM = "dadad.data.system";
	public final static String LOGGER__REPORTING = "dadad.data.reporting";
	
	private static volatile TokenIndex tokenIndex;
	private static volatile KVStore kvStore;
	private DocumentCatalogStore docCatalogStore;
	
	public Document currentDoc;

	
	// ===============================================================================
	// = METHODS

	public DataContext(PropertyView properties) {
	    super(properties);
	}
	
	public DataContext(Context context) {
	    super(context);
	}
	
	public synchronized TokenIndex getTokenIndex() {
		if (tokenIndex == null) {
			tokenIndex = new TokenIndex(this);
		}
		return tokenIndex;
	}
	
	public synchronized KVStore getKVStore() {
		if (kvStore == null) {
			kvStore = new KVStore(this);
		}
		return kvStore;
	}
	
	public synchronized DocumentCatalogStore getDocCatalogStore() {
		if (docCatalogStore == null) {
			docCatalogStore = new DocumentCatalogStore(this);
		}
		return docCatalogStore;
	}
	
	@Override
	public Context copyContext() {
		return new DataContext(copyProperties());
	}
	
	@Override
	public Context subContext() {
		return new DataContext(this);
	}	
	
	
	// ===============================================================================
	// = BUILDERS
	
	
	
	// ===============================================================================
	// = CONVENIENCE
	
	public String getTokenIndexSubdir() {
		return new File(getDataDirFile(), SUBDIR__TOKEN_INDEX).getAbsolutePath();
	}
	
	public String getKVStoreFile() {
		return new File(getDataDirFile(), FILE__KV_STORE).getAbsolutePath();
	}

}

