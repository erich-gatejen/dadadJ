package dadad.data.model;

import java.io.Serializable;

public class ResultMetric implements Serializable {


	// ===============================================================================
	// = FIELDS
	
	private static final long serialVersionUID = 1L;
	
	public final static char METRIC_SEPARATOR_CHARACTER = '|';
    
    public int total;
    public int complete;

    public int passed;
    public int inconclusive;
    public int failed;
    public int fault;
    
    public long startTime;
    public long totalTime;
    

	// ===============================================================================
	// = METHOD
    
    public ResultMetric() {
    }

    public ResultMetric(final int total) {
        this.total = total;
    }
    
    public ResultMetric(final int total, final int complete, final int passed, final int inconclusive, final int failed, final int fault) {
        this.total = total;
        this.complete = complete;
        this.passed = passed;
        this.inconclusive = inconclusive;
        this.failed = failed;
        this.fault = fault;
    }

    public void merge(final ResultMetric metric) {
        total += metric.total;
        complete += metric.complete;
        passed += metric.passed;
        inconclusive += metric.inconclusive;
        failed += metric.failed;
        fault += metric.fault;
    }
   
    public boolean isDone() {
        if (total == complete) return true;
        return false;
    }
    
	@Override
	public String toString() {
		return Integer.toString(total) + METRIC_SEPARATOR_CHARACTER + complete + METRIC_SEPARATOR_CHARACTER + passed +
				METRIC_SEPARATOR_CHARACTER + inconclusive + METRIC_SEPARATOR_CHARACTER + failed + METRIC_SEPARATOR_CHARACTER +
				fault;
    }
	
	public ResultMetric start() {
		startTime = System.currentTimeMillis();
		return this;
	}
	
	public ResultMetric start(final long startTime) {
		this.startTime = startTime;
		return this;
	}
	
	public ResultMetric done() {
		totalTime = System.currentTimeMillis() - startTime;
		return this;
	}
	
	public ResultMetric clone() {
		return new ResultMetric(total, complete, passed, inconclusive, failed, fault);
	}

}

