
import java.security.Key;   
import java.util.Random;
import javax.crypto.Cipher;   
import javax.crypto.SecretKey;    
import javax.crypto.SecretKeyFactory;   
import javax.crypto.spec.PBEKeySpec;   
import javax.crypto.spec.PBEParameterSpec; 

/**
 * 另一种对称加密算法
 * PBE——Password-based encryption（基于密码加密）。其特点在于口令由用户自己掌管，
 * 不借助任何物理媒体；采用随机数（这里我们叫做盐）杂凑多重加密等方法保证数据的安全性。
 * 是一种简便的加密方式 
 */
public class Type2_PBE extends HMAC{

	/**  
	  * 支持以下任意一种算法  
	  *  <pre>   
	  *  PBEWithMD5AndDES   
	  *  PBEWithMD5AndTripleDES   
	  *  PBEWithSHA1AndDESede  
	  *  PBEWithSHA1AndRC2_40  
	  *  /pre>  
	  *  */    
	public static final String ALGORITHM = "PBEWITHMD5andDES"; 
	
	/**  
	 * 盐初始化  
	 *  @return   
	 *  @throws Exception  
	 */    
	public static byte[] initSalt() throws Exception {   
		byte[] salt = new byte[8];   
		Random random = new Random();   
		random.nextBytes(salt);   
		return salt;   
	}  
	
	/**  
	 *  转换密钥<br>  
	 *  @param password  
	 *  @return   
	 *  @throws Exception  
	 * */    
	private static Key toKey(String password) throws Exception {   
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray ());   
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);   
		SecretKey secretKey = keyFactory.generateSecret(keySpec);   
		return secretKey;   
	}
	
	/**  
	 * 加密  
	 * @param data  
	 * 数据 
	 * @param password
	 * 密码  
	 * @param salt   盐  
	 * @return
	 *  @throws Exception
	 *  */  
	public static byte[] encrypt(byte[] data, String password, byte [] salt)    
			throws Exception {
		Key key = toKey(password);   
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);    
		Cipher cipher = Cipher.getInstance(ALGORITHM);   
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);   
		return cipher.doFinal(data);   
	}
	
	/**  
	 *  解密  
	 *  @param data  
	 *  数据  
	 *  @param password  
	 *  密码  
	 * @param salt 盐  
	 * @return   
	 * @throws Exception  
	 **/    
	public static byte[] decrypt(byte[] data, String password, byte [] salt)
			throws Exception {   
		Key key = toKey(password);   
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);   
		Cipher cipher = Cipher.getInstance(ALGORITHM);  
	    cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);   
	    return cipher.doFinal(data);  
	}   
	
	
	public static void main(String[] args) throws Exception{
		 String inputStr = "abc";    
		 System.err.println("原文: " + inputStr);  
		 byte[] input = inputStr.getBytes();   
		 String pwd = "efg";    
		 System.err.println("密码: " + pwd);  
		 byte[] salt = initSalt();   
		 byte[] data = encrypt(input, pwd, salt); 
		 System.err.println("加密后: " + encryptBASE64(data));  
		 byte[] output = decrypt(data, pwd, salt);  
		 String outputStr = new String(output);  
		 System.err.println("解密后: " + outputStr);  
//		 assertEquals(inputStr, outputStr);   
	}

}
