package dadad.system.api.http;

import java.util.Map;

import dadad.platform.Context;
import dadad.platform.PropertyView;
import dadad.platform.ResolverHandler;
import dadad.platform.config.Configuration;

/**
 * Resolve handler.
 */
public class HttpResolveHandler implements ResolverHandler {
		
	private final Configuration config;
	private final Map<String, String> nv;
	
	public HttpResolveHandler(final Context context, final Map<String, String> nv) {
		config = context.getConfig();
		this.nv = nv;
	}
	
	public String resolveConfiguration(final String name) {
		String[] value = config.getMultivalue(name);
		if (value == null) return "";
		if (value.length == 1) return value[0];
		if (value.length > 1) return PropertyView.encode(value);
		return "";
	}
	
	public String resolveVariable(final String name) {
		String value = nv.get(name);
		if (value == null) return "";
		return value;
	}
	
	public void resolveInclude(final String name) {
		// NOP for now
	}
}

