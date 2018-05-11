package dadad.system.api.http;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import dadad.platform.AnnotatedException;
import dadad.platform.PlatformDataType;
import dadad.system.api.API;
import dadad.system.api.APIResponse;
import dadad.system.api.APIResponseCodes;

/**
 * HTTP API client.
 */
public class Client {
	
	// ===============================================================================
	// = FIELDS
	
    private final String host;
    private final int port;

    public Client(final String host, final int port) {
        this.host = host;
        this.port = port;
    }
    
	// ===============================================================================
	// = METHODS
    
    public APIResponse get(final API api, final String... nv) {
        String url = url(api, PlatformDataType.DATA, nv);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://" + host + ":" + port + url);

        APIResponse result = new APIResponse();
        ObjectInputStream ois = null;
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);

            result.code = APIResponseCodes.getCode(response.getStatusLine().getStatusCode());

            InputStream in = response.getEntity().getContent();
            ois = new ObjectInputStream(in);
            result.response = ois.readObject();

        } catch (Exception e) {
            result.code = APIResponseCodes.FAULT;
            result.exception = new RuntimeException("Fatal exception trying to make call.", e);

        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception ee) { // dont care
                }
            }
            if (response != null){
                try {
                    response.close();
                } catch (Exception ee) {
                    // Dont care for now.
                }
            }
        }

        if (! ((API) api).getResultClass().isInstance(result)) {
            result.exception = new AnnotatedException("Object returned was not the expected class.")
            		.annotate("expected", ((API) api).getResultClass().getName(), "actual", result.getClass().getName());
        }

        return result;
    }   
    
	// ===============================================================================
	// = TOOLS
    

    public static String url(API api, PlatformDataType dataType, String... nv) {
        return url(api.getAPIName(), api.getMethodName(), dataType, nv);
    }

    public static String url(String name, String command, PlatformDataType dataType, String... nv) {
        StringBuffer sb = new StringBuffer();
        sb.append("/").append(name).append('/').append(command).append('/').append(dataType.name().toLowerCase());

        boolean leader = false;
        if ((nv != null) && (nv.length  > 0)) {
            sb.append('?');

            for (int rover = 0; rover < nv.length; rover += 2) {
                if (leader) sb.append('&');
                sb.append(nv[rover]).append('=').append(nv[rover + 1]);
                leader = true;
            }
        }

        return sb.toString();
    }


    
}

