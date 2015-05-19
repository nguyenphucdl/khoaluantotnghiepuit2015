package traveleescripts;

import java.sql.SQLException;

/**
 *
 * @author nguyenphucuit
 */
public class TraveleeScripts {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        TraveleeTask task1 = new TraveleeTask();
        task1.setAuthor("phuc");
        task1.setApiKey(Configuration.Get(SettingKeys.Place.API_KEY));//Don't need to set api key manually! It will automatically execute from api pool
        task1.setAutoKey(true);
        task1.init();
        //task1.miningFeaturedGeopoints(4, 30000);
        //task1.miningDetailPlace();
        //task1.miningPhotoFromPlace();
        task1.miningPhotosFromService();
        
    }
}
