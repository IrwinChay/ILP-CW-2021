package uk.ac.ed.inf;

/**
 * class to represent JSON word3word details
 * identical to structure in JSON file
 */
public class Details {

    String country;
    Squaure square;
    public class Squaure {

        Southwest southwest;
        public class Southwest {
            double lng;
            double lat;
        }

        Northeast northeast;
        public class Northeast {
            double lng;
            double lat;
        }
    }
    String nearestPlace;

    Coordinates coordinates;
    public class Coordinates {
        double lng;
        double lat;
    }

    String words;
    String language;
    String map;

}


