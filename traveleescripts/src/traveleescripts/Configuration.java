package traveleescripts;

import java.util.Properties;

/**
 *
 * @author nguyenphucuit
 */
public class Configuration {

    private static Configuration _instance = null;

    private Properties props;

    protected Configuration() {
        // Exists only to defeat instantiation.
        props = new Properties();
        _preloadProperties();
    }

    public static Configuration getInstance() {
        if (_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }

    private void _preloadProperties() {
        getProps().setProperty(SettingKeys.Db.JDBC_DRIVER, "com.mysql.jdbc.Driver");
        getProps().setProperty(SettingKeys.Db.DB_URL, "jdbc:mysql://localhost");
        getProps().setProperty(SettingKeys.Db.DB_NAME, "travelee");
        getProps().setProperty(SettingKeys.Db.USER, "root");
        getProps().setProperty(SettingKeys.Db.PASS, "root");
        
        getProps().setProperty(SettingKeys.Place.API_KEY_2, "AIzaSyDGdA8KNH-eBZ4jnxCJDNmZamtK8XOe-MI");
        getProps().setProperty(SettingKeys.Place.API_KEY_3, "AIzaSyAgJvoCE3_ukSwJRYeMG5Sdn2igeXcTFAY");//Hieu api key
        getProps().setProperty(SettingKeys.Place.API_KEY_4, "AIzaSyAs5K9smLiMwJwn6ENedmq1oMX4nwpYhi0");//Manh api key ->ERROR
        getProps().setProperty(SettingKeys.Place.API_KEY_6, "AIzaSyAlezT6XDlyKrAezaJKuBvRL8WzSMUs4Xs");//Quyen api key
        
        //Photo
        getProps().setProperty(SettingKeys.Place.API_KEY_5, "AIzaSyAlezT6XDlyKrAezaJKuBvRL8WzSMUs4Xs");//my Test project api key
        getProps().setProperty(SettingKeys.Place.API_KEY_7, "AIzaSyD-Nzj4USMWJf1ibf-XN8EHf9Oq8wvns10");//To huy api key
        getProps().setProperty(SettingKeys.Place.API_KEY_8, "AIzaSyDPeMpz8wq5tvLycKPl6eiZAQ-lrLtpRBQ");//Loc Pham api key
        getProps().setProperty(SettingKeys.Place.API_KEY_9, "AIzaSyAf2TFOLKXglWkCLzr28AuVsqQpQrMGYSk");//nguyenphucdl92 api key
        getProps().setProperty(SettingKeys.Place.API_KEY_10, "AIzaSyByQDU9SWLaE9P9CpQ5hcyMYFm-zyljKCQ");//nguyenphucdl92 api key 2
        getProps().setProperty(SettingKeys.Place.API_KEY_11, "AIzaSyDWi4XJqbHCv6tuGzOhL2fRI_fsGay-koc");//nguyenphucuit@gmail api key 1
        getProps().setProperty(SettingKeys.Place.API_KEY_12, "AIzaSyCOy3mHH863Kd3yJ49imd3DW6mcub5CVWg");//nguyenphucuit@gmail api key 2
        getProps().setProperty(SettingKeys.Place.API_KEY_13, "AIzaSyA_Kr6uILnSbZ26i56Tx77rda7wtPsnlbc");//nguyenphucuit@gmail api key 3
        getProps().setProperty(SettingKeys.Place.API_KEY, "AIzaSyDScfYL4WknyAshddTZFGddpmcP_X-cvFs");//my api project
        getProps().setProperty(SettingKeys.Place.RADAR_URL, "https://maps.googleapis.com/maps/api/place/radarsearch/json?");
        getProps().setProperty(SettingKeys.Place.DETAILED_URL, "https://maps.googleapis.com/maps/api/place/details/json?");
        
        //Set base path
        getProps().setProperty(SettingKeys.Photo.BASE_PATH, "C:\\travelee\\photos");
    }

    public String GetProperty(String key) {
        if (getProps().containsKey(key)) {
            return getProps().getProperty(key);
        }
        return null;
    }

    public static String Get(String key) {
        return Configuration.getInstance().GetProperty(key);
    }

    /**
     * @return the props
     */
    public Properties getProps() {
        return props;
    }
}
