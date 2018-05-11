package dadad.system.data.boot;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import dadad.data.DataContext;
import dadad.platform.AnnotatedException;
import dadad.platform.PropertyInjector;
import dadad.platform.PropertyStore;
import dadad.platform.PropertyView;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;
import dadad.platform.services.LoggerLevel;
import dadad.platform.services.LoggerTarget;
import dadad.system.WorkKernel;

public class BootKernel {
	
	public final static int ARG0__ROOT_PATH = 0;
	public final static int ARG1__SERVER_CONFIG_FILE = 1;
	
	private final DataContext rootContext;
	@SuppressWarnings("unused")
	private final PropertyView rootProperties;
	@SuppressWarnings("unused")
	private final LoggerTarget consoleLoggerTarget;
	private final Logger bootLogger;

	public BootKernel(final DataContext rootContext, final PropertyView rootProperties, final LoggerTarget consoleLoggerTarget, 
			final Logger bootLogger) {
		this.rootContext = rootContext;
		this.rootProperties = rootProperties;
		this.consoleLoggerTarget = consoleLoggerTarget;
		this.bootLogger = bootLogger;
	}
	
	public int run() {
		
		WorkKernel kernel;
		try {
			bootLogger.debug("Loading kernel.");
			kernel = new WorkKernel(rootContext); 
			bootLogger.debug(".... done");
					
		} catch (Exception e) {
			bootLogger.fault("Failed to build the kernel.", e);
			return 1;
		}
		
		try {
			bootLogger.debug("Starting kernel.");
			kernel.start();
			bootLogger.debug(".... done.  Waiting until it quits.");
			kernel.join();
			bootLogger.info("Kernel has stopped.");
					
		} catch (Exception e) {
			bootLogger.fault("Failed to build the kernel.", e);
			return 1;
		}
		
		return 0;
	}
	
	public static void main(String [] args) {
		
		int resultCode = 0;
		Logger bootLogger = null;
		try {
			if (args.length < 1) {
				throw new Exception("You must specify the install root path");
			}
			if (args.length < 2) {
				throw new Exception("You must give a path to the server configuration properties file.");
			}
			
			LoggerTarget consoleLoggerTarget = new LoggerTarget(new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out))),
					null);
			bootLogger = new Logger(consoleLoggerTarget, "BOOT");
			
			File rootDir = new File(args[ARG0__ROOT_PATH].trim());
			if (! rootDir.isDirectory()) throw new AnnotatedException("Root install directory does not exist").annotate("path", rootDir.getAbsolutePath());
			
			PropertyView properties = new PropertyStore();
			properties.set(ContextConfiguration.CONTEXT_ROOT_PATH.property(), rootDir.getAbsolutePath());
			if (args.length > 1) {
				PropertyInjector.inject(properties, Arrays.copyOfRange(args, 2, args.length));
			}			
			properties.load(new File(args[ARG1__SERVER_CONFIG_FILE].trim()));			
			
			bootLogger.setLevel(LoggerLevel.valueOf(properties.get(ContextConfiguration.CONTEXT_LOGLEVEL.property())));
			
			BootKernel bootloader = new BootKernel(new DataContext(properties), properties, consoleLoggerTarget, bootLogger);			
			resultCode = bootloader.run();
			bootLogger.info("System exiting.");
			
		} catch (Exception e) {
			if (bootLogger != null) bootLogger.fault("Fault trying to start bootstrap.", e);
			else System.out.println(AnnotatedException.render(e, true));
			resultCode = 1;
		}
		
		System.exit(resultCode);
	}
	
}
