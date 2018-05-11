package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.model.Document;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.ContextConfiguration;
import dadad.process.WorkflowStep;
import dadad.system.WorkKernel;

public class WFSource extends WorkflowStep<Object, DataContext> {

	// ===============================================================================
	// = FIELDS
	private String uri;

		
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WFSBlockReader.WFSBRConfiguration.class  };
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { WFSBlockReader.WFSBRConfiguration.DOCUMENT_URL};
	}	
	
	
	protected void _start() {
		uri = getConfig().getRequired(WFSBlockReader.WFSBRConfiguration.DOCUMENT_URL);
		getContext().currentDoc = getContext().getDocCatalogStore().create(uri);
	}
	
	protected void _end() { 
		Document document = getContext().currentDoc;
		getContext().getDocCatalogStore().put(document);
		
		WorkKernel.getSystemInterface().getLogger().data("Document completed.",
				"uri", document.uri, "document.id", document.documentId, "number.blocks", document.blocks,
				"run", getContext().getConfig().get(ContextConfiguration.CONTEXT_RUN));
		
		getContext().currentDoc = null;
	}
	
	protected Object _step(Object o) {		
		return null;
	}
	
	protected void _close() {
		getContext().currentDoc = null;
	}
	
	
	// ===============================================================================
	// = METHOD
	
	public void setUrl(final String uri) {
		this.uri = uri;
	}
	
}
