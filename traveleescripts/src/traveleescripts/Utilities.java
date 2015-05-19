package traveleescripts;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.*;
import java.text.Normalizer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author nguyenphucuit
 */
public class Utilities {

    /**
     * Get a hash by txt and hashType
     *
     * @param txt, text in plain format
     * @param hashType MD5 OR SHA1, etc.. Note: check list of algorithms
     * http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest
     *
     * @return hash in hashType
     */
    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //error action
        }
        return null;
    }

    public static String md2(String txt) {
        return Utilities.getHash(txt, "MD2");
    }

    public static String md5(String txt) {
        return Utilities.getHash(txt, "MD5");
    }

    public static String sha1(String txt) {
        return Utilities.getHash(txt, "SHA1");
    }

    public static String sha224(String txt) {
        return Utilities.getHash(txt, "SHA-224");
    }

    public static String sha256(String txt) {
        return Utilities.getHash(txt, "SHA-256");
    }

    public static String sha384(String txt) {
        return Utilities.getHash(txt, "SHA-384");
    }

    public static String sha512(String txt) {
        return Utilities.getHash(txt, "SHA-512");
    }

    public static String generateUniqueIDs(String name) {
        String code = null;
        try {
            //Initialize SecureRandom
            //This is a lengthy operation, to be done only upon
            //initialization of the application
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

            //generate a random number
            String randomNum = new Integer(prng.nextInt()).toString();

            //get its digest
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] result = sha.digest(randomNum.getBytes());
            code = hexEncode(result);
            System.out.println("Random number: " + randomNum);
            System.out.println("Message digest: " + code);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
        }
        return code;
    }

    /**
     * The byte[] returned by MessageDigest does not have a nice textual
     * representation, so some form of encoding is usually performed.
     *
     * This implementation follows the example of David Flanagan's book "Java In
     * A Nutshell", and converts a byte array into a String of hex characters.
     *
     * Another popular alternative is to use a "Base64" encoding.
     */
    static private String hexEncode(byte[] aInput) {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[b & 0x0f]);
        }
        return result.toString();
    }

    public static void saveBufferedIamage(BufferedImage image, String filePath, String ext) {
        File file = new File(filePath);
        try {
            ImageIO.write(image, ext, file);  // ignore returned boolean
            System.out.format("Save file to (%s)\n", file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Write error for " + file.getPath()
                    + ": " + e.getMessage());
        }
    }

    public static BufferedImage imageToBufferedImage(Image src) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;  // other options
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return dest;
    }

    /*
     * Here we will learn to create directory along with non existent parent Directory
     */
    public static void CreateDirWithNonExistentParentDir(String filePath) {

        // Here we are assuming that D:\Gautam\testing folder exists but first
        // and second folder is not there and user
        // has privilege to create directory in testing folder
        File file = new File(filePath);

        boolean b = false;

        /*
         * exists() method tests whether the file or directory denoted by this
         * abstract pathname exists or not accordingly it will return TRUE /
         * FALSE.
         */
        if (!file.exists()) {
            /*
             * mkdirs() method creates the directory mentioned by this abstract
             * pathname including any necessary but nonexistent parent
             * directories.
             * 
             * Accordingly it will return TRUE or FALSE if directory created
             * successfully or not. If this operation fails it may have
             * succeeded in creating some of the necessary parent directories.
             */
            b = file.mkdirs();
        }
        if (b) {
            System.out.println("Directory successfully created");
        } else {
            //System.out.println("Failed to create directory");
        }
    }

    public static String normalizeStringWithoutSpace(String txt) {
        String convertedString
                = Normalizer
                .normalize(txt, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        //convertedString = convertedString.toLowerCase().replaceAll(" ", "").replaceAll("/", "").replaceAll("'", txt);
        convertedString = convertedString.toLowerCase().replaceAll("[^0-9a-zA-Z]+", "");
        return convertedString;
    }

    public static String getSequenceNameInFolder(String prefix, String folder) {
        String photo_name_prefix = Utilities.normalizeStringWithoutSpace(prefix);

        int num = new File(folder).list().length + 1;

        String result = String.format("%s%d", photo_name_prefix, num);
        return result;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive. The
     * difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value. Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

    // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

    // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
