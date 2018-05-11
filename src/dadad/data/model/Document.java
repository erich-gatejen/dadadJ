package dadad.data.model;

import java.io.Serializable;

public class Document implements Serializable {
	
	final static long serialVersionUID = 1;
	
	// ===============================================================================
	// = FIELDS
	//
    //  - doc.id    : KPS : numeric - system unique
    //  - uri       :  pS : uri - globally unique.  there is a one to one relationship with id within a system
    //  - type      : IPS : enumerable type
    //  - TERM*     :  P. : document level terms
    //  - BLOCK*    :  P. : sub blocks

    public final String uri;
	public DataId documentId = DataId.NO_DOCUMENT_ID;
	
	public Term[] terms; 
	public Block[] blocks;
	
	// ===============================================================================
	// = METHODS

	public Document(final String uri) {
		this.uri = uri;
	}

	public Document(final String uri, final long documentId) {
		this.uri = uri;
        this.documentId = new DataId(documentId);
	}

}
