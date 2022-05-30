package uk.ac.ed.inf;

/**
 * Class to define the coordinate for the drone and its fundamental methods
 */
public class LongLat {

    /** coordinate of the drone */
    public final double longitude;
    public final double latitude;

    /**
     * Create a coordinate instance for the drone.
     * @param longitude the longitude coordinate of the drone
     * @param latitude the latitude coordinate of the drone
     */
    public LongLat (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;

    }

    /**
     * Test if the drone is inside the conﬁnement area
     *
     * @return true if inside the square confinement area defined in PDF, false if outside or on its edge
     */
    public boolean isConfined() {
        // inside the edges of the drone confinement area
        if (longitude < -3.184319 && longitude > -3.192473 &&
                latitude > 55.942617 && latitude < 55.946233){
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
     * @throws NullPointerException If the given input is null.
     */
    public double distanceTo (LongLat anotherPos) {

        if (anotherPos == null){
            throw new NullPointerException("The input cannot be null");
        }

        double x1 = this.longitude;
        double x2 = anotherPos.longitude;
        double y1 = this.latitude;
        double y2 = anotherPos.latitude;

        // calculate Pythagorean distance of two points
        double distance = Math.sqrt(
                Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
        return distance;
    }

    /**
     * Test if two points are close to each other:
     * strictly less than the distance tolerance of 0.00015 degrees
     *
     * @param anotherPos the other position to be tested
     * @return true if two points are strictly less than distance tolerance, false if otherwise
     * @throws NullPointerException If the given input is null.
     */
    public boolean closeTo (LongLat anotherPos) {
        if (anotherPos == null){
            throw new NullPointerException("The input cannot be null");
        }

        if (distanceTo (anotherPos) < 0.00015) {
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
        if (angle == -999){
            return this;
        } else {
            if (angle < 0 || angle > 350) {
                throw new IllegalArgumentException("angle must between 0 and 350");
            }
            if (angle % 10 != 0) {
                throw new IllegalArgumentException("angle must a multiple of 10");
            }

            double newLong = this.longitude + Math.cos (radius) * 0.00015;
            double newLat = this.latitude + Math.sin (radius) * 0.00015;

            LongLat newPos = new LongLat (newLong, newLat);
            return newPos;
        }
    }


}



