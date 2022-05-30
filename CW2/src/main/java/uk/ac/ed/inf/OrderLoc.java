package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an order
 */
public class OrderLoc {

    private String orderNo;
    private String deliverW3W;
    private LongLat deliverLoc;
    // although not used in current version, keep word3word variables here for future expansion
    private String restaurantOneW3W;
    private String restaurantTwoW3W;
    private LongLat restaurantOneLoc;
    private LongLat restaurantTwoLoc;
    private boolean singleRestaurant;
    private List<String> items;
    private int cost;

    /**
     * Create the order
     * @param orderNo order number
     * @param deliverLoc delivery location in LongLat
     * @param deliverW3W word3word delivery location
     * @param restaurantLoc restaurant locations
     * @param restaurantW3W word3word address of restaurants
     * @param items items of the order
     * @param cost total cost of the order, include the 50p fixed delivery chargee
     * @throws IllegalArgumentException when an order does not have one or two shops
     */
    public OrderLoc(String orderNo, LongLat deliverLoc, String deliverW3W,
                    ArrayList<LongLat> restaurantLoc, ArrayList<String> restaurantW3W, List<String> items, int cost){
        this.orderNo = orderNo;
        this.deliverLoc = deliverLoc;
        this.deliverW3W = deliverW3W;
        this.items = items;
        this.cost = cost;

        if (restaurantLoc.size() == 2){
            this.singleRestaurant = false;
            // word3word addresses of restaurants are kept for future expansion of the app
            this.restaurantOneW3W = restaurantW3W.get(0);
            this.restaurantTwoW3W = restaurantW3W.get(1);
            this.restaurantOneLoc = restaurantLoc.get(0);
            this.restaurantTwoLoc = restaurantLoc.get(1);
        }else if (restaurantLoc.size() == 1){
            this.singleRestaurant = true;
            this.restaurantOneW3W = restaurantW3W.get(0);
            this.restaurantTwoW3W = null;
            this.restaurantOneLoc = restaurantLoc.get(0);
            this.restaurantTwoLoc = null;

        }else{
            throw new IllegalArgumentException("An order can be made up of items from no more than two shops");
        }


    }

    /** getters */
    public boolean isSingleRestaurant(){
        return this.singleRestaurant;
    }

    public String getOrderNo(){
        return this.orderNo;
    }

    public LongLat getDeliverLoc(){
        return this.deliverLoc;
    }

    public String getDeliverW3W(){
        return this.deliverW3W;
    }

    public LongLat getRestaurantOneLoc(){
        return this.restaurantOneLoc;
    }

    public LongLat getRestaurantTwoLoc(){
        return this.restaurantTwoLoc;
    }

    public int getCost() { return this.cost; }


}
