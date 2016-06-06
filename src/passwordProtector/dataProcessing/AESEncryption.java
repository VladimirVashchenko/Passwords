package passwordProtector.dataProcessing;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;


/**
 * Этот класс выполняет шифрование текста с использованием AES PBE.
 * Создан на основе класса для шифрования файлов найденного на
 * <a href="http://javapapers.com/java/java-file-encryption-decryption-using-aes-password-based-encryption-pbe">javapapers</a>
 */
public class AESEncryption {
    public static String getEncrypted(String inputStr, String keyWord) throws Exception {
        byte[] salt = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(keyWord.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);

        AlgorithmParameters params = cipher.getParameters();

        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        byte[] input = /*new byte[inputStr.length()];
        input = */inputStr.getBytes("UTF-8");
        //  System.out.println("input: " + input);

        byte[] output = cipher.doFinal(input);

        //System.out.println("output: " + output);

        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(output);

        //System.out.println("encryptedText: " + encryptedText);
        //System.out.println("encryptedText.getBytes(): " + Arrays.toString(encryptedText.getBytes()));

        return PasswordHash.toHex(salt) + ":" + PasswordHash.toHex(iv) + ":" + encryptedText;
    }
}