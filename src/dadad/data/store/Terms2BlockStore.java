package dadad.data.store;

import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import dadad.data.DataContext;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.BlockInfo;
import dadad.data.model.Term;
import dadad.data.store.backend.KVStore;
import dadad.data.store.backend.TokenIndex;
import dadad.platform.AnnotatedException;

/**
 * Document store.  NOT THREAD SAFE!
 */
public class Terms2BlockStore implements TermWriter {	
	
	// ===============================================================================
	// = FIELDS
	
	public final static int NOT_SET = 0;
	
	public final static String RESERVED_KEY__TERM = "?T";
		
	//private Context context;
	private KVStore kvStore;
	private boolean normalize2Lowercase;
	
	// WARNING!!!!  This must be the same analyzer as used by the IndexElementStore and TokenIndex backend.
	private Analyzer analyzer = new StandardAnalyzer(TokenIndex.STOP_WORDS_SET);
	
	private BlockInfo currentBlockInfo = new BlockInfo();
	
	// ===============================================================================
	// = INTERFACE
	
	public void submit(BlockInfo blockInfo, String tag, Term[] terms) {		

		for (Term term : terms) {
			if (! term.isAcceptable()) continue;
			
			switch(term.element.type) {

			case STRING:
				if (normalize2Lowercase) {
					increment(blockInfo, term.text.toLowerCase());
				} else {
					increment(blockInfo, term.text);
				}
				break;
				
			case LONG:
			case DOUBLE:
				increment(blockInfo, term.text);
				break;
			
			case TEXT:
				// Tokanizer will normalize.
		        try {
		        	TokenStream tokenStream = analyzer.tokenStream(RESERVED_KEY__TERM, term.text);
		        	tokenStream.reset();
		        	try {
			            while(tokenStream.incrementToken()) {
			            	increment(blockInfo, tokenStream.getAttribute(CharTermAttribute.class).toString());
			            }
			            tokenStream.end();
		        	} finally {
		        		tokenStream.close();
		        	}
		        } catch (IOException ioe) {
		        	// NOP since it is reading from a string.
		        } 
				break;
			
			case TIMESTAMP:
			case BOOLEAN:
			case BREAKING:		
			default:
				throw new Error("UNHANDLED element type.  type=" + term.element.type.name());
			}
		}
		
	}	
	
	// ===============================================================================
	// = METHODS
	
	public Terms2BlockStore(DataContext context) {
		kvStore = context.getKVStore();
		kvStore.openForReadWrite();
		normalize2Lowercase = context.getConfig().getRequiredBoolean(WorkflowConfiguration.NORMALIZE2LOWER);
	}
	
	private HashSet<String> seenTermsPerBlock = new HashSet<String>();
	
	public void increment(final BlockInfo blockInfo, final String term) {
		
		if ((this.currentBlockInfo.ownerId() != blockInfo.ownerId()) || (this.currentBlockInfo.blockId() != blockInfo.blockId())) {
			this.currentBlockInfo = blockInfo;
			seenTermsPerBlock = new HashSet<String>();
		}
		
		try {
			if (! seenTermsPerBlock.contains(term)) {
			
				String mangle = mangle(term);
				Integer ovalue = (Integer) kvStore.get(mangle);
				if (ovalue == null) {
					kvStore.put(mangle, new Integer(1));
				} else {
					kvStore.put(mangle, new Integer(ovalue.intValue() + 1));
				}
				
				seenTermsPerBlock.add(term);
			}
					
		} catch (Exception e) {
			throw new AnnotatedException("Term2Doc store failure.", AnnotatedException.Catagory.ERROR, e)
					.annotate("term", term);
		}
		
	}
	
	public int get(final String term) {
		Integer ovalue = (Integer) kvStore.get(mangle(term));
		if (ovalue != null) return ovalue.intValue();
		return NOT_SET;
	}
	
	private String mangle(final String term) {
		return RESERVED_KEY__TERM + term;
	}
	
	public void close() {
		kvStore.closeForReadWrite();
	}
	
}