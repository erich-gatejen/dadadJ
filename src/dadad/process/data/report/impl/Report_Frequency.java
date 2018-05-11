package dadad.process.data.report.impl;

import java.io.BufferedWriter;
import java.util.ArrayList;

import dadad.process.data.report.Report;
import dadad.process.data.report.ReportContext;
import dadad.process.data.report.TermData;
import dadad.process.data.report.TermInfo;
import dadad.process.data.report.TermData.TermInfoPoints;

/**
 * Term frequency.
 */
public class Report_Frequency extends Report {		
	
	protected String reportName() {
		return "frequency";
	}

	protected String reportFileName() {
		return "freq";
	}
	
	protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception {
		TermData termData = reportContext.getTermData(false);
		ArrayList<TermInfo> termList = TermData.sortedByFrequency(termData.getTermList());
		TermData.writeTermInfo(termList, termData,bw, TermInfoPoints.TERM_FREQUENCY);		
	}

}
