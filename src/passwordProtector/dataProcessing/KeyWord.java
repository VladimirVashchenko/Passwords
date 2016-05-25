package passwordProtector.dataProcessing;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by Administrator on 02.09.2015.
 */
public class KeyWord {
    private String user;
    private String key;
    private static KeyWord instance;

    public static KeyWord getInstance() {
        if (instance == null) {
            instance = new KeyWord();
        }
        return instance;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * смешивает символы логина и пароля, чтобы использовать новую комбинацию символов для генерации ключа шифрования
     */
    public String makeSecretKey(String username, String password, String salt) throws UnsupportedEncodingException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] userBytes = username.getBytes("UTF-8");
        byte[] passBytes = password.getBytes("UTF-8");

        int length = userBytes.length + passBytes.length;
        byte[] newSequence = new byte[length];

        int i = 0;
        int userIndex = 0;
        int passIndex = 0;

        if (userBytes.length < passBytes.length) {
            while (userIndex < userBytes.length) {
                newSequence[i++] = userBytes[userIndex++];
                newSequence[i++] = passBytes[passIndex++];
            }
            while(passIndex < passBytes.length){
                newSequence[i++] = passBytes[passIndex++];
            }

        } else {
            while (passIndex < passBytes.length) {
                newSequence[i++] = passBytes[passIndex++];
                newSequence[i++] = userBytes[userIndex++];
            }
            while(userIndex < userBytes.length){
                newSequence[i++] = userBytes[userIndex++];
            }
        }

        String sequence = new String(newSequence, "UTF-8");
        return PasswordHash.createHash2(sequence, salt);
    }
}
