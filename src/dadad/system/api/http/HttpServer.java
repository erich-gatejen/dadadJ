package dadad.system.api.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.google.gson.Gson;

import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.PlatformDataType;
import dadad.platform.LimitedReader;
import dadad.platform.PuntException;
import dadad.platform.Resolver;
import dadad.platform.services.Logger;
import dadad.system.WorkKernel;
import dadad.system.api.APIDispatcher;
import dadad.system.api.APIRequest;
import dadad.system.api.APIResponse;
import dadad.system.api.APIResponseCodes;

/**
 * Http server.
 */
public class HttpServer {

	// ===============================================================================
	// = FIELDS
	
	public final static String DATA_TYPE_PARAMETER_NAME = "datatype";
	
	public final static String SPECIAL_API_CALL = "___api.call";
	public final static String SPECIAL_RESULT_PAGE_PARAM = "___result.page";
	public final static String SPECIAL_RESULT_TARGET_PARAM = "___result_target";
	public final static String SPECIAL_ERROR_PAGE_PARAM = "___error.page";
	public final static String SPECIAL_ERROR_REPORT_PARAM = "___error.report";
	
	private final static int RUNAWAY_TRIES_THRESHOLD = 10;
	
    private ServerSocket serverSocket;
    private HttpHandler httpHandler;
    private Logger logger;
    private Logger sysLogger;
    
    private Context context;
    
    private HttpFormURLProcessor formURLprocessor;
    
    private APIDispatcher dispatcher;
    
    private final static int INITIAL_BUFFER_SIZE = 1024 * 4;
    
	// ===============================================================================
	// = METHODS
    
    public void setup(final int listenPort, final int listenTimeout) {
    	try {
    		context = WorkKernel.getSystemInterface().getContext();
    		httpHandler = new HttpHandler(context);
    		serverSocket = new ServerSocket(listenPort);
    		serverSocket.setSoTimeout(listenTimeout);
    		
    	} catch (Exception e) {
    		throw new AnnotatedException("Could not start HttpServer").annotate("port", listenPort);
    	}
    	
    	logger = WorkKernel.getSystemInterface().getReportLogger("HTTP");
    	sysLogger = WorkKernel.getSystemInterface().getLogger();
    	sysLogger.info("HTTPServer started.", "port", listenPort);
    	
    	formURLprocessor = new HttpFormURLProcessor();
    	
    	dispatcher = WorkKernel.getSystemInterface().getAPIDispatcher();
    }

    public void service(final int socketBlockLimit) {
      
		try {
		    Socket socket = serverSocket.accept();
		    socket.setSoTimeout(socketBlockLimit);
		    InputStream inputStream = socket.getInputStream();
		    OutputStream outputStream = socket.getOutputStream();
		    try {
		        process(inputStream, outputStream);
		
		    } catch (Exception ee) {
		       throw new AnnotatedException("Server dying due to spurious exception.", ee);
		
		    } finally {
		        try {
		            inputStream.close();
		        } catch (Exception eee) {
		        }
		        try {
		            outputStream.close();
		        } catch (Exception eee) {
		        }
		        try {
		            socket.close();
		        } catch (Exception eee) {
		        }
		    }
		    
		} catch (SocketTimeoutException ste) {
			// Noop.  We net this happen.
		    
		} catch (AnnotatedException ae) {
			sysLogger.fault("Server dying due to spurious exception.", ae);
		
		} catch (Exception e) {
			sysLogger.fault("Server dying due to io exception.", e);
		}

    }
    
    public void close() {
    	try {
    		serverSocket.close();
    	} catch (Exception e) {
    		// Don't care
    	}
    }

    enum HttpMethod {
    	GET, 
    	POST;
    }
    
    class Request {
    	String requestLine;
    	HttpMethod method;
    	String uri;
    	String path;
    	Map<String, String> nv;  	
    }  
    
    private void process(final InputStream inputStream, final OutputStream outputStream) {

        Map<String, String> nv = new HashMap<String, String>();
        String path= "";
        try {

            BufferedReader brin = new BufferedReader(new InputStreamReader(inputStream));
            Request request = getRequest(brin);

            Object response = null;
            HttpRedirectException httpRedirectException = null;
            int tries = 0;
            do {
            	
            	if (tries == RUNAWAY_TRIES_THRESHOLD) throw new AnnotatedException("BUG BUG BUG!!!  Runaway http redirects.")
            		.annotate("request.line", request.requestLine);
            	
            	try {
            		response = getResponse(request);
            		httpRedirectException = null;
            		
            	} catch (HttpRedirectException e) {
            		Request newRequest = new Request();
            		newRequest.uri = e.url;
            		newRequest.nv = request.nv;
            		newRequest.method = request.method;
            		newRequest.path = cutPathAndDecode(newRequest.uri, newRequest.nv);
                    request = newRequest;
                    
                    // You may redirect to page only, not an API
                    newRequest.nv.remove(SPECIAL_API_CALL);
 
                    httpRedirectException = e;
            	}
            	tries++;
            	           	
            } while (httpRedirectException != null);
            
            path = request.path;

            if (response instanceof APIResponse) {
            	sendApi(outputStream, (APIResponse) response, nv.get(DATA_TYPE_PARAMETER_NAME), path);
            
            } else {
            	sendHttp(outputStream, (HttpResponse) response, path);
            	
            }
                     
        } catch (PuntException re) {
        	sendApi(outputStream, new APIResponse(APIResponseCodes.FAULT, PlatformDataType.TEXT, re.getMessage()), nv.get(DATA_TYPE_PARAMETER_NAME), path );
            
        } catch (Exception e) {
        	sendApi(outputStream, new APIResponse(APIResponseCodes.FAULT, PlatformDataType.TEXT, "Failed due to exception.", e), nv.get(DATA_TYPE_PARAMETER_NAME), path);
        }
       
    }
    
    private Request getRequest(final BufferedReader brin) throws Exception {
    	
    	Request request = new Request();
    	request.nv = new HashMap<String, String>();
    	
    	request.requestLine = brin.readLine();

        StringTokenizer st = new StringTokenizer(request.requestLine);
        if (!st.hasMoreTokens())
            throw new PuntException("Malformed request:" + request.requestLine);
        request.method = getMethod(st.nextToken());    // method

        if (!st.hasMoreTokens())
            throw new PuntException("Malformed request:" + request.requestLine);
        request.uri = st.nextToken();         

        // Decode parameters from the URI
        request.path = cutPathAndDecode(request.uri, request.nv);
    
        if (request.method == HttpMethod.POST) {
        	getBodyParams(request.nv, brin);
        }
        
        return request;
    }
    
    private Object getResponse(final Request httpRequest) throws Exception {
    	Object response = null;
        APIRequest request = dispatcher.parse(httpRequest.path);
        
        if (request == null) {
        
        	// Page request
	        String apiTarget = httpRequest.nv.get(SPECIAL_API_CALL);  
	        if ((request == null) && (apiTarget != null)) {      	
	        	request = dispatcher.parse(cutPathAndDecode(apiTarget, httpRequest.nv));
	        }

	        if (request == null) {
	        	response = httpHandler.handler(httpRequest.path, httpRequest.nv);
	                   	
	        } else {            	
	        		
	        	APIResponse apiResponse = (APIResponse) dispatcher.dispatcher(request, httpRequest.nv);	 
	
	        	String resultTarget = httpRequest.nv.get(SPECIAL_RESULT_TARGET_PARAM);
				if ((resultTarget != null) && (resultTarget.length() > 0)) {
					if (  (apiResponse.dataType != request.dataType) &&
						
						  ( ((request.dataType == PlatformDataType.DTMP) || (request.dataType == PlatformDataType.HTML) || (request.dataType == PlatformDataType.SNIP)) && 
							 (apiResponse.dataType == PlatformDataType.TEXT) ) 
						) {
						httpRequest.nv.put(resultTarget, "<pre>" +  apiResponse.response.toString() + "</pre>");
						
					} else {
						httpRequest.nv.put(resultTarget, apiResponse.response.toString());
					}
				}
				
	        	if (apiResponse.code.isSuccessful()) {
	    			String resultPage = httpRequest.nv.get(SPECIAL_RESULT_PAGE_PARAM);
	    			if ((resultPage != null)&&(resultPage.length() > 0)) {
	    				response = httpHandler.handler(resultPage, httpRequest.nv);
	    			} else {
	    				response = httpHandler.handler(httpRequest.path, httpRequest.nv);
	    			}
	    			        			
	    		} else {
	    			// Rip any redirect exception
	    			Throwable e = apiResponse.exception;
	    			if ((e != null) && (e instanceof AnnotatedException)) {
	    				Throwable cause = ((AnnotatedException) e).getCause();
	    				if (cause instanceof HttpRedirectException) throw (HttpRedirectException) cause;	    				
	    			}
	    			
	    			String errorPage = httpRequest.nv.get(SPECIAL_ERROR_PAGE_PARAM);
	    			if ((errorPage != null)&&(errorPage.length() > 0)) {
	    				response = httpHandler.handler(httpRequest.path, httpRequest.nv, apiResponse.response.toString(), apiResponse.exception, errorPage, logger.isDebugging());
	    			} else {
	    				response = new APIResponse(APIResponseCodes.FAULT, PlatformDataType.HTML, 
	    						"HTTP request failed.\r\n\r\n<br>\r\n" + apiResponse.response.toString(), apiResponse.exception); 
	    			}       			
	    		}
	        	        	
	        }
        
        } else {
        	// Pure API
        	APIResponse apiResponse = (APIResponse) dispatcher.dispatcher(request, httpRequest.nv);	 
        	if (apiResponse.code.isSuccessful()) {
        		response = new APIResponse(APIResponseCodes.SUCCESS, request.dataType, apiResponse.response);
 			
    		} else {
    			response = new APIResponse(APIResponseCodes.FAULT, PlatformDataType.HTML, 
    						"HTTP REST call failed.\r\n\r\n<br>\r\n" + apiResponse.response.toString(), apiResponse.exception);   			
    		}
        	
        }
    	
        return response;
    }
    
    private HttpMethod getMethod(final String method) {
    	HttpMethod result;
    	try {
    		result = HttpMethod.valueOf(method);
    	} catch (Exception e) {
    		throw new AnnotatedException("Http method not supported.").annotate("method", method);
    	}
    	return result;
    }

    private void decodeNV(String text, Map<String, String> nv) throws Exception {
        StringTokenizer tokanizer = new StringTokenizer(text, "&");

        while (tokanizer.hasMoreTokens()) {
            String token = tokanizer.nextToken();

            int split = token.indexOf('=');
            if (split >= 0) {
                nv.put(URLDecoder.decode(token.substring(0, split), "UTF-8").trim(),
                        URLDecoder.decode(token.substring(split + 1), "UTF-8"));
            } else {
                nv.put(URLDecoder.decode(token, "UTF-8").trim(), "");
            }
        }
    }

	private void sendApi(final OutputStream outputStream, APIResponse response, final String dataTypeString, final String path) {
		
		String statusText = getStatusText(response.code);
		
	    byte[] data;
	    try {
	    	data = getData(response);	    	
	    } catch (Exception e) {
	    	response = new APIResponse(APIResponseCodes.FAULT, response.dataType, e.getMessage(), e.getCause());
	    	data = getData(response);
	    }

	    String header = createHeader(response.code, statusText, response.dataType.getContentType(), data.length);

	    try {
		    outputStream.write(header.toString().getBytes("ISO-8859-1"));
		    outputStream.write(data);
		    outputStream.flush();
	    } catch (Exception e) {
	    	throw new AnnotatedException("IO failure while responding to request.", e);
	    	
	    } 
	    
        if (response.exception != null) {
        	if (logger.isDebugging()) {
        		logger.data("API call complete.", "path", path, "code", response.code.name(), "data.type", response.dataType.name(), "exception", AnnotatedException.render(response.exception, true));
        	} else {
        		logger.data("API call complete.", "path", path, "code", response.code.name(), "data.type", response.dataType.name(), "exception", response.exception.getMessage());
        	}
        	
        } else {
        	logger.data("API call complete.", "path", path, "code", response.code.name(), "data.type", response.dataType.name());
        }
	}
	
	private void sendHttp(final OutputStream outputStream, final HttpResponse response, final String path) {
		
	    String statusText = getStatusText(response.code);
	    String header = createHeader(response.code, statusText, response.mimeType, response.length);
	
	    try {
		    outputStream.write(header.toString().getBytes("ISO-8859-1"));
		   
		    InputStream is = response.response;
		    byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
		    while (true) {
				int read = is.read(buffer);
				if (read < 0) break;
				outputStream.write(buffer, 0, read);
		    }
		        
		    outputStream.flush();
		    
	    } catch (Exception e) {
	    	throw new AnnotatedException("IO failure while responding to request.", e);
	    	
	    } finally {
	    	try {
	    		response.response.close();
	    	} catch (Exception e) {
	    		// Don't care
	    	}
	    }
	     	
        logger.data("API call complete.", "path", path, "code", response.code.name(), "mime.type", response.mimeType);
	}
	
	private String getStatusText(final APIResponseCodes code) {
	    if (code.getCode() < 300) {
	        return "OK";
	    } else if ((code.getCode() >= 300) && (code.getCode() < 400)) {
	        return "Bad Request";
	    } else {
	        return "Internal Server Error";
	    }		
	}
	
	private String createHeader(final APIResponseCodes code, final String statusText, final String contentType, final long dataLength ) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("HTTP/1.1 " + code.getCode() + " " + statusText + " \r\n");
	    sb.append("Content-Type: " + contentType + " \r\n");
	
	    SimpleDateFormat dataFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
	    dataFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	    sb.append("Date: " + dataFormatter.format(new Date()) + "\r\n");
	    sb.append("Content-Length: " + dataLength + "\r\n");
	    sb.append("Connection: close\r\n\r\n");
	    
	    return sb.toString();
	}
  
	private byte[] getData(final APIResponse response) {
		byte[] result = null;
		
		try {
		    switch(response.dataType) {
		    case DTMP:
		    case HTML:
		    case SNIP:
		    	if (response.exception == null) {
		    		result = response.response.toString().getBytes("UTF-8");
		    	} else {
		    		result = (response.response.toString() + "<br><br>\r\n\r\n<pre>\r\n" + AnnotatedException.render(response.exception, logger.isDebugging()) + "\r\n</pre>\r\n").getBytes("UTF-8");
		    	}
		    	break;
		    	
		    case TEXT:
		    	if (response.exception == null) {
		    		result = response.response.toString().getBytes("UTF-8");
		    	} else {
		    		result = (response.response.toString() + "\r\n\r\n" + AnnotatedException.render(response.exception, logger.isDebugging())).getBytes("UTF-8");
		    	}
		    	break;
		    	
		    case DATA:
	            try {
	                ByteArrayOutputStream baos = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
	                ObjectOutputStream oos = new ObjectOutputStream(baos);
	                oos.writeObject(response.response);
	                result = baos.toByteArray();
	            } catch (Exception e) {
	                throw new Exception("Not serializable.", e);
	            }		    	
		    	break;
		    	
		    case JSON:
		    	try {
			    	Gson gson = new Gson();
			    	String json = gson.toJson(response.response);
			    	result = json.getBytes("UTF-8");
		    	} catch (Exception e) {
		    		throw new Exception("Not convertable to JSON.", e);
		    	}
		    	break;
		    }
	
		} catch (Exception e) {
			throw new PuntException("Failed to transform data.", e);
		}
		return result;
	}
	
	private void getBodyParams(final Map<String, String> nv, final BufferedReader brin) throws IOException {
		
		boolean doBody = false;
		long contentLength = 0;
		
		// headers
		String name;
		String value;
		int splitSpot;
		
		String current = brin.readLine();
		while ((current != null) && (current.trim().length() > 0)) {
			splitSpot = current.indexOf(':');
			if ((splitSpot > 1) && (splitSpot < (current.length() - 2))) {
				name = current.substring(0, splitSpot).toLowerCase();
				value = current.substring(splitSpot + 1, current.length()).trim();
				if (name.equals("content-type")) {
					
					if (value.equals("application/x-www-form-urlencoded") || (value.equals("text/plain"))) {
						doBody = true;
					}
				
				} else if (name.equals("content-length")) {
					
					try {
						contentLength = Long.parseLong(value);
					} catch (Exception e) {
						throw new AnnotatedException("Bad Content-Length header.", e).annotate("header.value", value);
					}
					
				} // else do nothing
			}
			
			current = brin.readLine();
		}

		// Do body
		if ((doBody) && (contentLength > 0)) {
			formURLprocessor.parser(new LimitedReader(brin, contentLength), nv);
			
			// Resolve NVs.  IMPORTANT: there is no guaranteed order!
			Resolver resolver = new Resolver(new HttpResolveHandler(context, nv));
			for (String n : nv.keySet()) {
				// Brute force now.  Not sure this really ever needs to be optimized.
				nv.put(n, resolver.resolve(nv.get(n)));
			}
			
		} 
		
	}
	
    private String cutPathAndDecode(final String uri, final Map<String,String> nv) throws Exception {
    	String result;
        int pathDelim = uri.indexOf('?');
        if (pathDelim >= 0) {
            decodeNV(uri.substring(pathDelim + 1), nv);
            result = URLDecoder.decode(uri.substring(0, pathDelim), "UTF-8");
        } else {
        	result = URLDecoder.decode(uri, "UTF-8");
        }   
        return result;
    }
}