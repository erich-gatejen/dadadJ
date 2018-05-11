package dadad.data.model;

public enum ElementType {
	
	/**
	 * Unknown.
	 */
	UNKNOWN("u", false, false),
	
	/**
	 * Rejected for further processing.
	 */
	REJECTED("r", false, false),
	
	/** 
	 * Unbreakable string.
	 */
	STRING("s", false, true),	
	
	/**
	 * Tokanizable text
	 */
	TEXT("x", false, true),
	
	LONG("l", true, false),
	DOUBLE("d", true, false),
	TIMESTAMP("t", false, false),		// Date and time
	BOOLEAN("b", false, true),
	BREAKING("|", false, false);
	
	private final String mangle;
	private final boolean isNumeric;
	private final boolean isText;
	private ElementType(final String mangle, final boolean isNumeric, final boolean isText) {
		this.mangle = mangle;
		this.isNumeric = isNumeric;
		this.isText = isText;
	}
	
	public String mangle() {
		return mangle;
	}

	public boolean isNumeric() {
		return isNumeric;
	}

	public boolean isText() {
		return isText;
	}
	
	public boolean isAcceptable() {
		if (this == REJECTED) return false;
		else return true;
	}
}
