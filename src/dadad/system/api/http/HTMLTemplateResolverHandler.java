package dadad.system.api.http;

public interface HTMLTemplateResolverHandler {

	public String getScript();
	
	public String getCss();
	
	public String addAction(final String actionName, final String formId, final String apiUrl, final String resultTarget, final String errorPage,
			final String... param);
	
	public String addHelp(final String apiParamName);
	
	public String addApiList();
	
}
