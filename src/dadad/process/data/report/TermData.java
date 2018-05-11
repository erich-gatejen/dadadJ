package dadad.process.data.report;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import dadad.platform.AnnotatedException;

public class TermData {
		
	// ===============================================================================
	// = FIELDS
	
	public final static int MAX_SIZE_FREQ = 512;
		
	private HashMap<String, TermInfo> termDictionary;
	public final long totalTermSize;
	public final long totalTermByteSize;
	public final int largestTermSize;
	public final int largestTermByteSize;
	public final long[] sizeFrequency;
	public final float meanFreq;
	
	public enum TermInfoPoints {
		TERM_FREQUENCY,
		BLOCK_FREQUENCY;
	}
	
	// ===============================================================================
	// = METHODS
	
	public TermData(final HashMap<String, TermInfo> termDictionary, final long totalTokenSize, final long totalTokenByteSize,
			final int largestTokenSize, final int largestTokenByteSize, final long[] sizeFrequency, final float meanFreq) {
		this.termDictionary = termDictionary;
		this.totalTermSize = totalTokenSize;
		this.totalTermByteSize = totalTokenByteSize;
		this.largestTermSize = largestTokenSize;
		this.largestTermByteSize = largestTokenByteSize;
		this.sizeFrequency = sizeFrequency;
		this.meanFreq = meanFreq;
	}
	
	public ArrayList<TermInfo> getTermList() {	
		if (termDictionary == null) throw new Error("BUG: term dictionary has already been disposed.");
	    return new ArrayList<TermInfo>(termDictionary.values());
	}
	
	public HashMap<String, TermInfo> getTermDictionary() {
		return termDictionary;
	}
	
	public void disposeTermDictionary() {
		// I'm not sure this will make a lot of difference, since the terms will be held in the lists.
		termDictionary = null;
	}
	
	// ===============================================================================
	// = PUBLIC STATIC TOOLS
	
	public static ArrayList<TermInfo> sortedByFrequency(ArrayList<TermInfo> termList) {	
		termList.sort(new Comparator<TermInfo>(){
					@Override
					public int compare(final TermInfo lhs, TermInfo rhs) {
						if (rhs.freq > lhs.freq) return 1;
						else if (rhs.freq == lhs.freq) return 0;
						else return -1;
					}
	    		});
	    return termList;
	}
	
	public static ArrayList<TermInfo> sortedByDocFrequency(ArrayList<TermInfo> termList) {	
		termList.sort(new Comparator<TermInfo>(){
					@Override
					public int compare(final TermInfo lhs, TermInfo rhs) {
						if (rhs.blockFreq > lhs.blockFreq) return 1;
						else if (rhs.blockFreq == lhs.blockFreq) return 0;
						else return -1;
					}
	    		});
	    return termList;
	}
	
	public static void writeTermInfo(final ArrayList<TermInfo> termList, final TermData termData, final BufferedWriter bw,
			final TermInfoPoints infoPoint) {
	    try {
	    	String format = "%1$-" + (termData.largestTermSize + 2) + "s  = %2$d";
	    	
		    for (TermInfo term : termList) {
		    	
		    	switch(infoPoint) {
		    	case TERM_FREQUENCY:
		    		bw.write(String.format(format, term.term, term.freq));
			    	break;
			    	
		    	case BLOCK_FREQUENCY:
		    		bw.write(String.format(format, term.term, term.blockFreq));
			    	break;
		    	}		    

		    	bw.newLine();
		    }
		    
	    } catch (Exception e) {
	    	throw new AnnotatedException("IO error while writing document frequency report file.", e);	    	
	    }
	}
}
