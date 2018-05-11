package dadad.data.store.backend;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import dadad.data.DataContext;
import dadad.platform.AnnotatedException;
 
/**
 * A token index.  There will be one per context.  The index is thread safe, but this
 * access object is NOT.
 */
public class TokenIndex {

	// ===============================================================================
	// = FIELDS
	
	private Logger sysLogger = Logger.getLogger(DataContext.LOGGER__SYSTEM);
	
	private DataContext context;

	private IndexWriter indexWriter;
	private int users;
	
	public static final CharArraySet STOP_WORDS_SET;
	static {
	    final List<String> stopWords = Arrays.asList(
	      "biggleboggleboogleboogermonsterstopword"			// 
	    );
	    final CharArraySet stopSet = new CharArraySet(stopWords, false);
	    STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet); 
	}

	
	// ===============================================================================
	// = METHODS
	
	public TokenIndex(final DataContext context) {
		this.context = context;		
	}
	
	// ===============================================================================
	// = INDEX WRITING
	
	public synchronized void openForWrite() {
		
		if (indexWriter == null) {
			File indexPath = new File(context.getTokenIndexSubdir());
			if (! indexPath.isDirectory()) {
				try {
					if (! indexPath.mkdirs() )
						throwException("Could not create token index directory");
				} catch (SecurityException se) {
					throwException("Not allowed to create token index directory", se);
				}
			}
			
			Analyzer analyzer = new StandardAnalyzer(STOP_WORDS_SET);
			IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
			try {
				indexWriter = new IndexWriter( FSDirectory.open(indexPath.toPath()), indexConfig);
			} catch (Exception e) {
				throwException("Not allowed to create token index directory", e);
			}
		}
		
		users++;
		sysLogger.fine("Token index opened for write.  user=" + users);
	}
	
	public void submit(Document doc) {
		try {
			indexWriter.addDocument(doc);
			
		} catch (Exception e) {
			throwException("Failed to index.", e);	
		}
	}
	
	public synchronized void closeForWrite() {
		if (users > 0) {
			users--;
			if (users == 0) {
				try {
					indexWriter.commit();
					indexWriter.close();
				} catch (Exception e) {
					throwException("Failed to close the kv store database.", e);			
				} finally {
					indexWriter = null;
				}
			}
		}
		
		sysLogger.fine("Token index closed for write.  user=" + users);
	}
	
	// ===============================================================================
	// = INDEX READING
	
	public IndexSearcher getSearcher() {

		File indexPath = new File(context.getTokenIndexSubdir());
		if (! indexPath.isDirectory()) {
			throwException("Index does not exist.");						
		}
		
		IndexSearcher searcher = null;
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexPath.toPath()));
			searcher = new IndexSearcher(indexReader);
			sysLogger.fine("Token store searcher obtained.");
			
		} catch (Exception e) {
			throwException("Could not open index.", e);
		}
		
		return searcher;
	}
	 

	// ===============================================================================
	// = INTERNAL
	
	private void throwException(final String message) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.FAULT).annotate("path", context.getTokenIndexSubdir());
	}
	private void throwException(final String message, final Throwable t) {
		throw new AnnotatedException(message, AnnotatedException.Catagory.FAULT, t).annotate("path", context.getTokenIndexSubdir());		
	}
	
}
