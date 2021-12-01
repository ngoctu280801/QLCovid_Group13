package model;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Encrypt {
	public static String toPBKDF2(String pwd, int length){
		if(length <= 0)
			return null;
//		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		byte[] hash = null;
		//random.nextBytes(salt);
		salt = getSalt(pwd);
		KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 1000, length);
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = factory.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toHex(hash);
	}
	
	private static byte[] getSalt(String pwd){
		String temp = pwd.substring(0, 8);
		temp = temp.concat(temp);
		return temp.getBytes();
	}
	
	private static String toHex(byte[] hash){
        BigInteger bi = new BigInteger(1, hash);
        String hex = bi.toString(16);
        int paddingLength = (hash.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
}
