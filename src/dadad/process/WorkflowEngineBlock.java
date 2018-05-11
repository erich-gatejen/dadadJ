package dadad.process;

import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Block;
import dadad.data.model.BlockType;
import dadad.platform.AnnotatedException;
import dadad.platform.Constants;
import dadad.platform.config.Configurable;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;
import dadad.system.WorkKernel;

public class WorkflowEngineBlock extends WorkflowEngine<Block> implements Configurable {

	// ===============================================================================
	// = FIELDS
	
	private int blocksPerYield;
	
	// ===============================================================================
	// = INTERFACE
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class };		
	}


	// ===============================================================================
	// = METHOD
	
	public WorkflowEngineBlock(final Configuration configuration, final WorkflowStep<?, ?>[] startSteps, WorkflowStep<?, ?>[] headerSteps,
			WorkflowStep<?, ?>[] processSteps, WorkflowStep<?, ?>[] endSteps, final int[] rejectForwardTargets) {
		super(configuration, startSteps, headerSteps, processSteps, endSteps, rejectForwardTargets);
		
		blocksPerYield = configuration.getRequiredInt(WorkflowConfiguration.BLOCKS_PER_YEILD);
	}	
	
	public void processInternal() {
		
		// Header steps?
		Block block = null;
		Block nextBlock = null;
	
		if (doheaderSteps == true) {	
			try {
				for (WorkflowStep<?, ?> step : getHeaderWorkflowSteps()) {
					block = (Block) step.step(block);
					if (block == null) break;
				}
				
			} catch (AnnotatedException ae) {
				if ((block != null)&&(block instanceof Block))
					throw ae.annotate("block.blockId", ((Block) block).info.blockId());
				throw ae;
			}
		}
		
		WorkflowStep<?, ?>[] steps = getWorkflowSteps();
		
		// Processing will end when the first workflow step fails to yield a block
		int stepNum = 0;
		block = null;
		int blocksToYield = 0;
		while (true) {

			try {

				// First step cannot fail forward.
				block = (Block) steps[0].step(null);
				if (block == null) break;
				
				if (logger.isTrace()) {
					logger.trace("WORKFLOW BLOCK - Entry step complete", "block.blockId", block.info.blockId());
				}
				
				WorkflowStep<?, ?> step;
				for (stepNum = 1; stepNum < steps.length; stepNum++) {
				
					step = steps[stepNum];
					if ((block.info.type() == BlockType.INTERSPACE) && (! step._takesInterspace())) {
						if (logger.isTrace()) {
							logger.trace("WORKFLOW BLOCK - Interspace not processed; skipping step.", "block.blockId", block.info.blockId(),
									"step.class", step.getClass().getName());
						}
						continue;		
					}
					
					nextBlock = (Block) step.step(block);
					
					// Rejected by the step ???
					if (nextBlock == null) {
								
						if  (rejectForwardTargets[stepNum] != Constants.NO_FOWARD) {
									
							// rejected and forwarded
							if (rejectForwardTargets[stepNum] >= steps.length) 
								throw new AnnotatedException("Bad reject forward step.  Workflow steps contructed wrong.", AnnotatedException.Catagory.FAULT)
									.annotate("bad.forward.step", rejectForwardTargets[stepNum]);
							
							// forward - the -1 is because the for loop is going to increment it
							stepNum = rejectForwardTargets[stepNum] - 1;
							
							if (logger.isTrace()) {
								logger.trace("WORKFLOW BLOCK - Step rejected block.  Forwarding.", "block.blockId", block.info.blockId(),
										"step.class", step.getClass().getName(), "target.stepnum", stepNum + 1, "target.step.class",
										steps[stepNum + 1].getClass().getName());
							}
						
						} else {
							
							// Rejected and workflow stopped
							if (logger.isTrace()) {
								logger.trace("WORKFLOW BLOCK - Step rejected block.  Halting workflow.", "block.blockId", block.info.blockId(),
										"step.class", step.getClass().getName());
							}
							break;
						}
							
					} else {
						block = nextBlock;
						if (logger.isTrace()) {
							logger.trace("WORKFLOW BLOCK - Step completed.", "block.blockId", block.info.blockId(),
									"step.class", step.getClass().getName());
						}
					}
					
				}
								
			} catch (AnnotatedException ae) {
				ae.annotate("step.number", stepNum);
				if ((block != null)&&(block instanceof Block))
					throw ae.annotate("block.blockId", ((Block) block).info.blockId());
				throw ae;
			
			} catch (Exception e) {
				AnnotatedException ae = new AnnotatedException("Failed due to spurious exception.", e);
				ae.annotate("step.number", stepNum);	
				if ((block != null)&&(block instanceof Block))
					throw ae.annotate("block.blockId", ((Block) block).info.blockId());
				throw ae;				
			}
			
			blocksToYield++;
			if(blocksToYield == blocksPerYield) {
				blocksToYield = 0;
				WorkKernel.getSystemInterface().yieldToSystem();
			}
		}

	}
	
}
