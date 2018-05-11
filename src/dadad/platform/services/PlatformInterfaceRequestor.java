package dadad.platform.services;

/**
 * ROAR!  HAXZILLA!
 *
 * I really, really want to keep platform and system separate, particularly if the latter needs to be stubbed.
 * This was easier in D because of linkage.  In java, we need to make sure the implementing class is actually
 * loaded since it is dangerous just to assume reflection will find a class that implements this is already loaded.
 * So, here we go.  Basically, the first to set the pi wins, which will usually be the system.
 */
public class PlatformInterfaceRequestor {

	private static PlatformInterface _pi = null;
	
	public static PlatformInterface getPlatformInterface() {
		return _pi;
	}
	
	public synchronized static void setPlatformInterface(final PlatformInterface platformInterface) {
		if (_pi != null) {
			throw new Error("PlatformInterface already set.");
		}
		_pi = platformInterface;
	}
	
}
