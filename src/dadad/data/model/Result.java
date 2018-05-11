package dadad.data.model;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import dadad.platform.AnnotatedException;
import dadad.platform.Constants;

public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	// ===============================================================================
	// = FIELDS
	
	public final static int MAX_PLY_RENDERED = 10;
	
	public ResultType type;
	public String name;
	public ResultMetric metric;
	public String reason;
	public Throwable fault;
	
	public HashMap<String, Result> children;
	
	public final HashMap<String, Object> annotations = new HashMap<String, Object>();
	
	// ===============================================================================
	// = METHOD
	
	public Result() {
	    this(null);
	}
	
	public Result(final String name) {
	    this.name = name;
	    type = ResultType.NEW;
	    metric = type.getMetric();
	    reason = "";
	    children = new HashMap<String, Result>();
	}
	
	public Result(final String name, final String reason, final Throwable fault) {
	    this.name = name;
	    type = ResultType.FAULT;
	    metric = ResultType.FAULT.getMetric();
	    this.reason = reason;
	    fault(fault);
	    children = new HashMap<String, Result>();
	}
	
	public Result(final String name, final ResultType type) {
	    this.name = name;
	    this.type = type;
	    this.metric = type.metric.clone();
	    children = new HashMap<String, Result>();
	}
	
	public Result(final String name, final String reason, final ResultType type) {
	    this.name = name;
	    this.type = type;
	    this.reason = reason;
	    this.metric = type.metric.clone();
	    children = new HashMap<String, Result>();
	}
	
	public Result(final String name, final ResultType type, final ResultMetric metric) {
	    this.name = name;
	    this.type = type;
	    this.metric = metric.clone();
	    children = new HashMap<String, Result>();
	}
	
	public Result addChild(final Result result) {
	    children.put(result.name, result);
	    return result;
	}
	
	public Result addNewChild(String name) {
		Result newChild = new Result(name);
	    children.put(name, newChild);
	    return newChild;
	}
		
	public Result getChild(String name) {
	    return children.get(name);
	}
	
	public Result message(final String message) {
	    this.reason = message;
	    return this;
	}
	
	public Result fault(final Throwable fault) {
		if (fault instanceof InvocationTargetException) this.fault = ((InvocationTargetException) fault).getTargetException();
	    else this.fault = fault;
	    return this;
	}
	
	public Result merge(final Result result) {
	    if (result.metric != null) this.metric.merge(result.metric);
	    if (result.reason == null) this.reason = result.reason;
	    if (result.fault == null) fault(this.fault);
	    return this;
	}
	
	public Result set(final ResultType resultType, final String message) {
	    type = resultType;
	    metric = type.getMetric();
	    this.reason = message;
	    return this;
	}
	
	// ===============================================================================
	// = WORKFLOW
	
	public static Result start(final String name) {
		Result result = new Result(name);	
		result.metric.start();
		return result;
	}

    public Result start() {
        metric.start();
        return this;
    }
	
	public Result transition(final ResultType resultType) {
		type = resultType;
		metric = resultType.transition(metric);
		return this;
	}
	
	/**
	 * Close makes the result is final.  This will also resolve it.
	 * @return
	 */
	public Result close() {
		metric.done();
		return resolve();
	}

	/**
	 * Resolve the result.  It will set the result type to match the metric values.
	 * @return this
	 */
	public Result resolve() {
	
		// Adapted from Things.
	    if (children.size() > 0) {
	
//	        metric = new ResultMetric();
	        boolean interrupted = false;
	
	        // Detect if
	        for (Result result : children.values()) {
	            if (result.type.interrupted) {
	                interrupted = true;
	            }
	        }
	
	        for (Result result : children.values()) {
	            if (interrupted && !result.type.isTerminal())
	                metric.merge(ResultType.INCONCLUSIVE.getMetric());
	            else
	                metric.merge(result.metric);
	        }

	    } else if (metric.total < 1) {
            metric.merge(ResultType.PASSED.getMetric());
        }
	
	    if (metric.isDone()) {
	        if (metric.fault > 0) type = ResultType.FAULT;
	        else if (metric.failed > 0) type = ResultType.FAILED;
	        else if (metric.total == metric.passed) type = ResultType.PASSED;
	        else type = ResultType.INCONCLUSIVE;
	    } else {
	        if (metric.fault > 0) type = ResultType.FAULT;
	        else if (metric.failed > 0)
	            type = ResultType.FAILING;
	        else type = ResultType.INCONCLUSIVE;
	    }
	
	    return this;
	}
	
	public String renderLongForm() {
		return renderLongForm(0);
	}
	
	public String renderLongForm(final int ply) {
	    if (ply > MAX_PLY_RENDERED) return "";
	
	    // Adapted from Things.
	    StringBuilder sbPlyPrefix = new StringBuilder();
	    for (int index = 0; index < ply; index++) {
	        sbPlyPrefix.append("    ");
	    }
	    String pp = sbPlyPrefix.toString();
	
	    StringBuilder sb = new StringBuilder();
	    sb.append(Constants.NEWLINE);
	
	    if (ply == 0) {
	        sb.append("- RESULT -----------------------------------------------------------------------------");
	    } else {
	        sb.append(pp).append("- ").append(ply).append(" - child result  ")
	                .append("-----------------------------------------------------------------".substring(ply *
	                        3));
	    }
	
	    sb.append(Constants.NEWLINE);
	    sb.append(pp).append("name=").append(name).append(Constants.NEWLINE);
	    sb.append(pp).append("type=").append(type.name()).append(Constants.NEWLINE);
	    sb.append(pp).append("metric=").append(metric.toString()).append(Constants.NEWLINE);
	    sb.append(pp).append("reason=").append(reason).append(Constants.NEWLINE);
	    for (String annotation : annotations.keySet()) {
	    	sb.append(pp).append(annotation).append("=").append(annotations.get(annotation).toString()).append(Constants.NEWLINE);	
	    }
	
	    if ((fault != null) && (fault instanceof java.lang.reflect.InvocationTargetException)) {
	        fault = fault.getCause();
	    }
	
	    if (fault != null) {
	
	        sb.append(pp).append("fault=").append(Constants.NEWLINE);
	        sb.append(pp).append("   ...fault........................").append(Constants.NEWLINE);
	        BufferedReader br = new BufferedReader(new StringReader(AnnotatedException.render(fault, true)));
	        try {
	            String faultLine = br.readLine();
	            while (faultLine != null) {
	                sb.append(pp).append("   ").append(faultLine).append(Constants.NEWLINE);
	                faultLine = br.readLine();
	            }
	        } catch (Exception e) {
	            throw new Error("BUG BUG BUG!!!  IOErrors are impossible here.");
	        }
	        sb.append(pp).append("   ................................").append(Constants.NEWLINE);
	    }
	
	    for (Result childResult : children.values()) {
	        sb.append(childResult.renderLongForm(ply + 1));
	    }
	
	    if (ply == 0) {
	        sb.append("--------------------------------------------------------------------------------------");
	    } else {
	        sb.append(pp).append
	                ("--------------------------------------------------------------------------------------"
	                .substring(ply * 3)).append(Constants.NEWLINE);
	    }
	
	    return sb.toString();
	}

    public String renderLineForm() {
        return renderLineForm(0);
    }

	public String renderLineForm(final int ply) {
	    if (ply > MAX_PLY_RENDERED) return "";
	
	    // Adapted from Things.
	    StringBuilder sbPlyPrefix = new StringBuilder();
	    for (int index = 0; index < ply; index++) {
	        sbPlyPrefix.append("   ");
	    }
	    String pp = sbPlyPrefix.toString();
	
	    StringBuilder sb = new StringBuilder();
	    sb.append(pp).append("name=[").append(name);
	    sb.append("], ").append("type=[").append(type.name());
	    sb.append("], ").append("metric=[").append(metric.toString());
	    sb.append("], ").append("reason=[").append(reason).append("]");
	    for (String annotation : annotations.keySet()) {
	    	sb.append(", ").append(annotation).append("=[").append(annotations.get(annotation).toString()).append("]");	
	    }
	
	    if ((fault != null) && (fault instanceof java.lang.reflect.InvocationTargetException)) {
	        fault = fault.getCause();
	    }
	
	    if (fault != null) {
	        sb.append(", ").append("fault=[").append(fault.getClass().getName()).append("]");
	        sb.append(", ").append("faultMessage=[").append(fault.getMessage()).append("]");
	    }
	    
	    sb.append(Constants.NEWLINE);
	
	    for (Result childResult : children.values()) {
	        sb.append(childResult.renderLineForm(ply + 1));
	    }
	
	    return sb.toString();
	}

	@Override
	public String toString() {
		//resolve();
	    return renderLongForm(0);
	}
	
	public String toStringShortForm() {
		resolve();		
		return type.name() + ResultMetric.METRIC_SEPARATOR_CHARACTER + metric.toString() + ResultMetric.METRIC_SEPARATOR_CHARACTER +
				name;
	}
	
	public Result annotate(final String name, final Object value) {
		annotations.put(name, value.toString());		
		return this;
	}

	public Result annotate(Object... nv) {
		if (nv == null) throw new Error("BUG: cannot annotate null nv.");
		if ((nv.length % 2) > 0) throw new Error("BUG: nv must be even name/value pairs.");
		for (int rover = 0; rover < nv.length; rover = rover + 2) {
			annotate(nv[rover].toString(), nv[rover + 1].toString());
		}
		return this;
	}

    /**
     * This will prune all results that are not failed or have failed children.  It doesn't work very well unless
     * the result is closed() or resolved().
     * @return this
     */
	public Result pruneNotFailed() {
        pruneNotFailed(this);
        return this;
    }

    private void pruneNotFailed(final Result ply) {
	    Set<String> keySet = ply.children.keySet();
	    String[] keys = keySet.toArray(new String[keySet.size()]);
        for (String name : keys) {
            Result child = ply.children.get(name);
            if (! child.type.failedOrFailing) {
                ply.children.remove(name);
            } else {
                pruneNotFailed(child);
            }
        }
    }
	
	// ===============================================================================
	// = CONVENIENCE
	
	public static Result newResult(final String name) {
	    return new Result(name, ResultType.NEW, ResultType.NEW.getMetric());
	}
	
	public static Result inconclusive(final String name) {
	    return new Result(name, ResultType.INCONCLUSIVE, ResultType.INCONCLUSIVE.getMetric());
	}
	
	public static Result pass(final String name) {
        return new Result(name, ResultType.PASSED, ResultType.PASSED.getMetric());
    }
		
	public static Result fail(final String name) {
	    return new Result(name, ResultType.FAILED, ResultType.FAILED.getMetric());
	}
	
	public static Result fail(final String name, final Throwable t) {
	    return new Result(name, ResultType.FAILED, ResultType.FAILED.getMetric()).fault(t);
	}
	
	public static Result fault(final String name) {
	    return new Result(name, ResultType.FAULT, ResultType.FAULT.getMetric());
	}
	
	public static Result fault(final String name, final Throwable t) {
	    return new Result(name, ResultType.FAULT, ResultType.FAULT.getMetric()).fault(t);
	}
	
}
