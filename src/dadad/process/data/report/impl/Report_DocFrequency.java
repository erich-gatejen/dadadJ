package dadad.process.data.report.impl;

import java.io.BufferedWriter;
import java.util.ArrayList;

import dadad.process.data.report.Report;
import dadad.process.data.report.ReportContext;
import dadad.process.data.report.TermData;
import dadad.process.data.report.TermInfo;
import dadad.process.data.report.TermData.TermInfoPoints;

/**
 * Block (or document) frequency.
 */
public class Report_DocFrequency extends Report {
		
	
	protected String reportName() {
		return "block frequency";
	}

	protected String reportFileName() {
		return "blockfreq";
	}
	
	protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception {
		TermData termData = reportContext.getTermData(false);
		ArrayList<TermInfo> termList = TermData.sortedByDocFrequency(termData.getTermList());
		TermData.writeTermInfo(termList, termData,bw, TermInfoPoints.BLOCK_FREQUENCY);		
	}
	
}
