package dadad.system.api.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dadad.platform.Constants;
import dadad.platform.Context;
import dadad.platform.PlatformDataType;
import dadad.platform.PropertyView;
import dadad.platform.config.Configuration;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;
import dadad.system.WorkProcess;
import dadad.system.WorkProcessInfo;
import dadad.system.WorkProcessState;
import dadad.system.api.APIImpl;

/**
 * Server api implementation.
 */
public class ServerAPI_SERVER extends APIImpl {

	// ===============================================================================
	// = FIELDS
	
	public final static long PERSISTENCE_LIMIT_MS = 60 * 60 * 2 * 1000;  // Two hours
	
	
	// ===============================================================================
	// = ABSTRACT
	
	public long persistanceLimit() {
		return PERSISTENCE_LIMIT_MS;
	}
	
	// ===============================================================================
	// = METHOD
	
	public String ping(final PlatformDataType dataType, final String text) {
		return text;
	}
	
	public String start(final PlatformDataType dataType, final String className) {
		SystemInterface si = WorkKernel.getSystemInterface();
		Context subContext = si.getContext().subContext();
		String result = si.startWorkProcess(className, subContext);
		WorkProcess wp = si.getWorkProcess(result);
		
		switch(dataType) {
		case TEXT:
		case DATA:
		case JSON:
			break;
			
		case HTML:
			StringBuilder sb = new StringBuilder();			
			sb.append("<html><body>\r\n");
			sb.append(wpStateLink(result, wp.getInfo().state.name(), dataType));	
			sb.append("</body></html>\r\n");
			result = sb.toString();
			break;
			
		case SNIP:
		case DTMP:
			result = wpStateLink(result, wp.getInfo().state.name(), dataType);
			break;
		}
		
		return result;
	}
	
	public String set(final PlatformDataType dataType, final String name, final String value) {
		SystemInterface si = WorkKernel.getSystemInterface();
		si.getContext().getConfig().set(name, value, true);
		si.getRootContext().getConfig().set(name,  value, true);
		return value;
	}
	
	public String get(final PlatformDataType dataType, final String name) {
		SystemInterface si = WorkKernel.getSystemInterface();
		Configuration config = si.getRootContext().getConfig(); 
		String[] value = config.getMultivalue(name);
		if (value == null) return "";
		return config.encode(value);
	}
	
	public String load(final PlatformDataType dataType, final String path) {
		SystemInterface si = WorkKernel.getSystemInterface();
		Configuration config = si.getContext().getConfig(); 
		
		// Does the full path find the file?  Honestly, this might be a titanic security risk.  
		// TODO: evaluate the security implications.		
		File file = new File(path);
		if (! file.exists()) {
			file = new File(si.getContext().getRootPath(), path);
		}
		
		config.load(file);
		return path;
	}

	public Object status(final PlatformDataType dataType, final String name) {
		WorkProcess wp = WorkKernel.getSystemInterface().getWorkProcess(name);
		StringBuilder sb = new StringBuilder();
		WorkProcessInfo info = wp.getInfo();
				
		switch(dataType) {
		case TEXT:
			sb.append("state").append("=").append(info.state.name()).append("\r\n");
			sb.append("id").append("=").append(info.id).append("\r\n");
			sb.append("name").append("=").append(info.name).append("\r\n");
			sb.append("result").append("=").append(info.result.toString()).append("\r\n");			
			break;
			
		case DATA:
		case JSON:
			return info;
			
		case DTMP:
		case SNIP:
			return getStatusInfoHtml(info);
			
		case HTML:
			sb.append("<html><body>\r\n");
			sb.append(getStatusInfoHtml(info));
			sb.append("</body></html>\r\n");
			break;
		}	
		return sb.toString();
	}
	
	
	private String getStatusInfoHtml(final WorkProcessInfo info) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table><tr><td>").append("State").append("</td><td>").append(info.state.name()).append("</td></tr>\r\n");
		sb.append("<tr><td>").append("id").append("</td><td>").append(info.id).append("</td></tr>\r\n");
		sb.append("<tr><td>").append("name").append("</td><td>").append(info.name).append("</td></tr>\r\n");
		sb.append("<tr><td>").append("result").append("</td><td><pre>").append(info.result.toString()).append("</pre></td></tr></table>\r\n");
		return sb.toString();
	}
	
	public Object properties(final PlatformDataType dataType) {
		Context context = WorkKernel.getSystemInterface().getRootContext();
		PropertyView pv = context.copyProperties();		
		return propertiesRender(dataType, pv);
	}
	
	public Object wpproperties(final PlatformDataType dataType, final String name) {
		WorkProcess wp = WorkKernel.getSystemInterface().getWorkProcess(name);
		PropertyView pv = wp.getContext().copyProperties();		
		return propertiesRender(dataType, pv);
	}
	
	private Object propertiesRender(final PlatformDataType dataType, final PropertyView pv) {
		Object result = null;			
		switch(dataType) {
		case TEXT:
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			pv.save(bw);
			try {
				bw.flush();
			} catch (IOException ioe) {
				// Will never happen.
			}
			result = sw.toString();
			break;
			
		case DATA:
		case JSON:
			result = pv.getAll();
			break;
			
		case HTML:
			StringBuilder sb = new StringBuilder();			
			sb.append("<html><body>\r\n");
			propertiesTable(sb, pv.getAll());	
			sb.append("</body></html>\r\n");
			result = sb.toString();
			break;
			
		case SNIP:
		case DTMP:
			StringBuilder sbs = new StringBuilder();			
			propertiesTable(sbs, pv.getAll());	
			result = sbs.toString();
			break;
		}		
		return result;
	}
	
	private void propertiesTable(final StringBuilder sb, final Map<String, String[]> props) {
		sb.append("<table>\r\n");
		for (String name : props.keySet()) {
			sb.append("<tr><td>").append(name).append("</td><td>").append(PropertyView.encode(props.get(name))).append("</td></tr>\r\n");
		}		
		sb.append("</table>\r\n");
	}
	
	public Object proclist(final PlatformDataType dataType) {
		Collection<WorkProcess> processes = WorkKernel.getSystemInterface().getProcessList().values();
		Object result = null;		
		
		switch(dataType) {
		case TEXT:
			StringBuilder sw = new StringBuilder();
			for(WorkProcess process : processes) {
				sw.append(process.getName()).append("  = ").append(process.getInfo().name);
				sw.append(Constants.NEWLINE);
			}
			result = sw.toString();
			break;
			
		case DATA:
		case JSON:
			Map<String, WorkProcessState> procMap = new HashMap<String, WorkProcessState>();
			for(WorkProcess process : processes) {
				procMap.put(process.getName(), process.getWorkProcessState());
			}
			result = procMap;
			break;
			
		case DTMP:
		case SNIP:	
			result = proclistTable(new StringBuilder(), processes, dataType).toString();
			break;
			
		case HTML:
			StringBuilder sb = new StringBuilder();			
			sb.append("<html><body>\r\n");
			proclistTable(sb, processes, dataType);
			sb.append("</body></html>\r\n");
			result = sb.toString();
			break;
		}
		
		return result;
	}
	
	public String stop(final PlatformDataType dataType, final String text) {
		SystemInterface si = WorkKernel.getSystemInterface();
		si.requestStop();		
		return text;
	}
	
	private StringBuilder proclistTable(final StringBuilder sb, final Collection<WorkProcess> processes, final PlatformDataType type) {
		sb.append("<table>\r\n");
		for(WorkProcess process : processes) {
			sb.append("<tr><td>").append(process.getName()).append("</td><td>")
				.append(wpStateLink(process.getInfo().name, process.getWorkProcessState().name(), type))
				.append("</td></tr>\r\n");
		}
		
		sb.append("</table>\r\n");
		return sb;
	}
	
	private String wpStateLink(final String name, final String state, final PlatformDataType type) {
		
		return "<a href=\"apihelp.dtemp?___api.call=/SERVER/STATE/" + type.name() + "&___result_target=___success.report&api.name=SERVER&name=" + name + "\"> " + state  + "</a>";
	}
	
}
