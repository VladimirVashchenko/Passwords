package passwordProtector;

import java.io.File;
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

    public static boolean deleteFile(String path, String name) {
        File file = new File(path + name + ".db");

        return file.exists() && file.delete();
    }

    public static boolean deleteDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        String[] files = dir.list();
        for (String file : files) {
            File f = new File(dir, file);
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        return dir.delete();
    }
}
