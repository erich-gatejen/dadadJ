package dadad.platform.services;

import dadad.platform.Context;
import dadad.platform.services.Logger;

public interface PlatformInterface {

	public Logger getReportLogger(final Object tag);
	
	public Logger getLogger();
	
	public Context getContext();
	
}
