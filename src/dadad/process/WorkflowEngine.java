package dadad.process;

import java.util.HashSet;

import dadad.data.config.WorkflowConfiguration;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;
import dadad.platform.services.Logger;
import dadad.platform.services.PlatformInterfaceRequestor;
import dadad.system.WorkKernel;

public abstract class WorkflowEngine<E> {

	// ===============================================================================
	// = FIELDS
	
	protected final Configuration configuration;
	protected boolean doheaderSteps = false;
	
	private final WorkflowStep<?, ?>[] startSteps;
	private final WorkflowStep<?, ?>[] headerSteps;
	private final WorkflowStep<?, ?>[] processSteps;
	private final WorkflowStep<?, ?>[] endSteps;
	
	private final HashSet<WorkflowStep<?, ?>> allSteps;
	
	// Available to subclasses.
	protected final int[] rejectForwardTargets;
	protected final Logger logger;

	// ===============================================================================
	// = ABSTRACT

	abstract protected void processInternal();
	
	// ===============================================================================
	// = METHOD
	
	public WorkflowEngine(final Configuration configuration, final WorkflowStep<?, ?>[] startSteps, WorkflowStep<?, ?>[] headerSteps,
			WorkflowStep<?, ?>[] processSteps, WorkflowStep<?, ?>[] endSteps, final int[] rejectForwardTargets) {
		
		if ((rejectForwardTargets != null) && (rejectForwardTargets.length != processSteps.length))
			throw new Error("BUG BUG BUG!  rejectForwardTargets.length (" + rejectForwardTargets.length + ") != processSteps.length ("
					+ processSteps.length + ").  They must be the same if rejectForwardTargets is not null.");
		
		this.startSteps = startSteps;
		this.headerSteps = headerSteps;
		this.processSteps = processSteps;
		this.endSteps = endSteps;
		this.configuration = configuration;
		
		allSteps = new HashSet<WorkflowStep<?, ?>>();
		if (startSteps != null) for (WorkflowStep<?, ?> step : startSteps) allSteps.add(step);
		if (headerSteps != null) for (WorkflowStep<?, ?> step : headerSteps) allSteps.add(step);
		if (processSteps != null) for (WorkflowStep<?, ?> step : processSteps) allSteps.add(step);
		if (endSteps != null) for (WorkflowStep<?, ?> step : endSteps) allSteps.add(step);
		
		this.rejectForwardTargets = rejectForwardTargets;
		
		logger = PlatformInterfaceRequestor.getPlatformInterface().getLogger();
		
	}	
	
	public WorkflowStep<?, ?>[] getWorkflowSteps() {
		return processSteps;
	}
	
	public WorkflowStep<?, ?>[] getHeaderWorkflowSteps() {
		return headerSteps;
	}
	
	public void process() {		
		
		if ((headerSteps != null) && (configuration.getBoolean(WorkflowConfiguration.HEADER_IS_PRESENT))) doheaderSteps = true;
	
		for (WorkflowStep<?, ?> step : allSteps) {
			ConfigurationType[] required = step._required();
			if (required != null) {
				for (ConfigurationType type : required) {
					configuration.getRequired(type);
				}
			}			
		}
		
		try {
			logger.debug("WORKFLOW - start steps.");
			if (startSteps != null) {
				for (WorkflowStep<?, ?> step : startSteps) {
					if (step.isClosed()) {
						throw new Error("BUG BUG BUG.  Cannot re-start() a closed workflow step.");
					}
					step.start();
				}			
			}
			
			WorkKernel.getSystemInterface().yieldToSystem();
			
			processInternal();
			
			WorkKernel.getSystemInterface().yieldToSystem();
				
			logger.debug("WORKFLOW - end steps.");
			if (endSteps != null) {
				for (WorkflowStep<?, ?> step : endSteps) {
					step.end();
				}			
			}
			
		} catch (Throwable t) {
			close();
			throw t;
		}
		
	}
	
	public Configuration getConfig() {
		return configuration;
	}
	
	public void close() {
		if (startSteps != null) {
			for (WorkflowStep<?, ?> step : startSteps) {
				step.closeIfNotClosed();
			}
		}
		if (headerSteps != null) {
			for (WorkflowStep<?, ?> step : headerSteps) {
				step.closeIfNotClosed();
			}
		}
		if (processSteps != null) {
			for (WorkflowStep<?, ?> step : processSteps) {
				step.closeIfNotClosed();
			}
		}
		if (endSteps != null) {
			for (WorkflowStep<?, ?> step : endSteps) {
				step.closeIfNotClosed();
			}	
		}
	}
		
}
