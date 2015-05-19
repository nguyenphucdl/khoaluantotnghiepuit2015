package traveleescripts;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import javax.imageio.ImageIO;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 *
 * @author nguyenphucuit
 */
public class PlaceSearch {

    private String apiKey = null;
    private String radarUrl = null;
    private String detailedUrl = null;
    private boolean isDebug = false;
    private JSONParser parser = null;
    private Integer countRadarSearchRequest = 0;
    private URL url = null;
    private HttpURLConnection connection = null;
    private ApiKeyPools apiKeyPools = null;
    private boolean autoKey = false;
    
    public PlaceSearch() {
        apiKeyPools = new ApiKeyPools("place_api_key");
    }
    
    public String prepareRadarSearch(double lat, double lon, int rad, String types) {
        
        
        StringBuilder reqRadarBuilder = new StringBuilder();
        reqRadarBuilder.append(radarUrl);
        reqRadarBuilder.append("location=").append(lat).append(",").append(lon);
        reqRadarBuilder.append("&radius=").append(rad);
        reqRadarBuilder.append("&types=").append(types);
        reqRadarBuilder.append("&key=").append(getApiKey());

        return reqRadarBuilder.toString();
    }

    public String prepareDetailSearch(String placeId) {
        StringBuilder reqDetailBuilder = new StringBuilder();
        reqDetailBuilder.append(detailedUrl);
        reqDetailBuilder.append("placeid=").append(placeId);
        reqDetailBuilder.append("&key=").append(getApiKey());

        return reqDetailBuilder.toString();
    }

    public String preparePlacePhotoSearch(String photo_reference) {
        String placePhotoSearchFormat = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1600&photoreference=%s&key=%s";
        String placePhotoSearchQuery = String.format(placePhotoSearchFormat, photo_reference, getApiKey());
        return placePhotoSearchQuery;
    }

    public String execute(String request) {
        String response = null;
        return response;
    }

    public String executeGet(String targetURL) {
        String response = null;
        try {
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            Thread.sleep(500);

            response = getStringFromInputStream(connection.getInputStream());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public JSONObject executeRadarSearch(String url) {
        JSONObject response = null;
        try {
            if (isDebug) {
                //hook file radar.json
                String radarResponseName = "radar.json";
                String workingDirectory = System.getProperty("user.dir");
                Path radarResponsePath = Paths.get(workingDirectory + "\\" + radarResponseName);

                response = (JSONObject) parser.parse(new FileReader(radarResponsePath.toString()));
            } else {
                //Call gooogle services
                String res = executeGet(url);
                if (res != null) {
                    response = (JSONObject) parser.parse(res);
                }

                countRadarSearchRequest++;
            }
        } catch (ParseException | IOException ex) {
            Logger.getLogger(TraveleeTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public JSONObject executeDetailSearch(String url) {
        JSONObject response = null;
        try {
            if (isDebug) {
                //hook file detail.json
                String detailResponseName = "detail.json";
                String workingDirectory = System.getProperty("user.dir");
                Path radarResponsePath = Paths.get(workingDirectory + "\\" + detailResponseName);

                response = (JSONObject) parser.parse(new FileReader(radarResponsePath.toString()));
            } else {
                //Call gooogle services
                String res = executeGet(url);
                if (res != null) {
                    response = (JSONObject) parser.parse(res);
                }
            }

        } catch (ParseException | IOException ex) {
            Logger.getLogger(TraveleeTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    //NOT USED
    public Image executePhotoSearch(String targetURL) {
        Image response = null;
        try {
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int response_code = connection.getResponseCode();
            System.out.format("Response code %d\n", response_code);
            
            
            //Image image = null;
            response = ImageIO.read(url);
            //BufferedImage bufferedImg = toBufferedImage(image);
            //save(bufferedImg, "jpg");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public PhotoResponse getPhotoFromURL(String targetURL) {
        PhotoResponse response = null;
        BufferedImage bufferedImage = null;
        try {
            Thread.sleep(200);
            // URLConnection.guessContentTypeFromStream only needs the first 12 bytes, but
            // just to be safe from future java api enhancements, we'll use a larger number
            int pushbackLimit = 100;
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            InputStream urlStream = connection.getInputStream();
            
            int response_code = connection.getResponseCode();
            if(response_code > 300)
            {
                System.err.format("Response code %d\n", response_code);
                return null;
            }
            System.out.format("Response code %d\n", response_code);
            
            
            PushbackInputStream pushUrlStream = new PushbackInputStream(urlStream, pushbackLimit);
            byte[] firstBytes = new byte[pushbackLimit];
            // download the first initial bytes into a byte array, which we will later pass to
            // URLConnection.guessContentTypeFromStream
            pushUrlStream.read(firstBytes);
            // push the bytes back onto the PushbackInputStream so that the stream can be read
            // by ImageIO reader in its entirety
            pushUrlStream.unread(firstBytes);
            
            String imageType = null;
            // Pass the initial bytes to URLConnection.guessContentTypeFromStream in the form of a
            // ByteArrayInputStream, which is mark supported.
            ByteArrayInputStream bais = new ByteArrayInputStream(firstBytes);
            String mimeType = URLConnection.guessContentTypeFromStream(bais);
            if (mimeType.startsWith("image/")) {
                imageType = mimeType.substring("image/".length());
            }
            // else handle failure here
            
            
            
            // read in image
            bufferedImage = ImageIO.read(pushUrlStream);
            response = new PhotoResponse(bufferedImage, mimeType);
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(PlaceSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public String getStringFromInputStream(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        String api_key = apiKey;
        if(autoKey){
            api_key = apiKeyPools.getRandom();
        }
        return api_key;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the detailedUrl
     */
    public String getDetailedUrl() {
        return detailedUrl;
    }

    /**
     * @param detailedUrl the detailedUrl to set
     */
    public void setDetailedUrl(String detailedUrl) {
        this.detailedUrl = detailedUrl;
    }

    /**
     * @return the radarUrl
     */
    public String getRadarUrl() {
        return radarUrl;
    }

    /**
     * @param radarUrl the radarUrl to set
     */
    public void setRadarUrl(String radarUrl) {
        this.radarUrl = radarUrl;
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
    public void setIsDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * @return the parser
     */
    public JSONParser getParser() {
        return parser;
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(JSONParser parser) {
        this.parser = parser;
    }

    /**
     * @return the autoKey
     */
    public boolean isAutoKey() {
        return autoKey;
    }

    /**
     * @param autoKey the autoKey to set
     */
    public void setAutoKey(boolean autoKey) {
        this.autoKey = autoKey;
    }
}
