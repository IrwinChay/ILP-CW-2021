package uk.ac.ed.inf;


import java.util.List;
import java.util.ArrayList;

/**
 * Class representing the drone,
 * with pathfinding method and method to deliver an order
 */
public class Drone {

    public static final int BATTERY = 1500;

    public boolean returned;

    private final LongLat startPos;
    private final Map map;
    private LongLat dronePos;
    private final List<LongLat> landmarks;
    private int remainBattery;
    private List<OrderLoc> orderDelivered;
    private List<Flightpath> flightpaths;



    /**
     * Create the drone
     * @param map the map the drone is traversing
     * @param startPos starting position of the drone, usually Appleton Tower
     */
    public Drone (Map map, LongLat startPos){
        this.map = map;
        this.startPos = startPos;
        this.dronePos = new LongLat (startPos.getLongitude(), startPos.getLatitude());
        this.landmarks = map.getLandmarks();
        this.remainBattery = BATTERY;
        this.orderDelivered = new ArrayList<OrderLoc>();
        this.flightpaths = new ArrayList<Flightpath>();
        this.returned = false;
    }

    /**
     * Calculate the path and move the drone to deliver the current order
     * @param order the order the drone is delivering
     */
    public void moveDrone(OrderLoc order){

        // calculate the route: connecting shops and customer and avoiding restricted area
        ArrayList<LongLat> routeOfOrder = planRoute(order);
        double heuristic = 0.0;
        boolean prepareToReturn = false;

        // calculate heuristic distance: straight line distance connecting the route
        for (int i = 0; i < routeOfOrder.size() - 1; i++){
            heuristic += routeOfOrder.get(i).distanceTo( routeOfOrder.get(i+1) );
        }

        // return to starting position if the drone does not have enough battery left to deliver the order
        // according to heuristic
        if (heuristic / LongLat.SMALLEST_DISTANCE > this.remainBattery){
            routeOfOrder.clear();
            addLocToRoute(this.dronePos, this.startPos, routeOfOrder);
            prepareToReturn = true;
            System.out.println("DRONE: not enough battery, give up current order and return");

            // set the drone to return if the order is the final order today
        } else if (this.orderDelivered.size() + 1 == this.map.getOrderLength()){
            addLocToRoute(routeOfOrder.get( routeOfOrder.size() - 1), this.startPos, routeOfOrder);
            prepareToReturn = true;
        }

        // move the drone
        moveSteps(order, routeOfOrder, prepareToReturn);

    }

    /**
     * Helper function to actually move the drone
     * @param order the current order
     * @param routeOfOrder the planned route to deliver the current order
     * @param prepareToReturn if the drone have finished today's order and ready to return
     */
    private void moveSteps(OrderLoc order, ArrayList<LongLat> routeOfOrder, boolean prepareToReturn ){
        while (this.remainBattery > 0){
            // move a step and record it
            LongLat newPos = this.dronePos.move(this.map, routeOfOrder.get(0));

            Flightpath thisMove = new Flightpath(order.getOrderNo(), this.dronePos.getLongitude(),
                    this.dronePos.getLatitude(), this.dronePos.getAngle(), newPos.getLongitude(), newPos.getLatitude());

            this.flightpaths.add(thisMove);

            this.dronePos.setLongLat(newPos);
            this.remainBattery -= 1;

            // hover if the drone is close to its target, while recording this step
            if (this.dronePos.closeTo(routeOfOrder.get(0))) {
                this.dronePos = this.dronePos.nextPosition(LongLat.ANGLE_WHILE_HOVERING);

                Flightpath hoverMove = new Flightpath(order.getOrderNo(), this.dronePos.getLongitude(), this.dronePos.getLatitude(),
                        LongLat.ANGLE_WHILE_HOVERING, this.dronePos.getLongitude(), this.dronePos.getLatitude());
                this.flightpaths.add(hoverMove);

                this.remainBattery -= 1;
                routeOfOrder.remove(0);

                // delivered the last order of the day
                if ((!routeOfOrder.isEmpty()) && routeOfOrder.get(0).equals(this.startPos)){
                    orderDelivered.add(order);
                    System.out.println("DRONE: the last order delivered, begin to return");
                }
            }
            // delivered the current order
            if (routeOfOrder.isEmpty() && !prepareToReturn) {
                orderDelivered.add(order);
                System.out.println("DRONE: current order delivered");
                break;
                // the drone returned to starting position
            } else if (routeOfOrder.isEmpty() && this.dronePos.closeTo(this.startPos)){
                this.returned = true;
                System.out.println("DRONE: returned to starting position");
                break;
            }
        }
    }


    /**
     * Plan the route of the drone for current order,
     * connecting shops and customer and avoiding restricted area
     * @param order the current order
     * @return the route
     */
    private ArrayList<LongLat> planRoute( OrderLoc order ){
        ArrayList<LongLat> route = new ArrayList<LongLat>();

        if (order.isSingleRestaurant()){
            addLocToRoute(this.dronePos, order.getRestaurantOneLoc(), route);
            addLocToRoute(order.getRestaurantOneLoc(), order.getDeliverLoc(), route);

        } else {
            // go to the closer shop if the order have two shops
            if (this.dronePos.distanceTo( order.getRestaurantOneLoc() )
                    < this.dronePos.distanceTo( order.getRestaurantTwoLoc() )){
                LongLat[] routeOrder =
                        {this.dronePos, order.getRestaurantOneLoc(), order.getRestaurantTwoLoc(), order.getDeliverLoc()};
                for (int i = 0; i < routeOrder.length - 1; i++){
                    addLocToRoute(routeOrder[i], routeOrder[i+1], route);
                }
            } else {
                LongLat[] routeOrder =
                        {this.dronePos, order.getRestaurantTwoLoc(), order.getRestaurantOneLoc(), order.getDeliverLoc()};
                for (int i = 0; i < routeOrder.length - 1; i++){
                    addLocToRoute(routeOrder[i], routeOrder[i+1], route);
                }
            }
        }
        return route;

    }

    /**
     * Helper function to plan the route of the drone for current order,
     * add a desired position for the drone
     * @param prePos previous position for the drone to visit
     * @param nextPos desired position for the drone
     * @param route the route of the drone
     */
    private void addLocToRoute( LongLat prePos, LongLat nextPos, ArrayList<LongLat> route ){
        if (!prePos.outsideNoFlyZone(this.map, nextPos)) {
            LongLat landmark = whichLandmark(prePos, nextPos);
            route.add(landmark);
        }
        route.add(nextPos);
    }

    /**
     * Helper function to plan the route of the drone for current order,
     * guide the drone to reach a landmark if
     * the direct path to desired position is blocked by no-fly-zones
     * @param prePos previous position for the drone to visit
     * @param nextPos desire position for the drone
     */
    private LongLat whichLandmark( LongLat prePos, LongLat nextPos){
        List<LongLat> acceptableLandmarks = new ArrayList<LongLat>();
        double minDistance = Double.POSITIVE_INFINITY;

        // get all landmarks unblocked by no fly zone
        for (LongLat landmark : this.landmarks){
            if(prePos.outsideNoFlyZone( this.map, landmark) && landmark.outsideNoFlyZone( this.map, nextPos )){
                acceptableLandmarks.add(landmark);
            }
        }
        // use all landmarks if all landmarks being blocked by no fly zone
        LongLat lm = this.landmarks.get(0);
        if(acceptableLandmarks.size() == 0){
            acceptableLandmarks = this.landmarks;
        }
        // go to the landmark that minimize total distance
        for (LongLat landmark : acceptableLandmarks){
            double distance = prePos.distanceTo(landmark) + landmark.distanceTo(nextPos);
            if (distance < minDistance){
                minDistance = distance;
                lm = landmark;
            }
        }
    return lm;

    }


    /** getters  */
    public List<OrderLoc> getOrderDelivered(){
        return this.orderDelivered;
    }

    public List<Flightpath> getFlightpaths(){
        return this.flightpaths;
    }

    public int getRemainBattery(){
        return this.remainBattery;
    }




}
