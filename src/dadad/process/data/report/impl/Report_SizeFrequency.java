package dadad.process.data.report.impl;

import java.io.BufferedWriter;

import dadad.process.data.report.Report;
import dadad.process.data.report.ReportContext;
import dadad.process.data.report.TermData;

/**
 * Summary.
 */
public class Report_SizeFrequency extends Report {
		
	
	protected String reportName() {
		return "size frequency";
	}

	protected String reportFileName() {
		return "sizefreq";
	}
	
	protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception {
		TermData termData = reportContext.getTermData(false);
		
		int last = (TermData.MAX_SIZE_FREQ - 1);
		for (; last > 0; last--) {
			if (termData.sizeFrequency[last] > 0) break;
		}

		for (int index = 1; index <= last; index++) {
			bw.write(format(Integer.toString(index), termData.sizeFrequency[index]));
			bw.newLine();			
		}
			
	    bw.newLine();			
	}
		
	private String format(final String name, final long value) {
		return String.format("%6s = %2$-16d", name, value);
	}
	
}
