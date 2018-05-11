package dadad.process.data.report;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import dadad.data.model.Document;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import dadad.data.DataContext;
import dadad.data.model.Term;
import dadad.data.store.TermIndex;
import dadad.data.store.IndexTermStoreReader;
import dadad.data.store.Terms2BlockStore;
import dadad.platform.AnnotatedException;
import dadad.platform.Context;

import static dadad.platform.AnnotatedException.Catagory.FAULT;

/**
 * Start making it configurable.
 */
public class ReportContext {	
	
	// ===============================================================================
	// = FIELDS

	private Logger sysLogger = Logger.getLogger(DataContext.LOGGER__SYSTEM);
	
	public final Context context;
	public final Document source;
	public final IndexTermStoreReader termStore;
	public final Terms2BlockStore terms2DocStore;
	
	private TermData termDataCache;
	private List<String> tagListCache;  // DO NOT USE THIS FOR getTagList()

	
	// ===============================================================================
	// = METHODS
	
	public ReportContext(final Context context, final Document source, final IndexTermStoreReader termStore,
                         final Terms2BlockStore terms2DocStore) {
		this.context = context;	
		this.source = source;
		this.termStore = termStore;
		this.terms2DocStore = terms2DocStore;
	}
	
	public List<String> getTagList() {
		List<String> tagList = new LinkedList<String>();
	    if (source.terms != null) {
	    	for (Term term : source.terms) {
	    		tagList.add(term.element.tag);
	    	}	    	
	    } else {
	    	tagList.add(TermIndex.TAG__TEXT);
	    }
	    tagListCache = tagList;
	    return tagList;
	}
	
	public int getDocCount(final String term) {
		return terms2DocStore.get(term);
	}
	
	/**
	 * Get the term data.  NOT THREAD SAFE.
	 * @param forceRefresh
	 * @return term data
	 */
	public TermData getTermData(boolean forceRefresh) {
		return getTermData(null, forceRefresh);
	}
	
	/**
	 * Get the term data.  NOT THREAD SAFE.  
	 * @param tagList
	 * @param forceRefresh
	 * @return term data
	 */
	public TermData getTermData(final List<String> tagList, boolean forceRefresh) {
		List<String> actualTagList = tagList;
		if (actualTagList == null) actualTagList = getTagList();
		
		if ((termDataCache == null) || (forceRefresh) || (! actualTagList.equals(tagListCache))) {
			HashMap<String, TermInfo> termInfos = new HashMap<String, TermInfo>();
			long totalTokenSize = 0;
			long totalTokenByteSize = 0;
			int largestTokenSize = 0;
			int largestTokenByteSize = 0;
			long[] sizeFrequency = new long[TermData.MAX_SIZE_FREQ];
			
			long meanFreqAccum = 0;

		    try {
		    	
		    	// Term merging
			    for (String tag : actualTagList) {
			    	long numTerms = 0;
			    	
			    	TermsEnum termsEnum = termStore.getTerms(tag);
			    	if (termsEnum == null) {
			    		sysLogger.fine("No terms for field [" + tag + "].");
		
			    	} else {
				    	BytesRef ref = termsEnum.next();
				    	
				    	while (ref != null) {
					    	totalTokenByteSize += ref.length;
					    	if (ref.length > largestTokenByteSize) largestTokenByteSize = ref.length;
					    	
				    		String term = ref.utf8ToString();
				    		totalTokenSize += term.length();
				    		if (term.length() > largestTokenSize) largestTokenSize = term.length();
				    		
				    		TermInfo termInfo = termInfos.get(term);
				    		if (termInfo == null) {
				    			termInfo = new TermInfo();
				    			termInfos.put(term, termInfo);
					    		termInfo.term = term;
					    		numTerms++;
				    		}
				    		
				    		// Temp hack. 
				    		termInfo.freq += termsEnum.totalTermFreq();
				    		
				    		// Cannot store this way if using lucene.  We will have to pull it from the kvstore.
				    		// termInfo.docFreq = termsEnum.docFreq();			    // This isn't right and it is going to be a titanic pain in the ass to fix it.		
				    		
				    		ref = termsEnum.next();
				    	}	
				    	
				    	sysLogger.fine("Merged all terms for field [" + tag + "].  Number of NEW terms=[" + numTerms + "]");
			    	}
			    }
			    
			    // Get size frequency numbers
			    for (TermInfo info : termInfos.values()) {
			    	if (info.term.length() < TermData.MAX_SIZE_FREQ) sizeFrequency[info.term.length()]++;
			    	info.blockFreq = terms2DocStore.get(info.term);
			    	meanFreqAccum += info.freq;
			    }
			    
		    } catch (Exception e) {
		    	throw new AnnotatedException("Fault while getting Term Info.", FAULT, e);
		    }
		    
		    termDataCache = new TermData(termInfos, totalTokenSize, totalTokenByteSize, largestTokenSize, largestTokenByteSize, sizeFrequency,
		    		(float)meanFreqAccum / termInfos.size());	    
		}
		
		return termDataCache;
	}
	
}
