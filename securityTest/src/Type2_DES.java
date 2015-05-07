import java.security.Key;
import java.security.SecureRandom; 
import javax.crypto.Cipher;   
import javax.crypto.KeyGenerator; 
import javax.crypto.SecretKey;  
import javax.crypto.SecretKeyFactory;   
import javax.crypto.spec.DESKeySpec;  


/**
 * 最常用的DES数据加密算法是对称加密算法
 * DES算法的入口参数有三个:Key、Data、Mode。其中Key为8个字节共64位,
 * 是DES算法的工作密钥;Data也为8个字节64位,是要被加密或被解密的数据;Mode为DES的工作方式,有两种:加密或解密
 * 具体内容 需要关注 JDK Document http://.../docs/technotes/guides/security/SunProviders.html 
 */
public class Type2_DES extends HMAC{
	/**   
	 * ALGORITHM 算法 <br>  
	 * 可替换为以下任意一种算法，同时key值的size相应改变。  
	 *  <pre> 
	 *  DES      key size must be equal to 
 		DESede(TripleDES)    key size must be equal to 112 or 168 
		AES                  key size must be equal to 128, 192 or 25 6,but 192 and 256 bits may not be available 
		Blowfish             key size must be multiple of 8, and can o nly range from 32 to 448 (inclusive) 
		RC2                  key size must be between 40 and 1024 bit s   
		RC4(ARCFOUR)         key size must be between 40 and 1024 bit s   
		</pre>     
	 	在Key toKey(byte[] key)方法中使用下述代码  
	 	<code>SecretKey secretKey = new SecretKeySpec(key, ALGORITHM); </code> 替换  
	 	<code>
	 	DESKeySpec dks = new DESKeySpec(key);   
	 	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALG ORITHM);
	 	SecretKey secretKey = keyFactory.generateSecret(dks); 
	 	*/    
	public static final String ALGORITHM = "DES";  
	
	/**
	 * 转换密钥<br>
	 *  @param key  
	 *  @return   
	 *  @throws Exception  
	 *   */ 
	private static Key toKey(byte[] key) throws Exception {   
		DESKeySpec dks = new DESKeySpec(key);    
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM); 
		SecretKey secretKey = keyFactory.generateSecret(dks);   
		// 当使用其他对称加密算法时，如AES、Blowfish等算法时，用下述代码替换上 述三行代码    
		// SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);   
		return secretKey;   
	} 
	/**  
	 *  解密  
	 *   @param data
	 *   @param key  
	 *   @return  
	 *    @throws Exception
	 */    
	public static byte[] decrypt(byte[] data, String key) throws Exception {   
		Key k = toKey(decryptBASE64(key));   
		Cipher cipher = Cipher.getInstance(ALGORITHM);   
		cipher.init(Cipher.DECRYPT_MODE, k);   
		return cipher.doFinal(data);   
	}
	 /**  
	  * 加密  
	  * @param data  
	  * @param key  
	  * @return   
	  *  @throws Exception 
	  * */    
	public static byte[] encrypt(byte[] data, String key) throws Exception {
		Key k = toKey(decryptBASE64(key));    
		Cipher cipher = Cipher.getInstance(ALGORITHM);  
		cipher.init(Cipher.ENCRYPT_MODE, k);   
		return cipher.doFinal(data);   
	}
	/**  
	 * 生成密钥  
	 * @return   
	 * @throws Exception  
	 * */    
	public static String initKey() throws Exception {   
		return initKey(null);   
	}
	
	/**  
	 * 生成密钥  
	 * @param seed  
	 * @return   
	 *  @throws Exception 
	 **/    
	public static String initKey(String seed) throws Exception {   
		SecureRandom secureRandom = null;   
	    if (seed != null) {  
	    secureRandom = new SecureRandom(decryptBASE64(seed));  
	    } else {    
	    	secureRandom = new SecureRandom(); 
	    	}   
	    KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);   
	    kg.init(secureRandom);   
	    SecretKey secretKey = kg.generateKey();   
	    return encryptBASE64(secretKey.getEncoded());
	}   
	
	/**
	 * 由控制台得到的输出，我们能够比对加密、解密后结果一致。这是一种简单的加密解密方式，只有一个密钥。  
   	        其实DES有很多同胞兄弟，如DESede(TripleDES)、AES、Blowfish、RC2、RC4(ARCFOUR)。这里就不过多阐述了，
	        大   同小异，只要换掉ALGORITHM换成对应的值，
	        同时做一个代码替换SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);就可以了，此外就是密钥长度不同了
	 */
	public static void main(String[] args) throws Exception{
		String inputStr = "DES";    
		String key = initKey();   
		System.err.println("原文:\t" + inputStr);   
		System.err.println("密钥:\t" + key);   
		byte[] inputData = inputStr.getBytes();  
		inputData = encrypt(inputData, key);   
		System.err.println("加密后:\t" + encryptBASE64(inputData));   
	    byte[] outputData = decrypt(inputData, key);   
	    String outputStr = new String(outputData);   
	    System.err.println("解密后:\t" + outputStr);   
//	    assertEquals(inputStr, outputStr);
	}
}
