package dadad.system;

public enum WorkProcessState {

	GENESIS(false),
	CONSTRUCTED(false),
	RUNNING(false),
	PAUSE_REQUESTED(false),
	PAUSED(false),
	STOPPING(false),
	
	FAILED(true),			// Could not construct it properly.
	DEAD(true);

	private boolean isTerminal;
	private WorkProcessState(final boolean isTerminal) {
		this.isTerminal = isTerminal;
	}
	
	public boolean isTerminal() {
		return isTerminal;
	}
	
}
