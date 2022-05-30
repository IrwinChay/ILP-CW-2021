package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to fetch and store the menu of all shops,
 * while includes helper methods
 */
public class Menus {

    private final HashMap<String, Integer> priceOfItems;
    private final HashMap<String, String> locOfItems;

    /**
     * Create the menu class, read the menu from web server and
     * transform the data into hashmaps for convenient access
     * @param web the web server
     */
    public Menus (ReadWeb web){
        this.priceOfItems = new HashMap<>();
        this.locOfItems = new HashMap<>();

        ArrayList<Restaurant> restaurantList = web.readMenus();
        // transform the fetched restaurant data into hashmaps for convenient access
        for (Restaurant oneRestaurant : restaurantList){
            ArrayList<Restaurant.Menu> menuItems = oneRestaurant.menu;
            for (Restaurant.Menu oneMenu : menuItems){
                this.priceOfItems.put(oneMenu.item, oneMenu.pence);
                this.locOfItems.put(oneMenu.item, oneRestaurant.location);
            }
        }
    }

    /**
     * get the delivery cost, including delivery charge of 50p
     * @param items list of items being deliver
     * @return cost in pence of having all of these items delivered by drone,
     * including the standard delivery charge of 50p per delivery
     */
    public int getDeliveryCost (List<String> items){
        int totalPrice = 50;

        // for each input, count the price of corresponding item in the menu of all restaurants
        for (String oneItem : items) {
            totalPrice += this.priceOfItems.get(oneItem);
        }
        return totalPrice;
    }

    /** get the word3word address of the restaurant providing the given item
     * @param item the item being provided
     * @return the word3word address of the restaurant
     */
    public String getRestaurantW3WLoc (String item){
        return this.locOfItems.get(item);
    }


}






