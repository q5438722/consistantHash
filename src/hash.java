import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hash
{
    public static final int hashLen = 16;
    public static final int hashMAX = 16384;

    public static int getHash(String inputLine) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] bytes = md.digest(inputLine.getBytes(StandardCharsets.UTF_8));
        return ((bytes[0] * bytes[1]) + 8192) % hashMAX;
    }
}
