package traveleescripts;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 *
 * @author nguyenphucuit
 */
public class TraveleeTask {

    private MySQLConnection mySqlConn = null;
    private PlaceSearch placeSearch = null;
    private String author = null;
    private JSONParser parser = null;
    private String apiKey = null;
    private String photoPath = null;
    private boolean autoKey = false;
    private boolean isDebug = false;

    public TraveleeTask() {
        
    }

    public void init() {
        mySqlConn = new MySQLConnection();

        placeSearch = new PlaceSearch();
        placeSearch.setApiKey(apiKey);
        placeSearch.setAutoKey(autoKey);
        placeSearch.setRadarUrl(Configuration.Get(SettingKeys.Place.RADAR_URL));
        placeSearch.setDetailedUrl(Configuration.Get(SettingKeys.Place.DETAILED_URL));
        placeSearch.setIsDebug(isDebug);
        parser = new JSONParser();
        placeSearch.setParser(parser);

        photoPath = Configuration.Get(SettingKeys.Photo.BASE_PATH); 
        
        
    }

    private Vector<String> getFeaturedPlaceType() {
        Vector<String> fPlaceType = new Vector<String>();
        fPlaceType.add("cafe");
        fPlaceType.add("restaurant");
        fPlaceType.add("church");
        fPlaceType.add("food");
        fPlaceType.add("night_club");
        fPlaceType.add("rv_park");
        fPlaceType.add("travel_agency");
        fPlaceType.add("zoo");
        fPlaceType.add("park");
        fPlaceType.add("night_club");
        fPlaceType.add("museum");
        fPlaceType.add("amusement_park");
        return fPlaceType;
    }

    private void processRadarSearchResponse(JSONObject response) throws SQLException {
        String placeInsertString = "insert ignore into places(place_id, g_id, geom) values";
        StringBuilder placeInsertQueryBuilder = new StringBuilder(placeInsertString);

        JSONObject resJSON = (JSONObject) response;
        String statusCode = (String) resJSON.get("status");
        if (!statusCode.equalsIgnoreCase("ok")) {
            return;
        }
        JSONArray resArray = (JSONArray) resJSON.get("results");
        System.out.format("HTTP RESPONSE: (%d object)\n", resArray.size());
        Iterator<JSONObject> iter = resArray.iterator();

        JSONObject geoObj = null, placeObj = null, locationObj = null;
        double geoLat, geoLon;
        String place_id = null, g_id = null;
        boolean isDone = false, isInsert = false;
        int count = 0;
        while (!isDone) {
            if (iter.hasNext()) {
                count++;
                placeObj = (JSONObject) iter.next();
                geoObj = (JSONObject) placeObj.get("geometry");
                locationObj = (JSONObject) geoObj.get("location");
                geoLat = (double) locationObj.get("lat");
                geoLon = (double) locationObj.get("lng");

                g_id = (String) placeObj.get("id");
                place_id = (String) placeObj.get("place_id");

                placeInsertQueryBuilder.append("(\"").append(place_id).append("\",\"").append(g_id).append("\",");
                placeInsertQueryBuilder.append("ST_GEOMCOLLFROMTEXT(\"Point(").append(geoLon).append(" ").append(geoLat).append(")\"))").append(",");
            } else {
                //System.out.println("Insert last!");
                isInsert = true;
                isDone = true;
            }

            if (count >= 40) {
                count = 0;
                //Insert into db
                isInsert = true;
            }

            if (isInsert) {
                if (placeInsertQueryBuilder.length() != placeInsertString.length()) {
                    //remove last colon if exist
                    placeInsertQueryBuilder.deleteCharAt(placeInsertQueryBuilder.length() - 1);
                    //insert db
                    Statement insertStatement = mySqlConn.GetConnection().createStatement();
                    if (!isDebug) {
                        System.out.println(placeInsertQueryBuilder.toString());
                        insertStatement.execute(placeInsertQueryBuilder.toString());
                    }

                    //reset query builder
                    placeInsertQueryBuilder = new StringBuilder();
                    placeInsertQueryBuilder.append(placeInsertString);

                    //reset insert 
                    isInsert = false;
                }
            }
        }
    }

    private void processDetailSearchResponse(JSONObject response, String place_id) throws SQLException {
        String placeUpdateFormat = "update places set `name`=\"%s\", `formatted_address`=\"%s\", `formatted_phone_number`=\"%s\", `icon`=\"%s\", `types`=\"%s\","
                + "`tags`=\"%s\", `response`=\"%s\", `rating`=%.2f, `international_phone_number`=\"%s\", `reference`=\"%s\", `url`=\"%s\", `vicinity`=\"%s\", `website`=\"%s\" where `place_id` like \"%s\"";

        String statusCode = (String) response.get("status");
        if (!statusCode.equalsIgnoreCase("ok")) {
            return;
        }

        JSONObject resultObj = (JSONObject) response.get("result");
        String formatted_address = (String) resultObj.get("formatted_address");
        String formatted_phone_number = (String) resultObj.get("formatted_phone_number");
        String international_phone_number = (String) resultObj.get("international_phone_number");
        String reference = (String) resultObj.get("reference");
        String url = (String) resultObj.get("url");
        String vicinity = (String) resultObj.get("vicinity");
        String website = (String) resultObj.get("website");
        String icon = (String) resultObj.get("icon");
        String name = (String) resultObj.get("name");
        name = name.replaceAll("\\\"", "");
        String responseEscaped = JSONObject.escape(response.toString());

        Object temp = (Object) resultObj.get("rating");
        double rating = 0.0;
        if (temp != null) {
            if (temp instanceof Long) {
                rating = ((Long) temp).doubleValue();
            } else {
                rating = (double) temp;
            }
        }

        String types = ((JSONArray) resultObj.get("types")).toString();

        String placeUpdateString = String.format(placeUpdateFormat, name, formatted_address, formatted_phone_number, icon,
                JSONObject.escape(types), "", responseEscaped, rating, international_phone_number, reference, url, vicinity, website, place_id);
        Statement placeUpdateStatement = mySqlConn.GetConnection().createStatement();
        if (!isDebug) {
            System.out.println("Inserting...");
            placeUpdateStatement.executeUpdate(placeUpdateString);
        }
        System.out.println(placeUpdateString);

    }

    public void miningFeaturedGeopoints(int f, int rad) {

        try {
            mySqlConn.connect();

            String qfgpString = "select geopoint_id as id, ST_Y(geopoint) as lat, ST_X(geopoint) as lon, featured  from geopoints where featured = ?";
            PreparedStatement qfgp = mySqlConn.GetConnection().prepareStatement(qfgpString);
            qfgp.setInt(1, f);

            ResultSet res = qfgp.executeQuery();
            while (res.next()) {
                int point_id = res.getInt("id");
                double lat = res.getDouble("lat");
                double lon = res.getDouble("lon");
                int featured = res.getInt("featured");

                System.out.format("Mining geopoint id(%d) lat(%f) lon(%f) featured(%d)\n", point_id, lat, lon, featured);

                //Loop through place types and call Google Place Service
                Vector<String> placeTypes = getFeaturedPlaceType();

                for (String placeType : placeTypes) {
                    // Call gooogle place services
                    String radarSearchReq = placeSearch.prepareRadarSearch(lat, lon, rad, placeType);

                    System.out.format("HTTP GET: %s\n", radarSearchReq);
                    JSONObject response = placeSearch.executeRadarSearch(radarSearchReq);
                    processRadarSearchResponse(response);

                    String insertTransaction = "insert ignore into geominings(type, geom, request, params, response, author) value(";
                    StringBuilder insertTransactionBuilder = new StringBuilder(insertTransaction);
                    insertTransactionBuilder.append(1).append(", ST_GEOMCOLLFROMTEXT(\"Point(").append(lon).append(" ").append(lat).append(")\"),");
                    insertTransactionBuilder.append("\"").append(radarSearchReq).append("\",\"").append(placeType).append("\",\"").append(JSONObject.escape(response.toJSONString())).append("\",\"");
                    insertTransactionBuilder.append(author).append("\")");

                    System.out.println(insertTransactionBuilder.toString());
                    if (!isDebug) {
                        Statement insertTransactionStatement = mySqlConn.GetConnection().createStatement();
                        insertTransactionStatement.execute(insertTransactionBuilder.toString());
                    }
                }
            }

        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        } finally {
            mySqlConn.close();
        }
    }

    public void miningPhotosFromService() {
        try {
            mySqlConn.connect();
            String getPhotoQuery = "select `id`, `name` ,`place_id`, `reference`, `type`, `width`, `height` from photos where path is null order by `id` desc limit 200";
            String updatePathPhotoQueryFormat = "update `photos` set path=\"%s\" where `reference` like \"%s\"";
            Statement getPhotoQueryStatement = mySqlConn.GetConnection().createStatement();
            Statement updatePhotoPathStatement = mySqlConn.GetConnection().createStatement();
            ResultSet ds = getPhotoQueryStatement.executeQuery(getPhotoQuery);

            String getPhotoUrl = null, photo_reference = null, place_id = null, placePhotoFolder = null, place_name = null, photo_name = null, absolute_photo_path = null;
            long id;
            while (ds.next()) {
                id = (long) ds.getLong("id");
                photo_reference = (String) ds.getString("reference");
                place_id = (String) ds.getString("place_id");
                place_name = (String) ds.getString("name");

                String folderName = Utilities.md5(place_id);
                String placeFolder = "pl";
                placePhotoFolder = photoPath + "\\" + folderName + "\\pl";
                Utilities.CreateDirWithNonExistentParentDir(placePhotoFolder);
                System.out.format("Create directory if non exist (%s)\n", placePhotoFolder);

                photo_name = Utilities.getSequenceNameInFolder(place_name, placePhotoFolder);

                getPhotoUrl = placeSearch.preparePlacePhotoSearch(photo_reference);
                System.out.format("Mining photo for id(%d) place_id(%s) photo_reference(%s)\n", id, place_id, photo_reference);
                System.out.format("HTTP GET: %s\n", getPhotoUrl);
                PhotoResponse imgResponse = placeSearch.getPhotoFromURL(getPhotoUrl);

                if (imgResponse != null) {
                    BufferedImage bufferedImg = imgResponse.getBufferedImage();
                    System.out.println("MIME type: " + imgResponse.getMimeType());
                    if (imgResponse.getMimeType().startsWith("image/")) {
                        
                        absolute_photo_path = placePhotoFolder + "\\" + photo_name + "." + imgResponse.getImageType();
                        Utilities.saveBufferedIamage(bufferedImg, absolute_photo_path, imgResponse.getImageType());
                        
                        //Update photo path 
                        String identifier = folderName + "/" + placeFolder + "/" + photo_name;
                        identifier = URLEncoder.encode(identifier, "UTF-8");
                        
                        String updatePhotoPathQuery = String.format(updatePathPhotoQueryFormat, identifier, photo_reference);
                        System.out.println("Inserting...!");
                        System.out.println(updatePhotoPathQuery);
                        updatePhotoPathStatement.execute(updatePhotoPathQuery);
                    }
                    else
                    {
                        System.err.format("MIME not soupported!\n", getPhotoUrl);
                    }
                } else {
                    System.err.format("Response invalid!\n", getPhotoUrl);
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TraveleeTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mySqlConn.close();
        }
    }

    public void miningPhotoFromPlace() {
        try {
            mySqlConn.connect();
            String selectResponsePlace = "select `id`, `place_id`, `name`, `response` from places where `getphoto`=0 limit 2000";
            String insertPhotoQuery = "insert into photos(`place_reference` ,`place_id`,`name`,`reference`,`width`,`height`) values";
            StringBuilder insertPhotosQueryBuilder = new StringBuilder(insertPhotoQuery);

            Statement selectResponsePlaceStatement = mySqlConn.GetConnection().createStatement();
            Statement updateGetPhotoFlagStatement = mySqlConn.GetConnection().createStatement();
            Statement insertStatement = mySqlConn.GetConnection().createStatement();

            ResultSet ds = selectResponsePlaceStatement.executeQuery(selectResponsePlace);
            int id = 0, photo_width = 0, photo_height = 0;
            String place_id = null, name = null, response = null, photo_reference = null;
            JSONObject resObj = null, resultObj = null, photoObj = null;
            JSONArray photosArr = null;
            Iterator<JSONObject> photosIterator = null;
            while (ds.next()) {
                id = ds.getInt("id");
                place_id = (String) ds.getString("place_id");
                name = (String) ds.getString("name");
                response = (String) ds.getString("response");

                System.out.format("Get photo from place_id(%s) name(%s)\n", place_id, name);
                if (response != null) {
                    resObj = (JSONObject) parser.parse(response);
                    resultObj = (JSONObject) resObj.get("result");
                    photosArr = (JSONArray) resultObj.get("photos");
                    if (photosArr != null) {
                        photosIterator = photosArr.iterator();
                        if (photosIterator.hasNext()) {
                            photoObj = (JSONObject) photosIterator.next();

                            photo_width = ((Long) photoObj.get("width")).intValue();
                            photo_height = ((Long) photoObj.get("height")).intValue();
                            photo_reference = (String) photoObj.get("photo_reference");
                            //insert db
                            insertPhotosQueryBuilder.append("(\"").append(id).append("\",\"").append(place_id).append("\",\"").append(name).append("\",\"").append(photo_reference).append("\",");
                            insertPhotosQueryBuilder.append(photo_width).append(",").append(photo_height).append("),");

                        }
                    } else {
                        System.out.println("Not photos data!");//Set flag getphoto = 3 (Fulture: ranking trending..place)
                    }
                }

                if (insertPhotosQueryBuilder.length() != insertPhotoQuery.length()) {
                    //remove last colon if exist
                    insertPhotosQueryBuilder.deleteCharAt(insertPhotosQueryBuilder.length() - 1);
                    //insert db
                    //if (!isDebug) {
                    System.out.println("Inserting...");
                    System.out.println(insertPhotosQueryBuilder.toString());
                    insertStatement.execute(insertPhotosQueryBuilder.toString());
                    //}
                    
                    //reset query builder
                    insertPhotosQueryBuilder = new StringBuilder();
                    insertPhotosQueryBuilder.append(insertPhotoQuery);
                }

                //set getpho flag on places
                String updateGetPhotoFlagFormat = "update places set `getphoto`=1 where `place_id` like \"%s\"";
                String updateGetPhotoFlagQuery = String.format(updateGetPhotoFlagFormat, place_id);

                System.out.println(updateGetPhotoFlagQuery);
                updateGetPhotoFlagStatement.execute(updateGetPhotoFlagQuery);
            }
        } catch (SQLException | ParseException ex) {
            System.out.println(ex.getMessage());
        } finally {
            mySqlConn.close();
        }
    }

    public void miningDetailPlace() {
        try {
            mySqlConn.connect();

            String qDetailPlace = "select id, place_id from places where response is null limit 500";
            Statement qDetailPlaceStatement = mySqlConn.GetConnection().createStatement();
            ResultSet res = qDetailPlaceStatement.executeQuery(qDetailPlace);

            int id = -1;
            String place_id = null;
            while (res.next()) {
                id = res.getInt("id");
                place_id = res.getString("place_id");
                System.out.format("Mining place id(%d) place_id(%s)\n", id, place_id);

                //Call google place service
                String placeDetailRequest = placeSearch.prepareDetailSearch(place_id);
                System.out.format("HTTP GET: %s\n", placeDetailRequest);

                JSONObject response = placeSearch.executeDetailSearch(placeDetailRequest);
                processDetailSearchResponse(response, place_id);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            mySqlConn.close();
        }
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the isDebug
     */
    public boolean IsDebug() {
        return isDebug;
    }

    /**
     * @param isDebug the isDebug to set
     */
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * @return the photoPath
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * @param photoPath the photoPath to set
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    /**
     * @return the autoKey
     */
    public boolean isAutoKey() {
        return this.autoKey;
    }

    /**
     * @param autoKey the autoKey to set
     */
    public void setAutoKey(boolean autoKey) {
        this.autoKey = autoKey;
    }
}
