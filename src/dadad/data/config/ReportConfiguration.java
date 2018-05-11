package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationPositive;

public enum ReportConfiguration implements ConfigurationType {
	
	SOURCES("report.sources", null, "Document names (uri) for the sources.", new ValidationNOP()),
	REPORTS("report.classes", null, "Report ply.  All numeric subs will be class names for the reports to run.",  new ValidationNOP()),
	RANKS("report.ranks", "100", "Number of ranks.", new ValidationPositive());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private ReportConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
