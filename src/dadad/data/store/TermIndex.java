package dadad.data.store;

import dadad.data.DataContext;
import dadad.data.config.StoreIndexConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.data.store.backend.TokenIndex;
import dadad.platform.AnnotatedException;

/**
 * Term index based on the text.
 */
public abstract class TermIndex {
	
	// ===============================================================================
	// = FIELDS
	
	public final static String TAG__TEXT = "_TEXT";
	public final static String TAG__BLOCK_ID = "_ID";
	public final static String TAG__BLOCK_TYPE = "_BT";
	public final static String TAG__BLOCK_OWNER_ID = "_OI";
	
	//private Context context;
	private TokenIndex tokenStore;
	protected boolean normalizeToLowerCase;
	protected boolean indexOwnerId;

	// ===============================================================================
	// = METHODS
	
	public TermIndex(DataContext context) {
		tokenStore = context.getTokenIndex();
		normalizeToLowerCase = context.getConfig().getBoolean(WorkflowConfiguration.NORMALIZE2LOWER);
        indexOwnerId = context.getConfig().getBoolean(StoreIndexConfiguration.ELEMENT__INDEX_OWNER_ID);
	}
	
	protected TokenIndex getTokenStore() {
		return tokenStore;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	protected abstract void open();
	
	protected abstract void close();
		
	// ===============================================================================
	// = TOOLS
	
	protected void throwException(final String message, final Throwable nfe, final ElementType type, final String text) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.ERROR, nfe)
			.annotate("type", type.name(), "text", text.trim());
	}
	
	protected void throwException(final String message, final Throwable nfe, final Term term) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.ERROR, nfe)
			.annotate("type", term.element.type.name(), "orig.text", term.text.trim(),
					  "element.text", term.element.text.trim());
	}
	
	protected void throwException(final String message, final Throwable nfe) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.ERROR, nfe);
	}
	
	protected void finalize() throws Throwable {
	     try {
	         close();
	     } finally {
	         super.finalize();
	     }
	 }

}