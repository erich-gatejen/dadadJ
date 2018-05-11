package dadad.system;

import java.util.concurrent.atomic.AtomicInteger;

import dadad.data.model.Result;
import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.Yieldable;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;

public class WorkProcess extends Thread implements Yieldable {

	// ===============================================================================
	// = FIELDS
	
	private static final AtomicInteger nextProcessId = new AtomicInteger(1);
	
	private SystemInterface si;
	private Context context;
	private WorkProcessContainer container;
	
	private Logger logger;
	
	// -- stateful data --
	private WorkProcessInfo info;
	private Object stateMonitor;
	
	
	// ===============================================================================
	// = METHODS
	
	public WorkProcessState state() {
		return info.state;
	}
	
	public void pause() {
		synchronized(stateMonitor) {
		
			switch(info.state) {
			case GENESIS:
				throw new Error("BUG BUG BUG!  Cannot attempt to pause a process in GENESIS.  Thar be race conditions here.");
			
			case CONSTRUCTED:
			case RUNNING:
				info.state = WorkProcessState.PAUSE_REQUESTED;
				break;
				
			case PAUSE_REQUESTED:
			case PAUSED:
			case STOPPING:
			case FAILED:
			case DEAD:
				break;
			}
		
		}	
	}
	
	public void unpause() {
		synchronized(stateMonitor) {
		
			switch(info.state) {
			case GENESIS:
			case CONSTRUCTED:
				throw new Error("BUG BUG BUG!  Cannot attempt to unpause a process in GENESIS or CONSTRUCTED.  Thar be race conditions here.");
		
			case RUNNING:
				break;
				
			case PAUSE_REQUESTED:
				info.state = WorkProcessState.RUNNING;
				break;
				
			case PAUSED:
				info.state = WorkProcessState.RUNNING;
				stateMonitor.notifyAll();
				break;
				
			case STOPPING:
			case FAILED:
			case DEAD:
				break;
			}

		}
	}
	
	public void halt() {
		synchronized(stateMonitor) 	{
			
			switch(info.state) {
			case GENESIS:
			case CONSTRUCTED:
			case RUNNING:
			case PAUSE_REQUESTED:	
				break;
				
			case PAUSED:
				stateMonitor.notifyAll();
				
			case STOPPING:
			case FAILED:
			case DEAD:
				break;
			}
			info.state = WorkProcessState.STOPPING;

		}
	}
	
	public Result getResult() {
		return container.getCurrentResult();
	}
	
	public Context getContext() {
		return context;
	}
	
	public SystemInterface getSystemInterface() {
		return si;
	}
	
	public WorkProcessState getWorkProcessState() {
		return info.state;
	}
	
	public WorkProcessInfo getInfo() {
		info.result = container.getCurrentResult();
		if (info.result == null) {
			info.result = Result.inconclusive("Process not fully started.");
		}
		return info;
	}
	
	public WorkProcess(final SystemInterface si, final Context context, final WorkProcessContainer container, final String name) {
		super();
		this.si = si;
		this.context = context;
		this.container = container;
		stateMonitor = new Object();
		
		context.setYieldable(this);
		
		info = new WorkProcessInfo();

		info.id = nextProcessId.getAndIncrement();
		if (name == null) info.name = "WP-" + info.id + '_' + context.getConfig().get(ContextConfiguration.CONTEXT_RUN);
		else info.name = name + "-" + info.id;
		
		setName(info.name);  
			
		// DO NOT CONFIGURE THE CONTAINER HERE!  The kernel thread is running this, not the process thread.  
	}
	
	
	// ===============================================================================
	// = INTERFACE
	
	public void doYield() {
		synchronized(stateMonitor) {
			
			if (info.state == WorkProcessState.PAUSE_REQUESTED) {
				info.state = WorkProcessState.PAUSED;
				try {
					stateMonitor.wait();					
				} catch (InterruptedException ie) {
					// Not sure who did this, so don't change the state.  Just clear and exit.
					Thread.interrupted();				
				}
			}
		
			// See if we are coming into a halt out of a pause.
			if (info.state == WorkProcessState.STOPPING) throw new ForceStopError();
			
		}	
	}
	
	
	// ===============================================================================
	// = INTERNAL
	
	private void configure() {
		// This cannot happen at construction, since the container will need to have access to kernel servcies.
		try {
			logger = si.getLogger();
			container.configure();	
			
		} catch (Throwable t) {
			info.state = WorkProcessState.FAILED;
			throw t;
		}
		info.state = WorkProcessState.CONSTRUCTED;		
	}
		
	public void run() {		
		
		try {
			configure();
		} catch (Throwable t) {
			if (logger == null) throw new Error("System logger could not be built, so exception for dying WorkProcess can not be logged.", t);
			else logger.fault("Work process could not be configured and is dying.", "id", info.id, "exception", AnnotatedException.render(t));
			return;
		}
		
		synchronized(stateMonitor) {
			if (info.state == WorkProcessState.CONSTRUCTED) info.state = WorkProcessState.RUNNING;
		}
		
		try {
			doYield();
			logger.info("Work process started.", "id", info.id);
			
			container.run();
			
			logger.info("Work process ended its job.", "id", info.id);
			
		} catch (ForceStopError fse) {
			// NOP
			logger.info("Work process stopping.", "id", info.id);
		
		} catch (Exception e) {
			logger.fault("Work process stopping due to spurious exception.", "id", info.id, "exception", AnnotatedException.render(e));

		} finally {
			// Time to die.
			info.state = WorkProcessState.DEAD;
			
			try {
				container.close();
			} catch (Exception e) {
				// Doesn't matter, but at least log it.
				logger.error("Exception while closing WorkProcess.", e);
			}
		}
		
		// THIS MUST BE THE ABSOLUTE LAST STEP IN THE THREAD!
		si.reportWorkProcessEnding();				
	}
	
}
