package dadad.data.store.backend;

import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import dadad.data.DataContext;
import dadad.platform.AnnotatedException;

/**
 * A kv store.  There will be one per context.  The index is thread safe, but this 
 * access object is NOT.
 */
public class KVStore {

	// ===============================================================================
	// = FIELDS

	private Logger sysLogger = Logger.getLogger(DataContext.LOGGER__SYSTEM);
	
	public final static String MASTER_MAP = "master";
	
	private DataContext context;	

	private DB db;
	private ConcurrentMap master;
	private int users;
	
	// ===============================================================================
	// = METHODS
	
	public KVStore(final DataContext context) {
		this.context = context;		
	}
	
	public synchronized void openForReadWrite() {
		
		if (db == null) {

			try {
				db = DBMaker.fileDB(new File(context.getKVStoreFile()))
						.fileMmapEnableIfSupported()
				        .make();
				
				master = db.hashMap(MASTER_MAP).createOrOpen();
				
			} catch (Exception e) {
				throwException("Not allowed to open KV store database", e);			
			}
		}
		
		users++;
		sysLogger.fine("KV store opened for write.  user=" + users);
	}
	
	public void put(final String key, Object value) {
		try {
			master.put(key, value);
			
		} catch (Exception e) {
			throwException("Failed to store.", e);	
		}
	}
	
	public Object get(final String key) {
		return master.get(key);
	}
	
	public synchronized void closeForReadWrite() {
		if (users > 0) {
			users--;
			if (users == 0) {
				try {
					db.commit();
					db.close();
					sysLogger.fine("KV store closed for read/write.  user=" + users);
				} catch (Exception e) {
					throwException("Failed to close the kv store database.", e);			
				} finally {
					db = null;
				}
			}
		}
	}
	
	
	// ===============================================================================
	// = INTERNAL
	
	private void throwException(final String message, final Throwable t) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.FAULT, t).annotate("path", context.getKVStoreFile());		
	}
	
}
