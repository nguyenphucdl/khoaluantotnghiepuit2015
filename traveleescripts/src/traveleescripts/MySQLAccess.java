/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traveleescripts;

import java.io.*;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nguyenphucuit
 */
public class MySQLAccess {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/travelee";

    // Database credentials
    static final String USER = "root";
    static final String PASS = "root";

    // Fields
    private Connection m_conn = null;

    private Statement m_statement = null;
    private PreparedStatement m_preparedStatement = null;

    public MySQLAccess() {

    }

    public boolean connect() throws SQLException {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName(JDBC_DRIVER);
            // Setup the connection with the DB
            System.out.println("Connecting to database...");
            m_conn = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (ClassNotFoundException cle) {
            System.out.println(cle.getMessage());
            return false;
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
            throw sqle;
        }
        System.out.println("Connect to database successfully!");
        return true;
    }

    public void close() throws SQLException {
        try {
            if (m_conn != null) {
                m_conn.close();
                System.out.println("Close OK!");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void test1() {
        String test1sql = "select count(*) as count from data_cities_raw";
        try {
            m_statement = m_conn.createStatement();
            ResultSet res = m_statement.executeQuery(test1sql);

            int count = 0;
            if (res.first()) {
                count = res.getInt("count");
                System.out.format("data_cities_raw counts %d\n", count);
            }

            m_statement.close();
        } catch (SQLException sqle) {
            System.out.println(sqle.getClass());
        }
    }

    public void test2() {
        String workingDirectory = System.getProperty("user.dir");
        String fileName = "PlaceTypes.txt";
        Path placeTypesPath = Paths.get(workingDirectory + "\\" + fileName);

        if (Files.notExists(placeTypesPath)) {
            System.out.format("%s is not exist\n", placeTypesPath.toString());
            return;
        }
        File file = new File(placeTypesPath.toString());
        List<String> placeTypes = new Vector<>();
        try {
            FileInputStream fis = new FileInputStream(file);
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("=");

                placeTypes.add(line);
            }
            System.out.format("PlaceTypes counts %d\n", placeTypes.size());

            fis.close();
            br.close();
        } catch (FileNotFoundException fe) {
            System.out.println(fe.getMessage());
        } catch (IOException ioe) {
            Logger.getLogger(MySQLAccess.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    public void generateGeoSearchPoints() {
        try {
            int min_latitude = 9;
            int max_latitude = 23;
            int min_longitude = 103;
            int max_longitude = 110;

            int gcount_latitude = (max_latitude - min_latitude + 1); // total vertex
            gcount_latitude += gcount_latitude - 1; // total middle vertex (total vertex - 1)
            int gcount_longitude = (max_longitude - min_longitude + 1);// total vertex
            gcount_longitude += gcount_longitude - 1; // total middle vertex  (total vertex - 1)

            float new_latitude, new_longitude;

            StringBuilder insertGeoPointsQuery = new StringBuilder();
            insertGeoPointsQuery.append("INSERT INTO geopoints(geopoint, featured, level) VALUES ");
            int length_origin = insertGeoPointsQuery.length();
            
            for (int lat = 0; lat < gcount_latitude; lat++) {
                for (int lon = 0; lon < gcount_longitude; lon++) {
                    new_latitude = min_latitude + 1.0f / 2 * lat;
                    new_longitude = min_longitude + 1.0f / 2 * lon;

                    if (checkCityNearby(new_latitude, new_longitude, 10)) {
                        //System.out.format("New %f,%f ", new_latitude, new_longitude);
                        insertGeoPointsQuery.append("(geomcollfromtext(\"Point(").append(new_longitude).append(" ").append(new_latitude).append(")\"),0,0)").append(",");
                    }
                }
                System.out.println("");
                
                
                //execute query
                if(insertGeoPointsQuery.length() != length_origin)
                {
                    //remove last colon if exist
                    insertGeoPointsQuery.deleteCharAt(insertGeoPointsQuery.length()-1);
                    
                    System.out.println(insertGeoPointsQuery.toString());
                    m_statement = m_conn.createStatement();
                    int res = m_statement.executeUpdate(insertGeoPointsQuery.toString());
                    System.out.format("Inserted result (%d rows)\n", res);
                }

                insertGeoPointsQuery = new StringBuilder();
                insertGeoPointsQuery.append("INSERT INTO geopoints(geopoint, featured, level) VALUES ");
                //reset query
            }

            //System.out.println(insertGeoPointsQuery.toString());
        } catch (SQLException sqle) {
            System.out.println(sqle.getClass());
        }
    }

    public boolean checkCityNearby(float latitude, float longitude, int distance) throws SQLException {

        boolean result = false;
        int row = 0;

        CallableStatement cstmt = null;
        StringBuilder strCityNearbyQuery = new StringBuilder();
        strCityNearbyQuery.append("{call getcitiesnearby(").append(latitude).append(",").append(longitude).append(",").append(distance).append(")}");
        
        cstmt = m_conn.prepareCall(strCityNearbyQuery.toString());
        

        ResultSet res = cstmt.executeQuery();

        if (res.first()) {
            row++;
            while (res.next()) {
                row++;
            }
        }
        System.out.format("CheckCityNearby la(%f), lon(%f), distance(%d) results(%d rows)\n", latitude, longitude, distance, row);
        if(row != 0)
            result = true;
        return result;
    }
}
