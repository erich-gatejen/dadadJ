package dadad.system.data;

import dadad.data.DataContext;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.process.WorkflowEngine;
import dadad.process.WorkflowEngineBlock;
import dadad.process.WorkflowStep;
import dadad.process.WorkflowStepsBuilder;
import dadad.process.data.wf.Processor;
import dadad.process.data.wf.WFSBlockAcceptor;
import dadad.process.data.wf.WFSBlockReader;
import dadad.process.data.wf.WFSElementAcceptor;
import dadad.process.data.wf.WFSLister;
import dadad.process.data.wf.WFSource;

/**
 * The lister workflow.
 */
public class ListerWorkflow extends Workflow {	
	
	// ===============================================================================
	// = FIELDS
	
	private WorkflowEngine<Block> processor;
	private WorkflowStep<Object, DataContext> wfSource;
	private WorkflowStep<Block, DataContext> wfBlockReader;
	private WorkflowStep<Block, DataContext> wfBlockAcceptor;
	private WorkflowStep<Block, DataContext> wfProcessor;
	private WorkflowStep<Block, DataContext> wfElementAcceptor;
	private WorkflowStep<Block, DataContext> wfLister;	
	
	// ===============================================================================
	// = METHODS
	
	public ListerWorkflow(final DataContext context) {
		super(context);	
	}
	
	public void configure() {
	
		wfSource = new WFSource().set(context);
		wfBlockReader = new WFSBlockReader().set(context);
		wfBlockAcceptor = new WFSBlockAcceptor().set(context);
		wfProcessor = Processor.getProcessor(context, context.getConfig().getRequired(WorkflowConfiguration.PROCESSOR), true);
		wfElementAcceptor = new WFSElementAcceptor().set(context);
		wfLister = new WFSLister().set(context);
		
		WorkflowStep<?, ?>[] startSteps = new WorkflowStepsBuilder()
				.add(wfSource)
				.add(wfBlockReader)
				.add(wfBlockAcceptor)
				.add(wfProcessor)
				.add(wfElementAcceptor)
				.add(wfLister)			
				.build();
		
		WorkflowStep<?, ?>[] processSteps = new WorkflowStepsBuilder()
				//.add(wfSource)
				.add(wfBlockReader)
				.add(wfBlockAcceptor)
				.add(wfProcessor)			
				.add(wfElementAcceptor)
				.add(wfLister)	
				.build();
		
		WorkflowStep<?, ?>[] endSteps = new WorkflowStepsBuilder()
				.add(wfBlockReader)
				.add(wfProcessor)
				.add(wfLister)	
				.add(wfSource)			// Must be last, because others may touch the source.
				.build();
				
		processor = new WorkflowEngineBlock(context.getConfig(), startSteps, processSteps, null, endSteps, null);

	}

	public void process(final String url) {
		
		if (processor == null) throw new Error("You must call configure() before process().");
		
		context.getConfig().set(WFSBlockReader.WFSBRConfiguration.DOCUMENT_URL, url);
				
		try {
			processor.process();
				
		} catch (Exception e) {
			throw new AnnotatedException("Listing failed.", AnnotatedException.Catagory.FAULT, e)
					.annotate("source.url", url);
			
		}
		
	}

	public void close() {
		if (processor == null) throw new Error("You must call configure() before close().");
		processor.close();
	}
	
}
