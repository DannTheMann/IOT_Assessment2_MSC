package dja33.msc.ukc.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * HTTP Handler
 *
 * Used to handle raw interactions using GET with AsyncTask provided
 * to not throttle the main applications thread. Given a URL it will
 * allow interactions with said website. Has some form of sanity checking
 * in the constructor to verify existence of website.
 *
 * Created by Dante on 15/03/2017.
 */
public class HTTPHandler extends AsyncTask<Void, Void, Void> {

    private final String url;
    private final List<String> rawContents;
    private final StringBuilder rawContentsString;
    private final HttpURLConnection connection;
    private int response;

    /**
     * Create the Handler, providing the URL to be used
     * will attempt to establish a connection to verify
     * the connection is real(ish) and not breakable.
     * @param url The url for the website
     */
    public HTTPHandler(final String url){
        this.url = url;
        this.rawContents = new ArrayList<>();
        this.rawContentsString = new StringBuilder();
        try{
            this.connection = (HttpURLConnection) new URL(url).openConnection();
        }catch(IOException e){
            throw new IllegalArgumentException("Invalid URL given.");
        }
    }

    @Deprecated
    /**
     * Used to connect directly on the main applications thread,
     * deprecated as Android does not explicitly allow for this although
     * it can be disabled in the build configuration. Otherwise safer to
     * use AsyncTask 'doInBackground'.
     *
     * Will attempt to connect to the website and scrape all information returned back
     * in raw format as a String.
     *
     * Returns the connection response given, i.e 200 for positive, 404 for nothing
     * @return response -1 if error arose otherwise response code from site
     */
    public int connect() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

            String next = null;

            rawContents.clear();
            rawContentsString.setLength(0);

            while((next = in.readLine()) != null){
                rawContents.add(next);
                rawContentsString.append(next);
            }

            return connection.getResponseCode();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Get the raw contents of the HTTPHandlers doing as a String
     * @return raw contents as continous string
     */
    public String getRawContentsAsString(){
        return rawContentsString.toString();
    }

    /**
     * Get the rawContents as a collection List.
     * @return List of raw contents
     */
    public List<String> getRawContents(){
        return rawContents;
    }

    /**
     * Get the URL being used by the HTTPHandler
     * @return the url
     */
    public String getURL(){
        return url;
    }

    @Override
    /**
     * AsyncTask to run in the background, safer that
     * connect as does not throttle main thread.
     *
     * Everything provided is null, not expected and
     * should not return any values.
     *
     * Will attempt to connect to the website and scrape all information returned back
     * in raw format as a String.
     *
     * Sets the connection response field given the response from the website, or -1 if an error
     * arose.
     */
    protected Void doInBackground(Void... params) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String next = null;

            rawContents.clear();
            rawContentsString.setLength(0);
            while((next = in.readLine()) != null){
                rawContents.add(next);
                rawContentsString.append(next);
            }

            response =  connection.getResponseCode();

        } catch (IOException e) {
            response = -1;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    /**
     * Unused, potential use for later.
     */
    protected void onPostExecute(Void result) {

    }

    /**
     * Get the response given from the HTTPHandler upon it's attempted connection
     * @return response
     */
    public int getResponse(){
        return response;
    }

}
