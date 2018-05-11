package dadad.data.model;

import java.io.Serializable;

public enum ResultType implements Serializable {

	NEW(false, false, false, false, new ResultMetric(0, 0, 0, 0, 0, 0)),
	FAILING(false, false, true, false, new ResultMetric(1, 0, 0, 0, 0, 0)),
	COMMENT(false, false, false, false, new ResultMetric(1, 0, 0, 0, 0, 0)),
	INCONCLUSIVE(true, false, false, false, new ResultMetric(1, 1, 0, 1, 0, 0)),
	PASSED(true, false, false, false, new ResultMetric(1, 1, 1, 0, 0, 0)),
	DATA(true, false, false, false, new ResultMetric(1, 1, 1, 0, 0, 0)), 
	FAILED(true, true, true, false, new ResultMetric(1, 1, 0, 0, 1, 0)),
	FAULT(true, true, true, true, new ResultMetric(1, 1, 0, 0, 0, 1));
	
	final boolean terminal;
	final boolean failed;
	final boolean failedOrFailing;
	final boolean interrupted;
	final ResultMetric metric;
	
	private ResultType(boolean terminal, boolean failed, boolean failedOrFailing, boolean interrupted, ResultMetric metric) {
	    this.terminal = terminal;
	    this.failed = failed;
	    this.failedOrFailing = failedOrFailing;
	    this.interrupted = interrupted;
	    this.metric = metric;
	}
	
	public boolean isTerminal() { 
	    return terminal; 
	}
	
	public boolean isFailed() { 
	    return failed; 
	}
	
	public boolean isFailedOrFailing() { 
	    return failedOrFailing; 
	}
	
	public boolean isInterrupted() { 
	    return interrupted; 
	}
	
	public ResultMetric getMetric() { 
	    return metric.clone(); 
	}
	
    public ResultMetric transition(final ResultMetric metric) {
    	ResultMetric result = getMetric();
    	result.startTime = metric.startTime;
    	result.totalTime = metric.totalTime;
    	return result;   	
    }
    

}


