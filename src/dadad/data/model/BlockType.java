package dadad.data.model;

public enum BlockType {

	INTERNAL("Bi"),		// Internal use
	
	LINE("Bl"),
	SCOPE("Bs"),
	
	INTERSPACE("Bn");
	
	private final String mangle;
	private BlockType(String mangle) {
		this.mangle = mangle;
	}
	
	public String mangle() {
		return mangle;
	}
}
