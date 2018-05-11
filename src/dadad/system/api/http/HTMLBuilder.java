package dadad.system.api.http;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dadad.platform.AnnotatedException;
import dadad.system.WorkKernel;
import dadad.system.api.API;
import dadad.system.api.APIDispatcher;

public class HTMLBuilder implements HTMLTemplateResolverHandler {
		
	// ===============================================================================
	// = FIELDS
	
	public static final String FUNCTION_PREFIX = "dodadad";
	public static final String FUNCTION_ADDPROP = FUNCTION_PREFIX + "AddProp";
	public static final String FUNCTION_DOSUBMIT = FUNCTION_PREFIX + "DoSubmit";
	
	private final static String initialScriptSection;
	private final static String initialCssSection;
	
	private StringBuilder scriptSection;
	private StringBuilder cssSection;
	private HashSet<String> actionNames;
	
	private  HTMLTemplateResolver templateResolver;
	
	private Map<String, String> parameters;

	// ===============================================================================
	// = METHODS
	
	public HTMLBuilder() {
		templateResolver = new HTMLTemplateResolver(this);
		reset(new HashMap<String, String>());
	}
	
	public String addAction(final String actionName, final String formId, final String apiUrl, final String resultTarget, final String errorPage) {
		return addAction(actionName, formId, apiUrl, resultTarget, errorPage, (String[]) null);
	}
	
	public String addAction(final String actionName, final String formId, final String apiUrl, final String resultTarget, final String errorPage,
			final String... param) {
		StringBuilder sb = new StringBuilder();
		String functionName = FUNCTION_PREFIX + actionName;
		
		try {
		
			if (actionNames.contains(actionName)) throw new AnnotatedException("ActionName already used.");
			actionNames.add(actionName);
			sb.append("function ").append(functionName).append("() {\r\n");
			
			if ((param != null) && (param.length > 1)) {
				if ((param.length % 2) > 0) throw new AnnotatedException("Odd number of parameters");
				for (int index = 0; index < param.length; index += 2) {
					sb.append(" " + FUNCTION_ADDPROP + "('").append(formId).append("','").append(param[index]).append("','")
						.append(param[index + 1]).append("');\r\n");	
				}			
			}

			
			sb.append(" " + FUNCTION_DOSUBMIT + "('").append(formId).append("','").append(apiUrl).append("','").append(resultTarget)
				.append("','").append(errorPage).append("');\r\n}\r\n");
			
			scriptSection.append(sb.toString());
	
		} catch (AnnotatedException ae) {
			throw ae.annotate("actionName", actionName, "formId", formId, "apiUrl", apiUrl, "resultTarget", resultTarget, "errorPage", errorPage);
		}
						
		return "javascript:" + functionName + "()";
	}
	
	public String templateResolve(final String page) {
		return templateResolver.resolve(page);
	}
	
	public void reset(final Map<String, String> parameters) {
		scriptSection = new StringBuilder();
		scriptSection.append(initialScriptSection);
		
		cssSection = new StringBuilder();
		cssSection.append(initialCssSection);
		
		actionNames = new HashSet<String>();
		
		this.parameters = parameters;
	}
	

	
	// ===============================================================================
	// = INTERFACE
	
	public String getScript() {
		return scriptSection.toString() + "</script>\r\n";
	}
	
	public String getCss() {
		return cssSection.toString() + "</style>";		
	}

	public String addHelp(final String apiParamName) {
		StringBuilder sb = new StringBuilder();
		
		String apiName = parameters.get(apiParamName);
		if ((apiName == null)||(apiName.trim().length() < 1)) throw new RuntimeException("The parameter api.name is not set or empty.");
		
		APIDispatcher dispatcher = WorkKernel.getSystemInterface().getAPIDispatcher();
		API[] apis = dispatcher.getAPI(apiName);
		if (apis == null) throw new AnnotatedException("API does not exist.").annotate("api.name", apiName);
		if (apis.length < 1) throw new AnnotatedException("API has no implemented methods.").annotate("api.name", apiName);
		
		sb.append("<form name=\"apihelper\" id=\"apihelper\" enctype=\"application/x-www-form-urlencoded\" method=\"post\" action=\"\">\r\n");
		sb.append("<input name=\"").append(HttpServer.SPECIAL_RESULT_PAGE_PARAM).append("\" type=\"hidden\" value=\"").append("apihelp.dtemp").append("\">\r\n");
		sb.append("<input name=\"dadadurl\" type=\"hidden\" value=\"").append("apihelp.dtemp").append("\">\r\n");
		sb.append("  <div class=\"dadadname\">Name: ").append(apis[0].getAPIName()).append("</div>\r\n");
		sb.append("  <div class=\"dadadhelp\">").append(apis[0].getAPIHelp()).append("</div>\r\n");
		sb.append("  <br>\r\n");
		helpAPIMethodsHtml(apiName, apis, sb);		
		sb.append("</form>\r\n");
			
		return sb.toString();				
	}
	
	public String addApiList() {
		StringBuilder sb = new StringBuilder();
		
		APIDispatcher dispatcher = WorkKernel.getSystemInterface().getAPIDispatcher();
		Set<String> apiNames = dispatcher.getAPINames();
		for (String name : apiNames) {
			sb.append("<div><a href=\"apihelp.dtemp?api.name=").append(name).append("\">").append(name).append("</a></div>\r\n");		
		}
		return sb.toString();
	}
	
	// ===============================================================================
	// = INTERNAL
	
	private void helpAPIMethodsHtml(final String name, final API[] apiMethods, final StringBuilder result) {
		
		for (API apiMethod : apiMethods) {
				if (apiMethod.getAPIHelp() == null) continue;
			
			result.append("<div class=\"dadadname\"> Method: ").append(apiMethod.getMethodName()).append("</div>\r\n");
			result.append("<div class=\"dadadhelp\">").append(apiMethod.getMethodHelp()).append("</div>\r\n");

			result.append("<table width=\"100%\" border=\"1\" class=\"dadadapitable\"><tbody>\r\n");
						
			String[] paramNames = apiMethod.getParameterNames();
			String[] paramHelp = apiMethod.getParameterHelp();
			String[] paramTokens = new String[paramNames.length];
			for (int index = 0; index < paramNames.length; index++) {
				String token = inputToken(name, apiMethod.getMethodName(), index + 1);
				paramTokens[index] = token;
				
				result.append("<tr class=\"dadadapitr\"><td width=\"20\"># ").append(index + 1).append("</td>\r\n");
				result.append("<td><div class=\"dadadname\">Parameter: ").append(paramNames[index]).append("</div>\r\n");
				result.append("<div class=\"dadadhelp\">").append(paramHelp[index]).append("</div>\r\n");
				result.append("<div><input name=\"").append(token).append("\" type=\"text\" id=\"").append(token)
						.append("\" value=\"$((").append(token).append("))\" ");
						
				//String value = parameters.get(token);
				//if (value == null) value = "";
				//result.append(value);
						
				result.append(token).append(" size=\"190\"></div></td></tr>\r\n");

			}
			
			String apiToken = "dadad" + apiMethod.getMethodName() + name;
			result.append("<tr class=\"dadadapitr\"><td width=\"20\">&nbsp;</td><td>");
			result.append("<button class=\"submit.button\" name=\"").append(apiToken).append("\" id=\"").append(apiToken)
					.append("\" onclick=\"$[[ADDACTION,").append(apiToken).append(",apihelper,")
					.append(getApiUrl(apiMethod)).append("?").append(HttpServer.SPECIAL_RESULT_PAGE_PARAM).append("=apihelp.dtemp&api.name=")
					.append(apiMethod.getAPIName()).append(",___success.report,");
			
			for (int index = 0; index < paramNames.length; index++) {
				result.append(",").append(paramNames[index]).append(",").append(paramTokens[index]);				
			}
			result.append("]]\">SUBMIT</button>\r\n");
			
			result.append("<br><span style=\"font-size:0.5em\">").append(getApiUrlText(apiMethod)).append("?");
			if (paramNames.length > 0) {
				result.append(paramNames[0]).append("=").append(paramTokens[0]);
				for (int index = 1; index < paramNames.length; index++) {
					result.append("&").append(paramNames[index]).append("=").append(paramTokens[index]);				
				}
			}
			result.append("</span>\r\n");
						
			result.append("</td></tr></tbody></table><br>\r\n");			
		}		
		
	}
	
	private String inputToken(final String name, final String method, final int id) {
		return "dadad" + name + method + id;  // Eventually I might mangle this.
	}
	
	private String getApiUrl(final API api) {
		return "/" + api.getAPIName().toUpperCase() + "/" + api.getMethodName().toUpperCase() + "/SNIP";
	}
	
	private String getApiUrlText(final API api) {
		return "/" + api.getAPIName().toUpperCase() + "/" + api.getMethodName().toUpperCase() + "/TEXT";
	}
	
	static {	
		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"text/javascript\">\r\n");
		sb.append("function ").append(FUNCTION_DOSUBMIT).append("(formid, api, resulttarget, errorpage) {\r\n");
		sb.append("	var theForm = document.getElementById(formid);\r\n");
		sb.append("	var ainput = document.createElement('input');\r\n");
		sb.append("	ainput.type = 'hidden';\r\n");
		sb.append("	ainput.name = '___api.call';\r\n");
		sb.append("	ainput.value = api;\r\n");
		sb.append("	theForm.appendChild(ainput);\r\n");
		sb.append("	var tinput = document.createElement('input');\r\n");
		sb.append("	tinput.type = 'hidden';\r\n");
		sb.append("	tinput.name = '___result_target';\r\n");
		sb.append("	tinput.value = resulttarget;\r\n");
		sb.append("	theForm.appendChild(tinput);\r\n");
		sb.append("	var einput = document.createElement('input');\r\n");
		sb.append("	einput.type = 'hidden';\r\n");
		sb.append("	einput.name = '___error.page';\r\n");
		sb.append("	einput.value = errorpage;\r\n");
		sb.append("	theForm.appendChild(einput);\r\n");
		sb.append("	theForm.submit();\r\n");
		sb.append("}\r\n");
		sb.append("function ").append(FUNCTION_ADDPROP).append("(formid, name, value) {\r\n");
		sb.append("	var theForm = document.getElementById(formid);\r\n");
		sb.append("	var evaluefield = document.getElementById(value).value;\r\n");
		sb.append("	var einput = document.createElement('input');\r\n");
		sb.append("	einput.type = 'hidden';\r\n");
		sb.append("	einput.name = name;\r\n");
		sb.append("	einput.value = evaluefield;\r\n");
		sb.append("	theForm.appendChild(einput);\r\n");
		sb.append("}\r\n");
		initialScriptSection = sb.toString();
				
		sb = new StringBuilder();
		sb.append("<style type=\"text/css\">\r\n");
		sb.append(".submit.button {\r\n");
		sb.append("	font-weight: normal;\r\n");
		sb.append("	font-style: normal;\r\n");
		sb.append("	font-size: medium;\r\n");
		sb.append("}\r\n");
		initialCssSection = sb.toString();
	}

}

