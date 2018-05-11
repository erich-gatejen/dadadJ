package dadad.system.data;

import dadad.data.DataContext;
import dadad.data.DataType;
import dadad.data.config.DataConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.process.WorkflowStep;
import dadad.process.data.wf.Processor;

/**
 * Abstract workflow.
 */
public abstract class Workflow {	
	
	// ===============================================================================
	// = FIELDS

	protected final DataContext context;
	
	// ===============================================================================
	// = METHODS
	
	public Workflow(final DataContext context) {
		this.context = context;	
	}
	
	public WorkflowStep<Block, DataContext> getDataProcessorStep() {
		return getElementProcessorStep(context);
	}
	
	public static WorkflowStep<Block, DataContext> getElementProcessorStep(final DataContext context) {
			
		// Try WorkflowConfiguration.PROCESSOR to get a processor.  If that fails, try DataConfiguration.TYPE.
		WorkflowStep<Block, DataContext> wfProcessor = Processor.getProcessor(context, context.getConfig().get(WorkflowConfiguration.PROCESSOR), false);
		if (wfProcessor == null) {
			String dataTypeName = context.getConfig().get(DataConfiguration.TYPE);
			if (dataTypeName != null) {
			
				try {
					dataTypeName = dataTypeName.trim().toUpperCase();
					wfProcessor = Processor.getProcessor(context, DataType.valueOf(dataTypeName), false);
					
				} catch (IllegalArgumentException iae) {
					throw new AnnotatedException("Data type configuration bad.  Unrecognized type.")
						.annotate("property.name", DataConfiguration.TYPE.property(), "value", dataTypeName);					
				}
				
			}
		}
		
		if (wfProcessor == null) {
			throw new AnnotatedException("Could not determine data processor.  Set " + WorkflowConfiguration.PROCESSOR.property() + " or " +
					DataConfiguration.TYPE.property() + ".");
		}
		return wfProcessor;
	}
	
	public static WorkflowStep<Block, DataContext> getTermProcessorStep(final DataContext context) {
		// Optional
		return Processor.getProcessor(context, context.getConfig().get(WorkflowConfiguration.TERMPROCESSOR), false);
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	abstract public void configure();

	abstract public void process(final String url);

	abstract public void close();
	
}
