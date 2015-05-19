package traveleescripts;

import java.awt.image.BufferedImage;

/**
 *
 * @author nguyenphucuit
 */
public class PhotoResponse {

    private String mimeType = null;
    private BufferedImage bufferedImage = null;
    private String imageType = null;

    public PhotoResponse() {

    }

    public PhotoResponse(BufferedImage bufferedImage, String mimeType) {
        this.mimeType = mimeType;
        this.bufferedImage = bufferedImage;
        getImageType();
    }

    public String getImageType() {
        if (mimeType.startsWith("image/")) {
            imageType = mimeType.substring("image/".length());
        }
        return imageType;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the bufferedImage
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * @param bufferedImage the bufferedImage to set
     */
    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
