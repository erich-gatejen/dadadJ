package dadad.system.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dadad.data.DataContext;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Block;
import dadad.data.model.Result;
import dadad.platform.AnnotatedException;
import dadad.platform.Constants;
import dadad.platform.ContextRunnable;
import dadad.platform.Resolver;
import dadad.platform.config.Configurable;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.ContextConfiguration;
import dadad.process.WorkflowEngine;
import dadad.process.WorkflowEngineBlock;
import dadad.process.WorkflowStep;
import dadad.process.WorkflowStepsBuilder;
import dadad.process.data.wf.WFSBlockReader;
import dadad.process.data.wf.WFSTest;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;
import dadad.system.WorkProcessContainer;

/**
 * Configurable workflow.
 */
public class DataWorkflow implements WorkProcessContainer, Configurable {	
	
	// ===============================================================================
	// = FIELDS
	
	public final static char COMMENT_START = '#';
	
	protected DataContext context;
	private WorkflowEngine<Block> engine;
	
	private Resolver resolver;
	
	// Per configuration.
	private HashMap<String, WorkflowStep<?, ?>> stepCatalog;
	private HashMap<String, WorkflowStep<?, ?>> processorCatalog;
	
	private HashSet<String> mayBeUndefined;
	
	private Result currentResult;
	
	// ===============================================================================
	// = METHODS
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class };
		
	}
	
	public DataWorkflow() {
		currentResult = Result.newResult("Data Workflow genesis.");
	}
	
	// This might matter later.
	private enum Type {
		OBJECT,
		BLOCK;
		
		public static Type match(final String name) {
			try {
				return Type.valueOf(name.trim().toUpperCase());
			} catch (Exception e) {
				//
			}
			return null;
		}
	}
	
	// ===============================================================================
	// = INTERFACE - WorkProcessContainer - used by the server kernel
	
	public void configure() {
		
		SystemInterface si  = WorkKernel.getSystemInterface();
		try {
			context = (DataContext) si.getContext();
		} catch (ClassCastException cce) {
			throw new AnnotatedException("Was expecting a DataContext, but did not get it.", cce);
		}
		
		currentResult = Result.inconclusive("Data Workflow: " + si.getContext().getConfig().get(ContextConfiguration.CONTEXT_RUN));
		
		resolver = context.getResolver();
		try {
			configureScript(context.getConfig().getRequired(WorkflowConfiguration.ORDER_SCRIPT));
			
		} catch (Throwable t) {
			currentResult = Result.fault(currentResult.name, t);			
			throw t;
		}
	}
	
	public Result getCurrentResult() {
		return currentResult;
	}
	
	public Result run() {

		try {
			process(context.getConfig().getRequired(ContextConfiguration.CONTEXT_SOURCE));
			close();
			currentResult = Result.pass(currentResult.name);
		
		} catch (Throwable t) {
			currentResult = Result.fault(currentResult.name, t);
			throw t;
		}
		
		return currentResult;
	}
	

	// ===============================================================================
	// = ABSTRACT - Workflow - accessible outside of server context
	
	/**
	 * You can configure as stand-alone or as part of the WorkProcess system.
	 * @param context
	 * @param script
	 */
	public void configure(final DataContext context, final String scriptPath) {
		this.context = context;
		configureScript(scriptPath);
	}
	
	public void process(final String url) {
		
		if (engine == null) throw new Error("You must call configure() before process().");
		if (url == null) throw new Error("BUT! BUG! BUG!  This should not happen.  Url must be required.");
		
		context.getConfig().set(WFSBlockReader.WFSBRConfiguration.DOCUMENT_URL, url);
				
		try {
			engine.process();
				
		} catch (Exception e) {
			throw new AnnotatedException("Workflow failed.", AnnotatedException.Catagory.FAULT, e)
					.annotate("source.url", url);			
		}
		
	}
	
	public void close() {
		if (engine == null) throw new Error("You must call configure() before close().");
		engine.close();
	}
	
	// ===============================================================================
	// = METHODS
		
	@SuppressWarnings("resource")  // For some dumb reason eclipse thinks the inner try will keep the outer finally from executing.
	public void configureScript(final String scriptFilePath) {
		
		WorkflowStepsBuilder startStepsBuilder = new WorkflowStepsBuilder();
		WorkflowStepsBuilder headerStepsBuilder = new WorkflowStepsBuilder();
		WorkflowStepsBuilder processStepsBuilder = new WorkflowStepsBuilder();
		WorkflowStepsBuilder endStepsBuilder = new WorkflowStepsBuilder();
		stepCatalog = new HashMap<String, WorkflowStep<?, ?>>();
		processorCatalog = new HashMap<String, WorkflowStep<?, ?>>();
		mayBeUndefined = new HashSet<String>();
		
		HashMap<String, String> failForwardMap = new HashMap<String, String>();
		String defaultFailForward = null;
		
		// TODO see other security issues
		File scriptFile = new File(context.getRootPath(), scriptFilePath);
		if (! scriptFile.exists()) scriptFile = new File(scriptFilePath);	
		if (! scriptFile.canRead()) throw new AnnotatedException("Cannot read script file.")
			.annotate("script.file.path", scriptFile.getAbsolutePath());
		
		boolean headerPresent = context.getConfig().getBoolean(WorkflowConfiguration.HEADER_IS_PRESENT);
		
		int lineNumber = 1;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(scriptFile));
			String line = reader.readLine();
			while (line != null) {
				
				line = line.trim();
				String[] tokens = compact(line.split("\\s+"));
				if ((tokens.length > 0) && (tokens[0].length() > 0) && (tokens[0].charAt(0) != COMMENT_START)){
				
					DataOrder order;
					try {
						order = DataOrder.valueOf(tokens[0]);
					} catch (Exception e) { 
						throw new AnnotatedException("Unknown order.").annotate("order.name", tokens[0]);
					}
					order.validate(tokens);
					
					switch(order) {
					
					case STEP:
						Type type = getType(tokens);
						String reservedName = reserveName(tokens);
						stepCatalog.put(reservedName, getStep(type, reservedName, tokens[2]));
						break;
						
					case TEST:
						String reservedTestName = reserveName(tokens);
						stepCatalog.put(reservedTestName, getTest(reservedTestName, tokens[2]));
						break;
						
					case PROCESSOR:
						processorCatalog.put(getName(tokens), Workflow.getElementProcessorStep(context));
						break;
						
					case TERMPROCESSOR:
						WorkflowStep<?, ?> termProcessor = Workflow.getTermProcessorStep(context);
						if (termProcessor == null) mayBeUndefined.add(getName(tokens));
						else processorCatalog.put(getName(tokens), termProcessor);
						break;
						
					case START:
						addToBuilder(startStepsBuilder, getStepFromCatalog(getName(tokens)));
						break;
						
					case HEADER:
						if (headerPresent) addToBuilder(headerStepsBuilder, getStepFromCatalog(getName(tokens)));
						break;
						
					case PROCESS:
						String pname = getName(tokens);
						WorkflowStep<?,?> pstep = getStepFromCatalog(pname);
						
						if (pstep != null) {
							if (tokens.length > 2) {
								String ptoken = tokens[2].trim();
								if (ptoken.length() > 0) {
									failForwardMap.put(pname, getFailForwardName(tokens));
								}
								
							} else if (defaultFailForward != null) {
								failForwardMap.put(pname, defaultFailForward);
							}
							addToBuilder(pname, processStepsBuilder, pstep);
						}
						break;
						
					case END:	
						addToBuilder(endStepsBuilder, getStepFromCatalog(getName(tokens)));
						break;
					
					case TEMP:
						File tempFile = new File(context.getTempDirFile().getAbsolutePath() + tokens[2]);
						context.getConfig().set(tokens[1], tempFile.getAbsolutePath());
						break;
						
					case SHARE:
						String extension = resolver.resolve(tokens[2]);
						File shareFile = new File(context.getShareDirTempFile().getAbsolutePath() + extension);
						context.getConfig().set(tokens[1], shareFile.getAbsolutePath());
						break;
						
					case COPY:
						context.getConfig().set(tokens[2], context.getConfig().getMultivalue(tokens[1]));
						break;
						
					case FORWARD:
						defaultFailForward = tokens[1];
						break;
						
					} // end order case
				
				} // end if line
				
				line = reader.readLine();
				lineNumber++;
			}			
		} catch (IOException ioe) {
			// Never going to happen to a string reader.
			
		} catch (AnnotatedException e) {
			throw e.annotate("line.number", Integer.toString(lineNumber));
			
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (Exception eee) {
				// Don't care
			}
		}
		
		// Fix fail forwards
		int[] failForwardTargets = null;

		if (failForwardMap.size() > 0) {
			int[] failForwardTargetsWorking = initFailFowardTargets(processStepsBuilder.size());
			
			for (String processStepName : failForwardMap.keySet()) {	
				int stepNumber = processStepsBuilder.getPositionByName(processStepName);
				int targetNumber = processStepsBuilder.getPositionByName(failForwardMap.get(processStepName));
				failForwardTargetsWorking[stepNumber] = targetNumber;			
			}
			
			failForwardTargets = failForwardTargetsWorking;
		}
				
		engine = new WorkflowEngineBlock(context.getConfig(), startStepsBuilder.build(), headerStepsBuilder.build(), processStepsBuilder.build(),
				endStepsBuilder.build(), failForwardTargets);
	}

	
	// ===============================================================================
	// = INTERNAL	
	
	private int[] initFailFowardTargets(final int size) {
		int[] result = new int[size];
		for (int index = 0; index < size; index++) {
			result[index] = Constants.NO_FOWARD;
		}
		return result;
	}
	
	private void addToBuilder(final WorkflowStepsBuilder builder, final WorkflowStep<?, ?> step) {
		if (step != null) builder.add(step);
	}
	
	private void addToBuilder(final String name, final WorkflowStepsBuilder builder, final WorkflowStep<?, ?> step) {
		if (step != null) builder.add(step, name);
	}
	
	private String reserveName(final String[] tokens) {
		String name = getName(tokens);
		if (stepCatalog.containsKey(name)) throw new AnnotatedException("Name already used").annotate("name", name);
		return name;
	}
	
	private String getName(final String[] tokens) {
		return tokens[1].trim().toLowerCase();
	}
	
	private String getFailForwardName(final String[] tokens) {
		return tokens[2].trim().toLowerCase();
	}
	
	private Type getType(final String[] tokens) {
		String name = tokens[3].trim();
		Type type = Type.match(name);
		if (type == null) throw new AnnotatedException("Unknown type.").annotate("type.name", tokens[3]);
		return type;
	}
	
	private WorkflowStep<?, ?> getStep(final Type type, final String name, final String className) {
		try {
			Class<?> clazz = Class.forName(className);
			switch(type) {
			case OBJECT:
				@SuppressWarnings("unchecked")
				WorkflowStep<Object, DataContext> ostep = (WorkflowStep<Object, DataContext>) clazz.newInstance();
				return (WorkflowStep<?, ?>) ostep.set(context);
				
			case BLOCK:
				@SuppressWarnings("unchecked")
				WorkflowStep<Block, DataContext> bstep = (WorkflowStep<Block, DataContext>) clazz.newInstance();
				return (WorkflowStep<?, ?>) bstep.set(context);
				
			default:
				throw new Error("BUG BUG BUG!!!  Unhandled Type.  name=" + type.name());
			}
			
		} catch  (ClassCastException cce) {
			throw new AnnotatedException("Could not load step because it isn't a WorkflowStep class.", cce)
			.annotate("name", name, "class.name", className);
			
		} catch (Exception e) {
			throw new AnnotatedException("Could not load step.", e)
				.annotate("name", name, "class.name", className);
		}
	}
	
	private WorkflowStep<?, ?> getTest(final String name, final String className) {
		try {
			Class<?> clazz = Class.forName(className);
			Object test = clazz.newInstance();
			WorkflowStep<Block, DataContext> ostep = new WFSTest((ContextRunnable) test, name);
			return (WorkflowStep<?, ?>) ostep.set(context);
			
		} catch  (ClassCastException cce) {
			throw new AnnotatedException("Could not load test because it isn't a ContextRunnable class.", cce)
			.annotate("name", name, "class.name", className);
			
		} catch (Exception e) {
			throw new AnnotatedException("Could not load test.", e)
				.annotate("name", name, "class.name", className);
		}
	}
	
	
	private WorkflowStep<?, ?> getStepFromCatalog(final String name) {
		WorkflowStep<?, ?> result = stepCatalog.get(name);
		if (result == null) {
			result = processorCatalog.get(name);
			if ((result == null) && (! processorCatalog.containsKey(name)) && (! mayBeUndefined.contains(name))) {
				throw new AnnotatedException("Step/Processor not defined.").annotate("name", name);
			}	
		}
		return result;
	}
	
	private String[] compact(final String[] tokens) {
		ArrayList<String> tokenList = new ArrayList<String>();
		for (String token : tokens) {
			String candidate = token.trim();
			if (candidate.length() > 0) tokenList.add(candidate);
		}
		return tokenList.toArray(new String[tokenList.size()]);
	}
	
}
