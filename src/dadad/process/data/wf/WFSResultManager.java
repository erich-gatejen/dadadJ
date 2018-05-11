package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.ResultConfiguration;
import dadad.data.model.Block;
import dadad.data.model.Result;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.platform.services.Logger;
import dadad.process.WorkflowStep;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;

public class WFSResultManager extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	private Result masterResult;
	private Logger reportLogger;
	private Logger masterReportLogger;
	private boolean postObjects;
	
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ ResultConfiguration.class };
		
	}
	
	protected ConfigurationType[] _required() {
		return null;
	}	
	
	protected void _start() {
		
		String resultName = getConfig().get(ResultConfiguration.RESULT_NAME);
		if ((resultName == null) || (resultName.trim().length() < 1)) resultName = "RESULT";
			
		SystemInterface si = WorkKernel.getSystemInterface();
		reportLogger = si.getReportLogger(resultName);
		masterReportLogger = si.getReportLogger("TOTAL");

		masterResult = new Result("Total result for " + resultName);
		
		postObjects = getConfig().getBoolean(ResultConfiguration.RESULT_POST_OBJECT);
	}
	
	protected Block _step(Block block) {		

		if (block.result != null) {
			masterResult.merge(block.result);
			
			try {
				if (postObjects) post(reportLogger, block);
					
			} catch (Exception e) {
				throw new AnnotatedException("Failed to post result.", AnnotatedException.Catagory.FAULT, e);
			}
			
		}
		
		return block;
	}

	protected void _end() {
		masterResult.resolve();
		masterReportLogger.data(masterResult.name,
				"type", masterResult.type.name(),
				"metric", masterResult.metric.toString(),
				"reason", masterResult.reason
				);	
		close();
	}
	
	protected void _close() {

	}
	
	// ===============================================================================
	// = METHODS
	
	private static void post(final Logger logger, final Block block) {
		if (block.result.fault == null) {
			logger.data(block.result.name, 
					"source.id", block.info.ownerId(),
					"block.blockId", block.info.blockId(),
					"type", block.result.type.name(),
					"metric", block.result.metric.toString(),
					"reason", block.result.reason
					);
		} else {
			logger.data(block.result.name, 
					"source.id", block.info.ownerId(),
					"block.blockId", block.info.blockId(),
					"type", block.result.type.name(),
					"metric", block.result.metric.toString(),
					"reason", block.result.reason,
					"fault", AnnotatedException.render(block.result.fault));						
		}
	}
	
	
}
