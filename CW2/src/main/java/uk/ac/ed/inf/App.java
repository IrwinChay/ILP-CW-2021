package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;

/**
 * The main class
 */
public class App {

    private static final String name = "localhost";

    /**
     * Main method
     *
     * @param args
     * <ul>
     * <li>[0] - required day </li>
     * <li>[1] - required month </li>
     * <li>[2] - required year </li>
     * <li>[3] - port for web server </li>
     * <li>[4] - port for database server </li>
     * </ul>
     */
    public static void main(String[] args) {

        String day = args[0];
        String month = args[1];
        String year = args[2];
        String httpPort = args[3];
        String jdbcPort = args[4];

        // connect to servers
        ReadWeb web = new ReadWeb(name, httpPort);
        ReadDataBase dataBase = new ReadDataBase(name, jdbcPort);

        // initialize web and drone
        Map map = new Map(web, dataBase, year, month, day);
        Drone drone = new Drone (map, map.getStartPos());

        // move the drone to deliver all orders
        for (int i = 0; i < map.getOrderLength(); i++){
            OrderLoc currentOrder = map.popMostExpensiveOrder();

            if (drone.getRemainBattery() <= 0 ){
                System.out.println("DRONE: out of battery");
                break;
            }
            if (drone.returned){
                break;
            }
            drone.moveDrone(currentOrder);
        }
        System.out.println("Remaining battery after delivery: " + drone.getRemainBattery());

        List<OrderLoc> delivered = drone.getOrderDelivered();
        List<Flightpath> flightpath = drone.getFlightpaths();

        // calculate precentage money value of this day
        double percentageMoneyValue = map.getPercentMoneyValue(delivered);
        System.out.println("percentage money value: " + percentageMoneyValue);

        // log the files
        createGeojson(flightpath, year, month, day);
        dataBase.createDeliveries(delivered);
        dataBase.createFlightpath(flightpath);

    }

    /**
     * Create the flightpath geojson file
     * @param allFlightpath all flightpath of the day
     * @param year required year
     * @param month required month
     * @param day required day
     * @throws IOException when failed to create Geojson file
     */
    private static void createGeojson(List<Flightpath> allFlightpath, String year,
                                   String month, String day) {
        // convert flightpath objects to linestring
        List<Point> flightpathPoints = new ArrayList<>();
        flightpathPoints.add(Point.fromLngLat(allFlightpath.get(0).fromLongitude, allFlightpath.get(0).fromLatitude));
        for (Flightpath fp : allFlightpath){
            flightpathPoints.add(Point.fromLngLat(fp.toLongitude, fp.toLatitude));
        }
        LineString flightpathLineString = LineString.fromLngLats(flightpathPoints);
        Feature flightpathFeature = Feature.fromGeometry( (Geometry) flightpathLineString );

        // convert flightpath to one feature in a feature collection
        ArrayList<Feature> flightpathList = new ArrayList<Feature>();
        flightpathList.add(flightpathFeature);
        FeatureCollection flightpathFC = FeatureCollection.fromFeatures(flightpathList);
        String flightpathJson = flightpathFC.toJson();

        // write the geojson file
        try {
            FileWriter myWriter = new FileWriter(
                    "drone-" + day + "-" + month + "-" + year + ".geojson");
            myWriter.write(flightpathJson);
            myWriter.close();
            System.out.println("Flightpath Geojson created");
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate flightpath Geojson");
            e.printStackTrace();
        }
    }



}
