package dadad.process.data.report.impl;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;

import dadad.data.config.ReportConfiguration;
import dadad.platform.AnnotatedException;
import dadad.process.data.report.Report;
import dadad.process.data.report.ReportContext;
import dadad.process.data.report.TermData;
import dadad.process.data.report.TermInfo;

/**
 * Term frequency.
 */
public class Report_FrequencyRank extends Report {		
	
	protected String reportName() {
		return "frequency average by rank";
	}

	protected String reportFileName() {
		return "freq.avg.by.rank";
	}
	
	protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception {
		TermData termData = reportContext.getTermData(false);
		ArrayList<TermInfo> termList = TermData.sortedByFrequency(termData.getTermList());
		
		long numberRanks = reportContext.context.getConfig().getRequiredLong(ReportConfiguration.RANKS);
		if (numberRanks < 2) throw new AnnotatedException("Pointless report if number of ranks is less than 2.")
			.annotate(ReportConfiguration.RANKS.property(), Long.toString(numberRanks));
		long[] ranks = new long[(int) numberRanks + 1];
		
		long perBucket = termList.size() / numberRanks;
		int currentRank = 1;
		Iterator<TermInfo> iter = termList.iterator();	
		
		TermInfo currentTerm;
		long currentBucketed = 0;
		long accum = 0;
		while ((currentRank <= numberRanks) && (iter.hasNext())) {
		    currentTerm = iter.next();
		    accum += currentTerm.freq;
		    
		    currentBucketed++;
		    if (currentBucketed == perBucket) {
		    	ranks[currentRank] = accum / currentBucketed;
				currentBucketed = 0;
				currentRank++;
				accum = 0;
		    }	
		}
		
		if (currentBucketed > 0) {
	    	ranks[currentRank] = accum / currentBucketed;			
		}
		
		bw.write("Terms per rank = ");
		bw.write(Long.toString(perBucket));
		bw.newLine();
		
    	String format = "%1$-" + ((String.valueOf(numberRanks).length()) + 2) + "s  = %2$d";
    	for (currentRank = 1; currentRank <= numberRanks; currentRank++) {
    		bw.write(String.format(format, currentRank, ranks[currentRank]));
    		bw.newLine();
    	}
		
	}

}
