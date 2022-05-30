package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Class to connect to, read and write the database
 */
public class ReadDataBase {

    private static Connection conn;
    private static Statement statement;
    private static final String ordersQuery = "select * from orders where deliveryDate=(?)";
    private static final String orderDetailsQuery = "select * from orderDetails where orderNo=(?)";

    /**
     * Constructor, connect to the database
     * @param name name of the database, usually localhost
     * @param port port of the database, usually 9876
     * @throws SQLException if connection to database server failed
     */
    public ReadDataBase(String name, String port) {
        String jdbcString = "jdbc:derby://" + name + ":" + port + "/derbyDB";

        try{
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Read the orders table from database by a date
     * @param year year required
     * @param month month required
     * @param day day required
     * @return a hashmap of key = order number and value = word3word delivery position
     * @throws SQLException if connection to database server failed
     */
    public HashMap<String, String> readOrders(String year, String month, String day) {
        String fullDate = year + "-" + month + "-" + day;
        HashMap<String, String> orderNoAndDeliverTo = new HashMap<>();

        try {
            PreparedStatement psOrdersQuery =
                    conn.prepareStatement(ordersQuery);
            psOrdersQuery.setString(1, fullDate);

            // get the result and fill in the hashmap
            ResultSet rs = psOrdersQuery.executeQuery();
            while (rs.next()) {
                String orderNo = rs.getString("orderNo");
                String deliverTo = rs.getString("deliverTo");
                orderNoAndDeliverTo.put(orderNo, deliverTo);
            }

        } catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }

        return orderNoAndDeliverTo;
    }

    /**
     * Read the orderDetails table from database,
     * get all the items from an order by its order number
     * @param orderNo unique order number of the order
     * @return all items of the given order
     * @throws SQLException if connection to database server failed
     */
    public List<String> readOrderDetails(String orderNo) {
        ArrayList<String> items = new ArrayList<>();

        try{
            // search for the given order
            PreparedStatement psOrderDetailsQuery =
                    conn.prepareStatement(orderDetailsQuery);
            psOrderDetailsQuery.setString(1, orderNo);

            // get the result and fill in the arraylist
            ResultSet rs = psOrderDetailsQuery.executeQuery();
            while (rs.next()) {
                String oneItem = rs.getString("item");
                items.add(oneItem);
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        return items;

    }

    /**
     * Create the deliveries table containing all deliveries made by the drone of a given day
     * @param orders all orders managed to deliverd by the drone in a given day
     * @throws SQLException if connection to database server failed
     */
    public void createDeliveries( List<OrderLoc> orders ) {
        try{
            // drop the table named deliveries if exist
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }

            // create the deliveries table
            statement.execute(
                    "create table deliveries(" +
                            "orderNo char(8), " +
                            "deliveredTo varchar(19), " +
                            "costInPence int)");

            // fill the table with provided entries
            PreparedStatement psDeliveries = conn.prepareStatement( "insert into deliveries values (?, ?, ?)");
            for (OrderLoc order : orders) {
                psDeliveries.setString(1, order.getOrderNo());
                psDeliveries.setString(2, order.getDeliverW3W());
                psDeliveries.setInt(3, order.getCost());
                psDeliveries.execute();
            }
            System.out.println("Deliveries table created in database");
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create the flightpath table containing all flight steps made by the drone in a given day
     * @param flightpaths the flightpath of the drone in a given day
     * @throws SQLException if connection to database server failed
     */
    public void createFlightpath( List<Flightpath> flightpaths ) {
        try{
            // drop the table named flightpath if exist
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }

            // create the flightpath table
            statement.execute(
                    "create table flightpath(" +
                            "orderNo char(8), " +
                            "fromLongitude double, " +
                            "fromLatitude double, " +
                            "angle integer, " +
                            "toLongitude double, " +
                            "toLatitude double)");

            // fill the table with provided entries
            PreparedStatement psDeliveries = conn.prepareStatement( "insert into flightpath values (?, ?, ?, ?, ?, ?)");
            for (Flightpath path : flightpaths) {
                psDeliveries.setString(1, path.orderNo);
                psDeliveries.setDouble(2, path.fromLongitude);
                psDeliveries.setDouble(3, path.fromLatitude);
                psDeliveries.setInt(4, path.angle);
                psDeliveries.setDouble(5, path.toLongitude);
                psDeliveries.setDouble(6, path.toLatitude);
                psDeliveries.execute();
            }
            System.out.println("Flightpath table created in database");
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
    }


}
