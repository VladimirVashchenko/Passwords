package passwordProtector.dataProcessing;

import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Этот класс выполняет расшифровку текста с использованием AES PBE.
 * Создан на основе класса для расшифровывания файлов найденного на
 * <a href="http://javapapers.com/java/java-file-encryption-decryption-using-aes-password-based-encryption-pbe">javapapers</a>
 */
public class AESDecryption {
    public static String decrypt(String encryptedText, String keyWord) throws Exception {
        String[] encryptedArray = encryptedText.split(":");
        byte[] salt = PasswordHash.fromHex(encryptedArray[0]);
        byte[] iv = PasswordHash.fromHex(encryptedArray[1]);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(keyWord.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");

        // file decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedArray[2]);

        //System.out.println("encryptedTextByte: " + Arrays.toString(encryptedArray));

        byte[] output = cipher.doFinal(encryptedTextByte);

        //System.out.println("output: " + Arrays.toString(output));

        String outputStr = new String(output, "UTF-8");

        //System.out.println("outputStr: " + outputStr);
        //System.out.println("outputStr.getBytes(): " + Arrays.toString(outputStr.getBytes()));

        return outputStr;
    }
}