package dadad.data;

public enum DataType {
	
	CSV("csv"),
	JSON("json");
	
	private final String text;
	private DataType(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
