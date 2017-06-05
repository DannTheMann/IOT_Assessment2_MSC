package dja33.msc.ukc.myapplication;

/**
 *
 * HTTP No Response Exception
 *
 * Custom exception to handle the circumstance
 * as to when a website may not exist or might not be
 * available to the client.
 *
 * Created by Dante on 16/03/2017.
 */
public class HTTPNoResponseException extends Throwable {

    private final String url;
    private final int errorCode;

    public HTTPNoResponseException(String url, int errorCode, String s) {
        this.url = url;
        this.errorCode = errorCode;
        System.err.println(s);
    }

    /**
     * Get the response code from the site
     * @return
     */
    public int getResponseCode(){
        return errorCode;
    }

    /**
     * Get the URL of the site
     * @return
     */
    public String getURL(){
        return url;
    }
}
