package uk.ac.ed.inf;

import java.util.ArrayList;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;


/**
 * Class responsible for connecting to web server and parse the JSON document
 * with fundamental method to check delivery cost
 */
public class Menus {

    /** URL, client and response from the web server */
    private final String urlString;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpResponse<String> response;

    /**
     * Create an url, connect to web server and log response
     * @param name name of the web server, usually "localhost"
     * @param port of the web server, usually "9898"
     */
    public Menus (String name, String port){
        this.urlString = "http://" + name + ":" + port + "/menus/menus.json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .build();
            response =
                    client.send(request, BodyHandlers.ofString());

            // catch errors
            if (response.statusCode() != 200){
                System.out.println("Fatal error: Unable to connect to " + name + " at port " + port + ".");
                System.exit(1);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * get the delivery cost, including delivery charge of 50p
     * @param items variable number of items being deliver
     * @return cost in pence of having all of these items delivered by drone,
     * including the standard delivery charge of 50p per delivery
     * @throws NullPointerException If the given input is null.
     */
    public int getDeliveryCost (String... items){
        if (items == null){
            throw new NullPointerException("The input cannot be null");
        }

        int totalPrice = 50;

        // parse the JSON list
        Type listType = new TypeToken<ArrayList<Restaurant>>() {} .getType();
        ArrayList<Restaurant> restaurantList =
                new Gson().fromJson(response.body(), listType);

        ArrayList<Restaurant.Menu> menuItems;

        // for each input, count the price of corresponding item in the menu of all restaurants
        for (String oneItem : items) {
            for (Restaurant oneRestaurant : restaurantList){
                menuItems = oneRestaurant.menu;

                for (Restaurant.Menu oneMenu : menuItems){
                    if (oneMenu.item.equals(oneItem)){
                        totalPrice += oneMenu.pence;
                    }
                }
            }
        }
        return totalPrice;
    }


}






