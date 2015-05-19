/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traveleescripts;

import java.sql.*;

/**
 *
 * @author nguyenphucuit
 */
public class MySQLConnection {

    private Connection dbConn = null;
    private String dbUrl = null;
    private String dbUser = null;
    private String dbPass = null;

    public MySQLConnection() {
        dbUrl = Configuration.Get(SettingKeys.Db.DB_URL) + "/" + Configuration.Get(SettingKeys.Db.DB_NAME);
        dbUser = Configuration.Get(SettingKeys.Db.USER);
        dbPass = Configuration.Get(SettingKeys.Db.PASS);
    }

    public MySQLConnection(String dbname, String user, String pass) {
        dbUrl = Configuration.Get(SettingKeys.Db.DB_URL) + "/" + dbname;
        dbUser = user;
        dbPass = pass;
    }

    public boolean connect() throws SQLException {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName(Configuration.Get(SettingKeys.Db.JDBC_DRIVER));
            // Setup the connection with the DB
            System.out.println("Connecting to database...");
            dbConn = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());

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
    
    public void close(){
        try {
            if(dbConn != null) {
                dbConn.close();
                System.out.println("Close OK!");
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }
    }

    /**
     * @return the m_conn
     */
    public Connection GetConnection() {
        return dbConn;
    }

    /**
     * @return the dbPass
     */
    public String getDbPass() {
        return dbPass;
    }

    /**
     * @param dbPass the dbPass to set
     */
    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    /**
     * @return the dbUser
     */
    public String getDbUser() {
        return dbUser;
    }

    /**
     * @param dbUser the dbUser to set
     */
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * @return the dbUrl
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * @param dbUrl the dbUrl to set
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
}
