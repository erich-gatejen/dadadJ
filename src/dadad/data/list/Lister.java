package dadad.data.list;

import dadad.platform.config.Configuration;

public interface Lister {

	public void configure(final Configuration configuration);
	
	/**
	 * Get the version for this lister.  
	 * @return
	 */
	public String version();

}
