package dadad.platform;

public interface ResolverHandler {

	public String resolveConfiguration(final String name);
	
	public String resolveVariable(final String name);
	
	public void resolveInclude(final String name);	
	
	    
}

