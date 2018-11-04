package kr.ac.mju.mjuapp.cipher;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * @author davidkim
 * 
 */
public class CipherManager {
	private static final String Algorithm = "AES";
	private static final String Transformation = "/ECB/PKCS5Padding";

	/**
	 * 주어진 데이터로, 해당 알고리즘에 사용할 비밀키(SecretKey)를 생성한다.
	 * 
	 * @param algorithm
	 *            DES/DESede/TripleDES/AES
	 * @param keyData
	 * @return
	 */
	private static Key generateKey(String algorithm, byte[] keyData)
			throws NoSuchAlgorithmException, InvalidKeyException,
			InvalidKeySpecException {
		if ("DES".equals(algorithm)) {
			KeySpec keySpec = new DESKeySpec(keyData);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory
					.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			return secretKey;
		} else if ("DESede".equals(algorithm) || "TripleDES".equals(algorithm)) {
			KeySpec keySpec = new DESedeKeySpec(keyData);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory
					.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			return secretKey;
		} else {
			SecretKeySpec keySpec = new SecretKeySpec(keyData, algorithm);
			return keySpec;
		}
	}

	/**
	 * @param context
	 * @return
	 */
	private static byte[] initKey(Context context) {
		TelephonyManager tpm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tpm.getDeviceId();
		byte[] deviceIdBytes = deviceId.getBytes();
		int len = deviceIdBytes.length;
		byte[] keyBytes = new byte[16];

		if (len >= 16)
			System.arraycopy(deviceIdBytes, 0, keyBytes, 0, 16);
		else {
			System.arraycopy(deviceIdBytes, 0, keyBytes, 0, len);
			for (int i = 0; i < (16 - len); i++) {
				keyBytes[len + i] = deviceIdBytes[i % len];
			}
		}
		return keyBytes;
	}

	/**
	 * @param bytes
	 * @return
	 */
	private static String toHexString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
			result.append(Integer.toString((b & 0xF0) >> 4, 16));
			result.append(Integer.toString(b & 0x0F, 16));
		}
		return result.toString();
	}

	/**
	 * @param hexString
	 * @return
	 */
	private static byte[] toHexByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = (byte) Short.parseShort(
					hexString.substring(2 * i, 2 * i + 2), 16);
		return result;
	}

	/**
	 * @param plainText
	 * @param context
	 * @return
	 */
	public static String encryptDES(String plainText, Context context) {
		return crypt(Cipher.ENCRYPT_MODE, plainText, context);
	}

	/**
	 * @param cipherText
	 * @param context
	 * @return
	 */
	public static String decryptDES(String cipherText, Context context) {
		return crypt(Cipher.DECRYPT_MODE, cipherText, context);
	}

	/**
	 * @param mode
	 * @param text
	 * @param context
	 * @return
	 */
	private static String crypt(int mode, String text, Context context) {
		try {
			// key set
			byte[] keyBytes = initKey(context);
			// Log.d("MDC", "initKey : " + toHexString(keyBytes));
			Key key = generateKey(Algorithm, keyBytes);
			Cipher cipher = Cipher.getInstance(Algorithm + Transformation);
			cipher.init(mode, key);

			// cipher
			byte[] beforeCipher = null;
			if (mode == Cipher.ENCRYPT_MODE)
				beforeCipher = text.getBytes();
			else if (mode == Cipher.DECRYPT_MODE)
				beforeCipher = toHexByte(text);
			byte[] afterCipher = cipher.doFinal(beforeCipher);
			// debug
			// Log.d("MDC", "before(toHexString) : " +
			// toHexString(beforeCipher));
			// Log.d("MDC", "after(toHexString) : " + toHexString(afterCipher));
			// Log.d("MDC", "new String(after) : " + new String(afterCipher));
			// return result
			if (mode == Cipher.ENCRYPT_MODE)
				return toHexString(afterCipher);
			else if (mode == Cipher.DECRYPT_MODE)
				return new String(afterCipher);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
/* end of file */
