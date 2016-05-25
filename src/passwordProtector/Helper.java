package passwordProtector;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 23.04.2016.
 */
public class Helper {
    public static String filePath() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/")+1);

        return decodedPath;
    }
}
