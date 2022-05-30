package uk.ac.ed.inf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

/**
 * Class to connect to, and read web server
 */
public class ReadWeb {
    private final String name;
    private final String port;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpResponse<String> response;


    /**
     * Constructor
     * @param name name of the web server, usually localhost
     * @param port port of the web server, usually 9898
     */
    public ReadWeb (String name, String port){
        this.name = name;
        this.port = port;
    }

    /**
     * Connect to the web server by a given url
     * @param urlString url of the web
     * @throws IOException if the web server could not be accessed
     */
    private void connect (String urlString){

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .build();
            response =
                    client.send(request, BodyHandlers.ofString());

            // catch errors
            if (response.statusCode() != 200){
                System.err.println("Fatal error: Unable to connect to " + this.name + " at port " + this.port + ".");
                System.exit(1);
            }
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Fetch the menu from web server
     * @return the menus of all restaurants
     */
    public ArrayList<Restaurant> readMenus(){
        String urlString = "http://" + this.name + ":" + this.port + "/menus/menus.json";
        connect (urlString);

        // fetch the menu and fill the array list
        Type listType = new TypeToken<ArrayList<Restaurant>>() {} .getType();
        ArrayList<Restaurant> restaurantList =
                            new Gson().fromJson(response.body(), listType);
        return restaurantList;

    }

    /**
     * Fetch no fly zones from web server
     * @return all no fly zones
     */
    public List<Feature> readNoFlyZones(){
        String urlString = "http://" + this.name + ":" + this.port + "/buildings/no-fly-zones.geojson";
        connect (urlString);

        List<Feature> noFlyZones = FeatureCollection.fromJson(response.body()).features();
        return noFlyZones;
    }

    /**
     * Fetch all landmarks from web server
     * @return all landmarks
     */
    public List<Feature> readLandmarks(){
        String urlString = "http://" + this.name + ":" + this.port + "/buildings/landmarks.geojson";
        connect (urlString);
        List<Feature> landmarks = FeatureCollection.fromJson(response.body()).features();
        return landmarks;
    }

    /**
     * Fetch coordinate of a point from web server via its word3word address
     * @return the coordinate of the give point
     */
    public double[] readDetails(String w1, String w2, String w3){
        String urlString = "http://" + this.name + ":" + this.port + "/words/" + w1 + "/" + w2 + "/" + w3 + "/details.json";
        connect (urlString);

        Details wordDetails = new Gson().fromJson(response.body(), Details.class);
        // record the coordinate and disregard everything else
        double[] longlat = new double[2];
        longlat[0] = wordDetails.coordinates.lng;
        longlat[1] = wordDetails.coordinates.lat;
        return longlat;
    }




}
