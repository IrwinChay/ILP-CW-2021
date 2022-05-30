package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Feature;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Class to set up the map to traverse
 */
public class Map {

    /** constants */
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;

    private final LongLat startPos;
    private final ReadWeb web;
    private final ReadDataBase dataBase;
    private final Menus menus;
    private final String year;
    private final String month;
    private final String day;

    private List<Polygon> noFlyZones;
    private List<LongLat> landmarks;
    private List<OrderLoc> orders;
    private List<OrderLoc> ordersMod;


    /**
     * Create the map for a day
     * @param web connection to web server
     * @param dataBase connection to database server
     * @param year year from input
     * @param month month from input
     * @param day day from input
     */
    public Map(ReadWeb web, ReadDataBase dataBase, String year, String month, String day) {
        this.noFlyZones = new ArrayList<Polygon>();
        this.landmarks = new ArrayList<LongLat>();
        this.orders = new ArrayList<OrderLoc>();
        this.ordersMod = new ArrayList<OrderLoc>();
        this.year = year;
        this.month = month;
        this.day = day;
        this.web = web;
        this.dataBase = dataBase;
        this.menus = new Menus(this.web);
        this.startPos = new LongLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);

        initializeMap();

    }

    /**
     * Set up the no-fly-zones, landmarks, orders of the map
     */
    private void initializeMap() {

        // read and setup no-fly-zones from web server
        List<Feature> noFlyZoneRaw = this.web.readNoFlyZones();
        for (Feature value : noFlyZoneRaw) {
            Polygon oneZone = (Polygon) value.geometry();
            this.noFlyZones.add(oneZone);
        }

        // read and setup landmarks from server
        List<Feature> landmarkRaw = this.web.readLandmarks();
        for (Feature feature : landmarkRaw) {
            Point oneLandmark = (Point) feature.geometry();
            assert oneLandmark != null;
            LongLat lm = new LongLat(oneLandmark.longitude(), oneLandmark.latitude());
            this.landmarks.add(lm);
        }

        // read and setup orders from database
        HashMap<String, String> ordersRaw = this.dataBase.readOrders(this.year, this.month, this.day);
        System.out.println("Total number of orders today: " + ordersRaw.size());
        for (String orderNo : ordersRaw.keySet()) {
            String deliverTo = ordersRaw.get(orderNo);
            this.orders.add( initializeOrder(orderNo, deliverTo) );
        }
        // copy a list of order for drone traversal
        this.ordersMod = new ArrayList<OrderLoc>(this.orders);

    }

    /**
     * Fetch and setup one order from web and database
     * @param orderNo order number fetch from database
     * @param deliverTo word3word delivery location fetch from database
     */
    private OrderLoc initializeOrder(String orderNo, String deliverTo) {
        OrderLoc orderLoc;

        // fetch coordinates of delivrery location from web using word3word address
        String[] words = deliverTo.split("\\.");
        double[] longLatDeliverLoc = this.web.readDetails(words[0], words[1], words[2]);
        LongLat deliverLoc = new LongLat(longLatDeliverLoc[0], longLatDeliverLoc[1]);

        // fetch all items of an order from database
        List<String> items =  this.dataBase.readOrderDetails(orderNo);
        ArrayList<String> restaurantW3Ws = new ArrayList<>();

        // get word3word location of shops from menu by the ordered items
        for (String i : items){
            String restaurantW3W = this.menus.getRestaurantW3WLoc(i);
            if (!(restaurantW3Ws.contains(restaurantW3W))){
                restaurantW3Ws.add(restaurantW3W);
            }
        }

        // fetch coordinates of shops by their word3word address
        ArrayList<LongLat> restaurantLocs = new ArrayList<>();
        for (String restaurantW3W : restaurantW3Ws){
            String[] restaurantWords = restaurantW3W.split("\\.");
            double[] longLatRestaurantLoc = this.web.readDetails(restaurantWords[0], restaurantWords[1], restaurantWords[2]);
            LongLat restaurantLoc = new LongLat(longLatRestaurantLoc[0], longLatRestaurantLoc[1]);
            restaurantLocs.add(restaurantLoc);

        }
        // check validity of the order
        if (restaurantLocs.size() != 1 && restaurantLocs.size() != 2 ) {
            throw new IllegalArgumentException("An order can be made up of items from no more than two shops");
        }
            int cost = this.menus.getDeliveryCost(items);

            orderLoc = new OrderLoc(orderNo, deliverLoc, deliverTo, restaurantLocs, restaurantW3Ws, items, cost);
            return orderLoc;

    }


    /**
     * Pop the most expensive order from a copy list of today's order
     * @return the most expensive order
     */
    public OrderLoc popMostExpensiveOrder(){
        if (this.ordersMod.size() == 0){
            return null;
        }

        OrderLoc mostExpensiveOrder = this.ordersMod.get(0);
        int maxCost = 0;
        for (OrderLoc order : this.ordersMod){
            if (order.getCost() > maxCost){
                maxCost = order.getCost();
                mostExpensiveOrder = order;
            }
        }
        // pop it from the list
        this.ordersMod.remove(mostExpensiveOrder);
        return mostExpensiveOrder;

    }

    /**
     * Calculate the percentage monetary value delivered each day
     * ﬁxed delivery charge of 50p per order is included in both delivered value and total value
     * @param deliveredOrders all orders managed to delivered by the drone in a given day
     * @return the percentage monetary value, from zero to one
     */
    public double getPercentMoneyValue( List<OrderLoc> deliveredOrders ){
        int moneyDelivered = 0;
        int moneyTotal = 0;
        for (OrderLoc order : deliveredOrders){
            moneyDelivered += order.getCost();
        }
        for (OrderLoc o: this.orders){
            moneyTotal += o.getCost();
        }

        return ((double) moneyDelivered / moneyTotal);
    }


    /** getters */
    public List<Polygon> getNoFlyZones(){
        return this.noFlyZones;
    }

    public List<LongLat> getLandmarks(){
        return this.landmarks;
    }

    public int getOrderLength(){
        return this.orders.size();
    }

    public LongLat getStartPos(){
        return this.startPos;
    }


}
