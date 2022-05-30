package uk.ac.ed.inf;


import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Point;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * Class to define the coordinate for the objects and fundamental methods
 */
public class LongLat {

    /** coordinate of the object: drone, restaurant, delivery point or landmark */
    private double longitude;
    private double latitude;
    private int angle;

    /** constants */
    private static final double LONGITUDE_RIGHT = -3.184319;
    private static final double LONGITUDE_LEFT = -3.192473;
    private static final double LATITUDE_DOWN = 55.942617;
    private static final double LATITUDE_UP = 55.946233;
    public static final double SMALLEST_DISTANCE = 0.00015;
    public static final int ANGLE_WHILE_HOVERING = -999;



    /**
     * Create a coordinate instance for the drone.
     * @param longitude the longitude coordinate of the drone
     * @param latitude the latitude coordinate of the drone
     */
    public LongLat (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        // define initial angle to be zero
        this.angle = 0;

    }

    /**
     * Test if the drone is inside the conﬁnement area
     * @param pos the position object to be tested
     * @return true if inside the square confinement area defined in PDF, false if outside or on its edge
     */
    private boolean isConfined( LongLat pos ) {
        // inside the edges of the drone confinement area
        if (pos.getLongitude() < LONGITUDE_RIGHT && pos.getLongitude() > LONGITUDE_LEFT &&
                pos.getLatitude() > LATITUDE_DOWN && pos.getLatitude() < LATITUDE_UP){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pythagorean distance from the current drone position to another position,
     * assuming the drone is on a plane
     *
     * @param anotherPos the other position to be calculated
     * @return the Pythagorean distance between two points
     */
    public double distanceTo (LongLat anotherPos) {
        double x1 = this.longitude;
        double x2 = anotherPos.getLongitude();
        double y1 = this.latitude;
        double y2 = anotherPos.getLatitude();

        // calculate Pythagorean distance of two points
        double distance = Math.sqrt(
                Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
        return distance;
    }

    /**
     * Test if going to a position from current position is outside No-Fly-Zone
     *
     * @param map the map of the drone navigating
     * @param anotherPos the position to be tested
     * @return outside No-Fly-Zone or not
     */
    public boolean outsideNoFlyZone(Map map, LongLat anotherPos){
        Line2D path = new Line2D.Double(this.latitude, this.longitude,
                                        anotherPos.getLatitude(), anotherPos.getLongitude());
        List<Polygon> noFlyZones = map.getNoFlyZones();

        // for every line segment of every zone,
        // test if it intersects with the line segment of the current move
        for (Polygon zone: noFlyZones) {
            List<Point> points = zone.coordinates().get(0);
            for (int i = 0; i < points.size() - 1; i++) {
                int j = (i + 1) % points.size();
                Point pointA = points.get(i);
                Point pointB = points.get(j);
                Line2D lineAB = new Line2D.Double(pointA.latitude(), pointA.longitude(),
                        pointB.latitude(), pointB.longitude());
                if (path.intersectsLine(lineAB)) {
                    return false;
                }

            }

        }
        return true;

    }



    /**
     * Test if two points are close to each other:
     * strictly less than the distance tolerance of 0.00015 degrees
     *
     * @param anotherPos the other position to be tested
     * @return true if two points are strictly less than distance tolerance, false if otherwise
     */
    public boolean closeTo (LongLat anotherPos) {
        if (distanceTo (anotherPos) < SMALLEST_DISTANCE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * When the drone makes a move, fly or hover, give the result coordinate of the drone
     *
     * @param angle The angle the drone is flying to, -999 if hovering,
     *must between 0 and 350 when flying, and must be a multiple of 10 degrees
     * @return The result coordinate of the move, the original coordinate if choose to hover
     * @throws IllegalArgumentException If the given input is not between 0 and 350 when moving
     * @throws IllegalArgumentException If the given input is not a multiple of 10 when moving
     */
    public LongLat nextPosition (int angle){
        double radius = Math.toRadians (angle);

        // drone is hovering
        if (angle == ANGLE_WHILE_HOVERING){
            return this;
        } else {
            if (angle < 0 || angle > 350) {
                throw new IllegalArgumentException("angle must between 0 and 350");
            }
            if (angle % 10 != 0) {
                throw new IllegalArgumentException("angle must a multiple of 10");
            }

            double newLong = this.longitude + Math.cos (radius) * SMALLEST_DISTANCE;
            double newLat = this.latitude + Math.sin (radius) * SMALLEST_DISTANCE;

            LongLat newPos = new LongLat (newLong, newLat);
            return newPos;
        }
    }

    /**
     * Get the valid direction of the drone (multiple of ten) to travel to another position
     *
     * @param anotherPos the desired position
     * @return valid angle of the drone
     */
    private int calculateAngle(LongLat anotherPos){
        double x1 = this.longitude;
        double x2 = anotherPos.getLongitude();
        double y1 = this.latitude;
        double y2 = anotherPos.getLatitude();

        double angleRadius = Math.atan2(x2 - x1, y2 - y1);
        int angleRaw = (int) Math.toDegrees(angleRadius);

        // set the angle valid
        if (angleRaw < 0){
            angleRaw += 360;
        } else if (angleRaw >= 360){
            angleRaw -= 360;
        }
        if (angleRaw >= 0 && angleRaw < 90) {
            angleRaw = 90 - angleRaw;
        } else if (angleRaw >= 90 && angleRaw < 360) {
            angleRaw = 450 - angleRaw;
        }

        // set angle to multiples of ten
        int angleTruth = Math.round( angleRaw / 10) * 10;

        if (angleTruth == 360){
            angleTruth = 0;
        }

        return Math.round(angleTruth / 10) * 10;


    }

    /**
     * Calculate the next valid move to a desired position,
     * outside no-fly-zone and inside drone confinment area,
     * if next move not valid, modify angle in +10,-10, +20,-20
     *
     * @param map the map to traverse
     * @param desirePos desired position of the drone
     * @return the next valid move
     */
    public LongLat move(Map map, LongLat desirePos){
        int preAngle = this.angle;
        this.angle = calculateAngle(desirePos);
        LongLat nextPos = nextPosition(this.angle);
        int adjustment = 10;

        // if the move is not valid, increase the angle until it is valid
        // modify angle in +10,-10, +20,-20
        while ( !(outsideNoFlyZone(map, nextPos) && isConfined(nextPos)) ){
            this.angle += adjustment;
            // go back to previous location is forbidden,
            // since it might cause the drone trap in a point
            if (Math.abs(preAngle - this.angle) == 180){
                this.angle += 10*(adjustment/Math.abs(adjustment));
            }
            if (this.angle >= 360){
                this.angle -= 360;
            }
            if (this.angle < 0){
                this.angle += 360;
            }
            nextPos = nextPosition(this.angle);
            adjustment = - (adjustment + 10*(adjustment/Math.abs(adjustment)));

        }
        return nextPos;

    }


    /** getters and setters */
    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public int getAngle(){
        return this.angle;
    }

    public void setLongLat(LongLat newPos){
        this.longitude = newPos.getLongitude();
        this.latitude = newPos.getLatitude();
    }


}



