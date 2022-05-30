package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * class to represent JSON menu
 * identical to structure in JSON file
 */
public class Restaurant {
    public String name;
    public String location;
    public ArrayList<Menu> menu;

    public static class Menu{
        public String item;
        public int pence;
    }

}
