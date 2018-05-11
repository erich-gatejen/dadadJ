package dadad.system.data;

import dadad.data.DataContext;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.process.WorkflowEngine;
import dadad.process.WorkflowEngineBlock;
import dadad.process.WorkflowStep;
import dadad.process.WorkflowStepsBuilder;
import dadad.process.data.wf.WFSIndexBlockStore;
import dadad.process.data.wf.WFSBlockReader;
import dadad.process.data.wf.WFSource;

/**
 * Start making it configurable.
 */
public class AnalysisWorkflow extends Workflow {	
	
	// ===============================================================================
	// = FIELDS
	
	private WorkflowEngine<Block> processor;
	private WorkflowStep<Object, DataContext> wfSource;
	private WorkflowStep<Block, DataContext> wfBlockReader;
	private WorkflowStep<Block, DataContext> wfProcessor;
	private WorkflowStep<Block, DataContext> wfStore;
	
	// ===============================================================================
	// = METHODS
	
	public AnalysisWorkflow(final DataContext context) {
		super(context);	
	}
	
	public void configure() {

		wfSource = new WFSource().set(context);
		wfBlockReader = new WFSBlockReader().set(context);
		wfStore = new WFSIndexBlockStore().set(context);
		wfProcessor = getDataProcessorStep();

		
		WorkflowStep<?, ?>[] startSteps = new WorkflowStepsBuilder()
				.add(wfSource)
				.add(wfBlockReader)
				.add(wfProcessor)
				.add(wfStore)
				.build();
		
		WorkflowStep<?, ?>[] headerSteps = new WorkflowStepsBuilder()
				//.add(wfSource)
				.add(wfBlockReader)
				.add(wfProcessor)
				//.add(wfStore)
				.build();
		
		WorkflowStep<?, ?>[] processSteps = new WorkflowStepsBuilder()
				//.add(wfSource)
				.add(wfBlockReader)
				.add(wfProcessor)
				.add(wfStore)
				.build();
		
		WorkflowStep<?, ?>[] endSteps = new WorkflowStepsBuilder()
				.add(wfBlockReader)
				.add(wfProcessor)
				.add(wfStore)
				.add(wfSource)		// Must be last because the others touch the source.
				.build();
				
		processor = new WorkflowEngineBlock(context.getConfig(), startSteps, headerSteps,
				endSteps, processSteps, null);

	}

	public void process(final String url) {
		
		if (processor == null) throw new Error("You must call configure() before process().");		
		context.getConfig().set(WFSBlockReader.WFSBRConfiguration.DOCUMENT_URL, url);
				
		try {
			processor.process();
				
		} catch (Exception e) {
			throw new AnnotatedException("Processing failed.", AnnotatedException.Catagory.FAULT, e)
					.annotate("source.url", url);
			
		}
		
	}

	public void close() {
		if (processor == null) throw new Error("You must call configure() before close().");
		processor.close();
	}
	
}
