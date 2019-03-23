package MainPackage;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Cryptogram {

    public static boolean isKeyLengthValid(String secretKey) {
        return secretKey.length() == 16 || secretKey.length() == 24 || secretKey.length() == 32;
    }

    public static String encrypt(String secretKey, String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec Key = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, Key);
        return new String(Hex.encodeHex(cipher.doFinal(plainText.getBytes("UTF-8")), false));
    }

    public static String decrypt(String secretKey, String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, DecoderException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher decriptCipher = Cipher.getInstance("AES");
        SecretKeySpec Key = new SecretKeySpec(secretKey.getBytes(), "AES");
        decriptCipher.init(Cipher.DECRYPT_MODE, Key);
        return new String(decriptCipher.doFinal(Hex.decodeHex(cipherText.toCharArray())));
    }
}