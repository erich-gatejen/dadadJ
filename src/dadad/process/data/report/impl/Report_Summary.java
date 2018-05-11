package dadad.process.data.report.impl;

import java.io.BufferedWriter;

import dadad.process.data.report.Report;
import dadad.process.data.report.ReportContext;
import dadad.process.data.report.TermData;

/**
 * Summary.
 */
public class Report_Summary extends Report {
		
	
	protected String reportName() {
		return "summary";
	}

	protected String reportFileName() {
		return "summary";
	}
	
	protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception {
		TermData termData = reportContext.getTermData(false);
		
	    bw.write(format("Total entries", reportContext.source.blocks.length));
	    bw.newLine();
	    bw.write(format("Total terms", termData.getTermList().size()));
	    bw.newLine();
	    bw.write(format("Total term size", termData.totalTermSize));
	    bw.newLine();	    
	    bw.write(format("Total term size (bytes)", termData.totalTermByteSize));
	    bw.newLine();	
	    bw.write(format("Largest term size", termData.largestTermSize));
	    bw.newLine();	
	    bw.write(format("Largest term size (bytes)", termData.largestTermByteSize));
	    bw.newLine();	
	    bw.write(format("Mean frequency", termData.meanFreq));
	    bw.newLine();	
		
	}
		
	private String format(final String name, final long value) {
		return String.format("%1$30s = %2$d", name, value);
	}
	
	private String format(final String name, final float value) {
		return String.format("%1$30s = %2$f", name, value);
	}
	
}
