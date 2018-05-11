package dadad.process.data.wf;

import static dadad.platform.AnnotatedException.Catagory.FAULT;

import java.util.HashMap;

import dadad.data.DataContext;
import dadad.data.DataType;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.process.WorkflowStep;

/**
 * The various processing workflow steps.
 */
public enum Processor {

	// Element processors
	CSV(WFSCSVProcessor.class),
	JSON(WFSJSONProcessor.class),
	PROGRAMMED(WFSProgrammedProcessor.class),
	
	// Term processors
	JSON_TERM(WFSJSONTermProcessor.class);
	
	private final Class<?> processorClass;
	private Processor(final Class<?> processorClass) {
		this.processorClass = processorClass;
	}
	
	// ===============================================================================
	// = METHODS
	
	@SuppressWarnings("unchecked")
	public WorkflowStep<Block, DataContext> getProcessor(final DataContext context) {
		WorkflowStep<Block, DataContext> result;
		try {
			result = (WorkflowStep<Block, DataContext>) processorClass.newInstance();
			result.set(context);
			
		} catch (Exception e) {
			throw new Error("BUG BUG BUG!  Something has referenced a workflow step class incorrectly.", e);
		}
		
		return result;
	}
	
	/**
	 * Get the workflow step for the named processor.
	 * @param context
	 * @param name
	 * @param throwIfFail if true, it throw an exception if the processor cannot be determined.  Otherwise, it would return null.
	 * @return
	 */
	public static WorkflowStep<Block, DataContext> getProcessor(final DataContext context, final String name,
			final boolean throwIfFail) {
		if (name == null) {
			if (throwIfFail) throw new Error("BUG BUG BUG!  Name is a null String.");
			return null;
		}
		
		Processor processor = null;
		try {
			processor = Processor.valueOf(name.trim().toUpperCase());
			
		} catch(Exception e) {
			if (throwIfFail) throw new AnnotatedException("Cannot determine the processor.", FAULT, e).annotate("processor.name", name);
			return null;
		}
		
		return processor.getProcessor(context);
	}
	
	/**
	 * Get the workflow step for given data type.
	 * @param context
	 * @param name
	 * @param throwIfFail if true, it throw an exception if the processor cannot be determined.  Otherwise, it would return null.
	 * @return
	 */
	public static WorkflowStep<Block, DataContext> getProcessor(final DataContext context, final DataType type,
			final boolean throwIfFail) {
		Processor processor = null;
		processor = lookUpDataType(type);
		if (processor == null) {
			if (throwIfFail) throw new AnnotatedException("Cannot determine the processor.", FAULT).annotate("processor.name", type.name());
			return null;
		}
		return processor.getProcessor(context);
	}
	
	private static HashMap<DataType, Processor> processorCatalog;
	
	public static synchronized Processor lookUpDataType(final DataType type) {
		if (processorCatalog == null) populateCatalog();
		return processorCatalog.get(type);	
	}
	
	// ===============================================================================
	// = INTERNAL
	
	private static void populateCatalog() {
		processorCatalog = new HashMap<DataType, Processor>();
		processorCatalog.put(DataType.CSV, PROGRAMMED);
	}
}
